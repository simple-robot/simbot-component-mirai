package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.SupervisorJob
import love.forte.simbot.BotAlreadyRegisteredException
import love.forte.simbot.ID
import love.forte.simbot.LoggerFactory
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiBotManager
import love.forte.simbot.component.mirai.NativeMiraiBot
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.tryToLongID
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import java.util.concurrent.ConcurrentHashMap


/**
 *
 * @author ForteScarlet
 */
internal class MiraiBotManagerImpl(
    private val eventProcessor: EventProcessor
) : MiraiBotManager() {
    companion object {
        private val logger = LoggerFactory.getLogger(MiraiBotManagerImpl::class)
    }

    private val completableJob = SupervisorJob()
    private val botCache = ConcurrentHashMap<Long, MiraiBotImpl>()


    override fun register(code: Long, password: String, configuration: BotConfiguration): MiraiBotImpl {
        logger.debug("Register bot {} with password: <length {}>", code, password.length)
        return processMiraiBot(code) {
            BotFactory.newBot(code, password, configuration.configurationProcess())
        }
    }

    override fun register(code: Long, passwordMD5: ByteArray, configuration: BotConfiguration): MiraiBotImpl {
        logger.debug("Register bot {} with password(MD5): <size {}>", code, passwordMD5.size)
        return processMiraiBot(code) {
            BotFactory.newBot(code, passwordMD5, configuration.configurationProcess())
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

    private inline fun processMiraiBot(code: Long, crossinline factory: () -> NativeMiraiBot): MiraiBotImpl {
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


    override fun invokeOnCompletion(handler: CompletionHandler) {
        completableJob.invokeOnCompletion(handler)
    }

    override suspend fun join() {
        completableJob.join()
    }


}




