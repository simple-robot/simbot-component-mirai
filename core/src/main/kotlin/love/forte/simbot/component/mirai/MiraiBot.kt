package love.forte.simbot.component.mirai

import love.forte.simbot.Bot
import love.forte.simbot.Component
import love.forte.simbot.LongID
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.message.Image
import love.forte.simbot.resources.Resource
import net.mamoe.mirai.supervisorJob
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext


/**
 * 原生的MiraiBot [net.mamoe.mirai.Bot] 类型。
 */
public typealias NativeMiraiBot = net.mamoe.mirai.Bot

/**
 *
 * Mirai的Bot [NativeMiraiBot] 在 simbot中的整合类型。
 *
 * @see NativeMiraiBot
 * @see Bot
 * @author ForteScarlet
 */
public interface MiraiBot : Bot {

    /**
     * 得到这个Bot所代表的原生mirai bot。
     *
     * @see NativeMiraiBot
     */
    public val nativeBot: NativeMiraiBot

    /**
     * bot的id。
     *
     * 在mirai中，bot的账号都是 [Long] 类型的。
     */
    override val id: LongID

    /**
     * @see Bot.eventProcessor
     */
    override val eventProcessor: EventProcessor


    /**
     * @see Bot.logger
     */
    override val logger: Logger


    /**
     *  @see Bot.avatar
     */
    override val avatar: String get() = nativeBot.avatarUrl

    override val component: Component get() = ComponentMirai.component

    /** 直接使用 [nativeBot] 的协程作用域。 */
    override val coroutineContext: CoroutineContext get() = nativeBot.coroutineContext

    override val isActive: Boolean get() = nativeBot.supervisorJob.isActive
    override val isCancelled: Boolean get() = nativeBot.supervisorJob.isCancelled
    override val isStarted: Boolean get() = isCancelled || isActive

    override val manager: MiraiBotManager

    override val username: String get() = nativeBot.nick

    /**
     * TODO 注释
     */
    override suspend fun uploadImage(resource: Resource): Image<*> {
        return uploadImage(resource, false)
    }

    /**
     * TODO 注释
     */
    public suspend fun uploadImage(resource: Resource, flash: Boolean): Image<*>

    override suspend fun join() {
        nativeBot.join()
    }

    override suspend fun start(): Boolean {
        nativeBot.login()
        return true
    }

    override suspend fun cancel(reason: Throwable?): Boolean {
        return if (isCancelled) false
        else true.also {
            nativeBot.close(reason)
        }
    }
}