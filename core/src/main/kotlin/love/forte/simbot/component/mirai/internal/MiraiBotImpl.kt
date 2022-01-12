package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import love.forte.simbot.*
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.NativeMiraiBot
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.event.impl.MiraiFriendMessageEventImpl
import love.forte.simbot.component.mirai.event.impl.MiraiGroupMessageEventImpl
import love.forte.simbot.component.mirai.event.impl.MiraiMemberMessageEventImpl
import love.forte.simbot.component.mirai.event.impl.MiraiStrangerMessageEventImpl
import love.forte.simbot.component.mirai.message.MiraiSendOnlyImageImpl
import love.forte.simbot.component.mirai.message.asSimbot
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.event.Event
import love.forte.simbot.event.EventProcessingResult
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.event.pushIfProcessable
import love.forte.simbot.message.Image
import love.forte.simbot.resources.IDResource
import love.forte.simbot.resources.Resource
import love.forte.simbot.resources.StreamableResource
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.message.data.flash
import org.slf4j.Logger
import java.util.stream.Stream
import net.mamoe.mirai.message.data.Image as miraiImageFunc


/**
 *
 * @author ForteScarlet
 */
internal class MiraiBotImpl(
    override val nativeBot: NativeMiraiBot,
    override val manager: MiraiBotManagerImpl,
    override val eventProcessor: EventProcessor
) : MiraiBot {
    override val logger: Logger = LoggerFactory.getLogger("love.forte.simbot.mirai.bot.${nativeBot.id}")
    override val id: LongID = nativeBot.id.ID
    override val status: UserStatus get() = MiraiBotStatus

    override suspend fun friends(limiter: Limiter): Flow<MiraiFriend> {
        return nativeBot.friends.asFlow().map { it.asSimbot(this) }.withLimiter(limiter)
    }

    override fun getFriends(): Stream<out MiraiFriend> {
        return nativeBot.friends.stream().map { it.asSimbot(this) }
    }


    override suspend fun groups(limiter: Limiter): Flow<MiraiGroup> {
        return nativeBot.groups.asFlow().map { it.asSimbot(this) }.withLimiter(limiter)
    }

    override fun getGroups(): Stream<out MiraiGroup> {
        return nativeBot.groups.stream().map { it.asSimbot(this) }.withLimiter(limiter())
    }

    override suspend fun uploadImage(resource: Resource, flash: Boolean): Image<*> {
        return when (resource) {
            is IDResource -> {
                val image = miraiImageFunc(resource.id.toString())
                if (flash) image.flash().asSimbot() else image.asSimbot()
            }
            is StreamableResource -> MiraiSendOnlyImageImpl(resource, flash)
        }
    }


    private val loginLock = Mutex()

    @Volatile
    private var eventRegistered = false

    override suspend fun start(): Boolean = loginLock.withLock {
        // 注册事件。
        if (!eventRegistered) {
            registerEvents()
            eventRegistered = true
        }
        nativeBot.login()
        true
    }

}

private val MiraiBotStatus = UserStatus.builder().bot().fakeUser().build()


private fun MiraiBotImpl.registerEvents() {

    nativeBot.eventChannel.subscribeAlways<NativeMiraiEvent> {
        when (this) {
            // friend event
            is NativeMiraiFriendMessageEvent ->
                doHandler(this, MiraiFriendMessageEvent) { MiraiFriendMessageEventImpl(this@registerEvents, this) }
            // stranger event
            is NativeMiraiStrangerMessageEvent ->
                doHandler(this, MiraiStrangerMessageEvent) { MiraiStrangerMessageEventImpl(this@registerEvents, this) }
            // group event
            is NativeMiraiGroupMessageEvent ->
                doHandler(this, MiraiGroupMessageEvent) { MiraiGroupMessageEventImpl(this@registerEvents, this) }
            // group temp event
            is GroupTempMessageEvent ->
                doHandler(this, MiraiMemberMessageEvent) { MiraiMemberMessageEventImpl(this@registerEvents, this) }

            // todo

            else -> {
                // else other.

                TODO()
            }
        }
        // this.intercept()
    }

    // listener

}

private suspend inline fun <reified E : NativeMiraiEvent, reified SE : MiraiSimbotEvent<E>>
        MiraiBotImpl.doHandler(
    event: E,
    key: Event.Key<SE>,
    noinline handler: suspend E.(bot: MiraiBotImpl) -> SE
): EventProcessingResult? {
    // return nativeBot.eventChannel.subscribeAlways { event ->
    return eventProcessor.pushIfProcessable(key) {
        event.handler(this)
    }
    // }
}