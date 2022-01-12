package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import java.time.Instant


internal class MiraiFriendMessageEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiFriendMessageEvent
) : MiraiFriendMessageEvent {
    override val metadata: MiraiFriendMessageEvent.Metadata = MetadataImpl(nativeEvent)
    override val timestamp: Timestamp = Timestamp.byInstant(Instant.ofEpochSecond(nativeEvent.time.toLong()))
    override val messageContent: MiraiReceivedMessageContent = nativeEvent.toSimbotMessageContent()

    override val friend = nativeEvent.friend.asSimbot(bot)

    private class MetadataImpl(e: NativeMiraiFriendMessageEvent) :
        MiraiFriendMessageEvent.Metadata, BaseMiraiSimbotEventMetadata<NativeMiraiFriendMessageEvent>(e)
}