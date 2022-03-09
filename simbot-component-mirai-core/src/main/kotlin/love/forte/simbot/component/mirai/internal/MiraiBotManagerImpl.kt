/*
 *  Copyright (c) 2022-2022 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU 通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU 通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU 通用公共许可证的复本。如果没有，请看:
 *  https://www.gnu.org/licenses
 *  https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *  https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 *
 *
 */

package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import love.forte.simbot.*
import love.forte.simbot.LoggerFactory
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.event.impl.*
import love.forte.simbot.event.*
import net.mamoe.mirai.*
import net.mamoe.mirai.utils.*
import org.slf4j.*
import java.util.concurrent.*
import kotlin.coroutines.*


/**
 *
 * @author ForteScarlet
 */
internal class MiraiBotManagerImpl(
    private val eventProcessor: EventProcessor
) : MiraiBotManager() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(MiraiBotManagerImpl::class)
    }

    override val logger: Logger get() = LOGGER

    private val completableJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = completableJob + CoroutineName("MiraiBotManagerImpl")
    private val botCache = ConcurrentHashMap<Long, MiraiBotImpl>()


    override fun register(code: Long, password: String, configuration: BotConfiguration): MiraiBotImpl {
        logger.debug("Register bot {} with password: <length {}>", code, password.length)
        return processMiraiBot(code) {
            BotFactory.newBot(code, password, configuration.configurationProcess())
        }.also { bot ->
            launch { pushRegisteredEvent(bot) }
        }
    }

    override fun register(code: Long, passwordMD5: ByteArray, configuration: BotConfiguration): MiraiBotImpl {
        logger.debug("Register bot {} with password(MD5): <size {}>", code, passwordMD5.size)
        return processMiraiBot(code) {
            BotFactory.newBot(code, passwordMD5, configuration.configurationProcess())
        }.also { bot ->
            launch { pushRegisteredEvent(bot) }
        }
    }


    override fun register(
        code: Long,
        password: String,
        configuration: BotFactory.BotConfigurationLambda
    ): MiraiBotImpl {
        logger.debug("Register bot {} with password: <length {}>", code, password.length)
        return processMiraiBot(code) {
            BotFactory.newBot(code, password, configuration.configurationProcess())
        }.also { bot ->
            launch { pushRegisteredEvent(bot) }
        }
    }

    override fun register(
        code: Long,
        passwordMD5: ByteArray,
        configuration: BotFactory.BotConfigurationLambda
    ): MiraiBotImpl {
        logger.debug("Register bot {} with password(MD5): <size {}>", code, passwordMD5.size)
        return processMiraiBot(code) {
            BotFactory.newBot(code, passwordMD5, configuration.configurationProcess())
        }.also { bot ->
            launch { pushRegisteredEvent(bot) }
        }
    }


    private suspend fun pushRegisteredEvent(bot: MiraiBotImpl): EventProcessingResult? {
        return eventProcessor.pushIfProcessable(MiraiBotRegisteredEvent) {
            MiraiBotRegisteredEventImpl(bot)
        }
    }


    private fun BotConfiguration.configurationProcess(): BotConfiguration {
        parentCoroutineContext += completableJob
        return this
    }

    private fun BotFactory.BotConfigurationLambda.configurationProcess(): BotFactory.BotConfigurationLambda {
        return BotFactory.BotConfigurationLambda {
            run { apply { invoke() } }
            parentCoroutineContext += completableJob
        }
    }

    private inline fun processMiraiBot(code: Long, crossinline factory: () -> OriginalMiraiBot): MiraiBotImpl {
        return botCache.compute(code) { key, old ->
            if (old != null) {
                throw BotAlreadyRegisteredException("$key")
            }
            MiraiBotImpl(factory(), this@MiraiBotManagerImpl, eventProcessor)
        }!!.also {
            invokeOnCompletion {
                botCache.remove(code)
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

    override fun get(id: ID): MiraiBot? = botCache[id.tryToLongID().number]

    override fun all(): Sequence<MiraiBot> {
        return botCache.values.asSequence()
    }

    override fun invokeOnCompletion(handler: CompletionHandler) {
        completableJob.invokeOnCompletion(handler)
    }

    override suspend fun join() {
        completableJob.join()
    }

    override fun toString(): String {
        return "MiraiBotManager@${hashCode()}(botSize=${botCache.size}, isActive=$isActive, eventProcessor$eventProcessor)"
    }

}




