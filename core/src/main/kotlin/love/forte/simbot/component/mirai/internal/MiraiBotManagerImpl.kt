package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.Job
import love.forte.simbot.ID
import love.forte.simbot.LoggerFactory
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiBotManager
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration


/**
 *
 * @author ForteScarlet
 */
internal class MiraiBotManagerImpl : MiraiBotManager() {
    companion object {
        private val logger = LoggerFactory.getLogger(MiraiBotManagerImpl::class)
    }

    private val completableJob = Job()


    override fun register(code: Long, password: String, configuration: BotConfiguration): MiraiBot {
        TODO("Not yet implemented")
    }

    override fun register(code: Long, passwordMD5: ByteArray, configuration: BotConfiguration): MiraiBot {
        TODO("Not yet implemented")
    }

    override fun register(code: Long, password: String, configuration: BotFactory.BotConfigurationLambda): MiraiBot {
        TODO("Not yet implemented")
    }

    override fun register(
        code: Long,
        passwordMD5: ByteArray,
        configuration: BotFactory.BotConfigurationLambda
    ): MiraiBot {
        TODO("Not yet implemented")
    }

    override val isActive: Boolean
        get() = TODO("Not yet implemented")
    override val isCancelled: Boolean
        get() = TODO("Not yet implemented")
    override val isStarted: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun doCancel(reason: Throwable?): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(id: ID): MiraiBot? {
        TODO("Not yet implemented")
    }

    override fun invokeOnCompletion(handler: CompletionHandler) {
        TODO("Not yet implemented")
    }

    override suspend fun join() {
        TODO("Not yet implemented")
    }

    override suspend fun start(): Boolean {
        TODO("Not yet implemented")
    }
}




