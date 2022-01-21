package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiStranger
import love.forte.simbot.component.mirai.NativeMiraiStranger
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.message.Message


/**
 *
 * @author ForteScarlet
 */
internal class MiraiStrangerMessageEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiStrangerMessageEvent
) : MiraiStrangerMessageEvent {

    override val timestamp: Timestamp = Timestamp.Companion.bySecond(nativeEvent.time.toLong())
    override val metadata: MiraiStrangerMessageEvent.Metadata = MetadataImpl(nativeEvent)
    override val user: MiraiStranger = nativeEvent.stranger.asSimbot(bot)
    override val messageContent: MiraiReceivedMessageContent = nativeEvent.toSimbotMessageContent()

    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<NativeMiraiStranger> {
        val miraiMessage = message.toNativeMiraiMessage(nativeEvent.stranger)
        val receipt = nativeEvent.stranger.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<NativeMiraiStranger> {
        return SimbotMiraiMessageReceiptImpl(nativeEvent.stranger.sendMessage(text))
    }

    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiStranger> = reply(message)
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiStranger> = reply(text)

    private class MetadataImpl(nativeEvent: NativeMiraiStrangerMessageEvent) :
        MiraiStrangerMessageEvent.Metadata, BaseMiraiSimbotEventMetadata<NativeMiraiStrangerMessageEvent>(nativeEvent)

}