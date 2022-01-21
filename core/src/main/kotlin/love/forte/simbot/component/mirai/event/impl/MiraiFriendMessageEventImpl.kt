package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.NativeMiraiFriend
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.message.Message
import java.time.Instant


internal class MiraiFriendMessageEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiFriendMessageEvent
) : MiraiFriendMessageEvent {
    override val metadata: MiraiFriendMessageEvent.Metadata = MetadataImpl(nativeEvent)
    override val timestamp: Timestamp = Timestamp.byInstant(Instant.ofEpochSecond(nativeEvent.time.toLong()))
    override val messageContent: MiraiReceivedMessageContent = nativeEvent.toSimbotMessageContent()

    override val friend = nativeEvent.friend.asSimbot(bot)

    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend> {
        val miraiMessage = message.toNativeMiraiMessage(nativeEvent.friend)
        val receipt = nativeEvent.friend.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend> {
        val receipt = nativeEvent.friend.sendMessage(text)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend> = reply(message)
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend> = reply(text)


    private class MetadataImpl(e: NativeMiraiFriendMessageEvent) :
        MiraiFriendMessageEvent.Metadata, BaseMiraiSimbotEventMetadata<NativeMiraiFriendMessageEvent>(e)
}