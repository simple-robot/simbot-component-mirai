package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.event.BaseMiraiSimbotEventMetadata
import love.forte.simbot.component.mirai.event.MiraiFriendMessageEvent
import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.component.mirai.event.NativeMiraiFriendMessageEvent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.event.Event
import love.forte.simbot.message.ReceivedMessageContent
import net.mamoe.mirai.message.data.source
import java.time.Instant


internal class MiraiFriendMessageEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiFriendMessageEvent
) : MiraiFriendMessageEvent {
    override val key: Event.Key<MiraiFriendMessageEvent> get() = MiraiFriendMessageEvent
    override val metadata: MiraiFriendMessageEvent.Metadata = MetadataImpl(nativeEvent)
    override val timestamp: Timestamp = Timestamp.byInstant(Instant.ofEpochSecond(nativeEvent.time.toLong()))
    override val messageContent: ReceivedMessageContent =
        MiraiReceivedMessageContent(nativeEvent.message, nativeEvent.message.source)

    override val friend = nativeEvent.friend.asSimbot(bot)

    private class MetadataImpl(e: NativeMiraiFriendMessageEvent) :
        MiraiFriendMessageEvent.Metadata, BaseMiraiSimbotEventMetadata<NativeMiraiFriendMessageEvent>(e)
}