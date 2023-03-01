/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU Affero通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
 *
 *
 */

package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.*
import love.forte.simbot.ID
import love.forte.simbot.bot.BotAlreadyRegisteredException
import love.forte.simbot.component.mirai.MiraiBotConfiguration
import love.forte.simbot.component.mirai.MiraiComponent
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.component.mirai.bot.MiraiBotManager
import love.forte.simbot.component.mirai.bot.MiraiBotManagerConfiguration
import love.forte.simbot.component.mirai.event.MiraiBotRegisteredEvent
import love.forte.simbot.component.mirai.event.impl.MiraiBotRegisteredEventImpl
import love.forte.simbot.component.mirai.simbotMiraiDeviceInfo
import love.forte.simbot.event.EventProcessingResult
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.event.pushIfProcessable
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.tryToLong
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.supervisorJob
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.LoggerAdapters.asMiraiLogger
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import net.mamoe.mirai.Bot as OriginalMiraiBot


/**
 *
 * @author ForteScarlet
 */
internal class MiraiBotManagerImpl(
    private val eventProcessor: EventProcessor,
    override val component: MiraiComponent,
    configuration: MiraiBotManagerConfiguration,
) : MiraiBotManager() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(MiraiBotManagerImpl::class)
    }
    
    override val logger: Logger get() = LOGGER
    
    private val completableJob: CompletableJob
    override val coroutineContext: CoroutineContext
    private val botHolder = ConcurrentHashMap<Long, MiraiBotImpl>()
    
    init {
        val configCoroutineContext = configuration.parentCoroutineContext + CoroutineName("MiraiBotManagerImpl")
        completableJob = SupervisorJob(configCoroutineContext[Job])
        coroutineContext = configCoroutineContext + completableJob
    }
    
    override fun register(
        code: Long,
        password: String,
        configuration: MiraiBotConfiguration,
    ): MiraiBotImpl {
        logger.debug("Register bot {} with password: <length {}>", code, password.length)
        val botConfiguration = configuration.createBotConfiguration {
            it ?: createInitialBotConfiguration()
        }.apply { configurationContext() }
        
        return processMiraiBot(code, configuration) {
            BotFactory.newBot(code, password, botConfiguration)
        }.also { bot ->
            launch { pushRegisteredEvent(bot) }
        }
    }
    
    override fun register(
        code: Long,
        password: ByteArray,
        configuration: MiraiBotConfiguration,
    ): MiraiBotImpl {
        logger.debug("Register bot {} with password(MD5): <size {}>", code, password.size)
        val botConfiguration = configuration.createBotConfiguration {
            it ?: createInitialBotConfiguration()
        }.apply { configurationContext() }
        return processMiraiBot(code, configuration) {
            BotFactory.newBot(code, password, botConfiguration)
        }.also { bot ->
            launch { pushRegisteredEvent(bot) }
        }
    }
    
    
    private suspend fun pushRegisteredEvent(bot: MiraiBotImpl): EventProcessingResult? {
        return eventProcessor.pushIfProcessable(MiraiBotRegisteredEvent) {
            MiraiBotRegisteredEventImpl(bot)
        }
    }
    
    
    private fun createInitialBotConfiguration(): BotConfiguration {
        return BotConfiguration {
            botLoggerSupplier = { LoggerFactory.getLogger("love.forte.simbot.mirai.bot.${it.id}").asMiraiLogger() }
            networkLoggerSupplier = { LoggerFactory.getLogger("love.forte.simbot.mirai.net.${it.id}").asMiraiLogger() }
            deviceInfo = { simbotMiraiDeviceInfo(it.id) }
        }
    }
    
    private fun BotConfiguration.configurationContext(): BotConfiguration {
        parentCoroutineContext += coroutineContext
        return this
    }
    
    private inline fun processMiraiBot(
        code: Long,
        configuration: MiraiBotConfiguration,
        crossinline factory: () -> OriginalMiraiBot,
    ): MiraiBotImpl {
        return botHolder.compute(code) { key, old ->
            if (old != null) {
                throw BotAlreadyRegisteredException("$key")
            }
            MiraiBotImpl(factory(), this@MiraiBotManagerImpl, eventProcessor, component, configuration)
        }!!.also {
            val originalBot = it.originalBot
            originalBot.supervisorJob.invokeOnCompletion {
                botHolder.compute(code) { _, old ->
                    if (old?.originalBot === originalBot) null else old
                }
            }
        }
    }
    
    
    override val isActive: Boolean get() = completableJob.isActive
    override val isCancelled: Boolean get() = completableJob.isCancelled
    override val isStarted: Boolean get() = isActive || isCancelled
    
    override suspend fun doCancel(reason: Throwable?): Boolean {
        if (isCancelled) return false
        val cancelledException: CancellationException? = when (reason) {
            null -> null
            is CancellationException -> reason
            else -> CancellationException(reason.localizedMessage, reason)
        }
        completableJob.cancel(cancelledException)
        return true
    }
    
    override fun get(id: ID): MiraiBot? = botHolder[id.tryToLong()]
    
    override fun all(): List<MiraiBot> {
        return botHolder.values.toList()
    }
    
    override fun invokeOnCompletion(handler: CompletionHandler) {
        completableJob.invokeOnCompletion(handler)
    }
    
    override suspend fun join() {
        completableJob.join()
    }
    
    override fun toString(): String {
        return "MiraiBotManager(bots=${
            botHolder.keys().asSequence().joinToString(", ", prefix = "[", postfix = "]")
        }, isActive=$isActive, eventProcessor$eventProcessor)@${hashCode()}"
    }
    
}




