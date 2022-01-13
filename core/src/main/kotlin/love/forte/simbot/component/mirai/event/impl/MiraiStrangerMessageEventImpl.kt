package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.action.MessageReplyReceipt
import love.forte.simbot.component.mirai.MiraiStranger
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageReceipt


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

    override suspend fun reply(message: Message): MessageReplyReceipt {
        val miraiMessage = message.toNativeMiraiMessage(nativeEvent.stranger)
        val receipt = nativeEvent.stranger.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceipt(receipt)
    }

    override suspend fun send(message: Message): MessageReceipt = reply(message)

    private class MetadataImpl(nativeEvent: NativeMiraiStrangerMessageEvent) :
        MiraiStrangerMessageEvent.Metadata, BaseMiraiSimbotEventMetadata<NativeMiraiStrangerMessageEvent>(nativeEvent)

}