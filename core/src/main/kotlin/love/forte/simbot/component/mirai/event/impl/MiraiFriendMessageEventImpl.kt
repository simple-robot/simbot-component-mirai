package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.action.MessageReplyReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageReceipt
import java.time.Instant


internal class MiraiFriendMessageEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiFriendMessageEvent
) : MiraiFriendMessageEvent {
    override val metadata: MiraiFriendMessageEvent.Metadata = MetadataImpl(nativeEvent)
    override val timestamp: Timestamp = Timestamp.byInstant(Instant.ofEpochSecond(nativeEvent.time.toLong()))
    override val messageContent: MiraiReceivedMessageContent = nativeEvent.toSimbotMessageContent()

    override val friend = nativeEvent.friend.asSimbot(bot)

    override suspend fun reply(message: Message): MessageReplyReceipt {
        val miraiMessage = message.toNativeMiraiMessage(nativeEvent.friend)
        val receipt = nativeEvent.friend.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceipt(receipt)
    }

    override suspend fun send(message: Message): MessageReceipt = reply(message)



    private class MetadataImpl(e: NativeMiraiFriendMessageEvent) :
        MiraiFriendMessageEvent.Metadata, BaseMiraiSimbotEventMetadata<NativeMiraiFriendMessageEvent>(e)
}