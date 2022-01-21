package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.NativeMiraiGroup
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiGroupImpl
import love.forte.simbot.component.mirai.internal.MiraiMemberImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.message.Message
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.toPlainText


/**
 */
internal class MiraiGroupMessageEventImpl(
    override val bot: MiraiBotImpl,
    private val nativeEvent: NativeMiraiGroupMessageEvent
) : MiraiGroupMessageEvent {
    override val metadata: MiraiGroupMessageEvent.Metadata = MetadataImpl(nativeEvent)
    override val messageContent: MiraiReceivedMessageContent = nativeEvent.toSimbotMessageContent()
    override val author: MiraiMemberImpl = nativeEvent.sender.asSimbot(bot)
    override val group: MiraiGroupImpl = nativeEvent.group.asSimbot(bot)
    override suspend fun delete(): Boolean {
        return try {
            messageContent.messageSource.recall()
            true
        } catch (illegalState: IllegalStateException) {
            false
        }
    }


    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<NativeMiraiGroup> {
        val miraiMessage = message.toNativeMiraiMessage(nativeEvent.group)
        val receipt = nativeEvent.group.sendMessage(QuoteReply(nativeEvent.source) + miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<NativeMiraiGroup> {
        val receipt = nativeEvent.group.sendMessage(QuoteReply(nativeEvent.source) + text.toPlainText())
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiGroup> {
        val miraiMessage = message.toNativeMiraiMessage(nativeEvent.group)
        val receipt = nativeEvent.group.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiGroup> {
        val receipt = nativeEvent.group.sendMessage(text)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }


    override val timestamp: Timestamp = Timestamp.bySecond(nativeEvent.time.toLong())

    private class MetadataImpl(nativeEvent: NativeMiraiGroupMessageEvent) :
        MiraiGroupMessageEvent.Metadata,
        BaseMiraiSimbotEventMetadata<NativeMiraiGroupMessageEvent>(nativeEvent)
}