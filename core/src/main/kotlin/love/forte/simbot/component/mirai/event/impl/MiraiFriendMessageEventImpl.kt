package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.event.MiraiFriendMessageEvent
import love.forte.simbot.component.mirai.event.MiraiSimbotEvent
import love.forte.simbot.component.mirai.event.NativeMiraiFriendMessageEvent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.event.Event
import love.forte.simbot.message.ReceivedMessageContent
import java.time.Instant

/**
 * TODO
 */
internal class MiraiFriendMessageEventImpl(
    override val bot: MiraiBotImpl,
    private val nativeEvent: NativeMiraiFriendMessageEvent
) : MiraiFriendMessageEvent {
    override val key: Event.Key<MiraiFriendMessageEvent> get() = MiraiFriendMessageEvent
    override val timestamp: Timestamp = Timestamp.byInstant(Instant.ofEpochSecond(nativeEvent.time.toLong()))

    override val messageContent: ReceivedMessageContent
        get() = TODO("Not yet implemented")

    override suspend fun friend(): MiraiFriend {
        TODO("Not yet implemented")
    }

    override val metadata: MiraiSimbotEvent.Metadata<NativeMiraiFriendMessageEvent>
        get() = TODO("Not yet implemented")
}