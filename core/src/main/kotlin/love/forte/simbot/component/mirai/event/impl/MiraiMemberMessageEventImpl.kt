package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.action.MessageReplyReceipt
import love.forte.simbot.component.mirai.MiraiMember
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
internal class MiraiMemberMessageEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiGroupTempMessageEvent
) : MiraiMemberMessageEvent {
    override val timestamp: Timestamp = Timestamp.bySecond(nativeEvent.time.toLong())
    override val user: MiraiMember = nativeEvent.sender.asSimbot(bot)
    override val metadata: MiraiMemberMessageEvent.Metadata = MetadataImpl(nativeEvent)
    override val messageContent: MiraiReceivedMessageContent = nativeEvent.toSimbotMessageContent()


    override suspend fun reply(message: Message): MessageReplyReceipt {
        val miraiMessage = message.toNativeMiraiMessage(nativeEvent.sender)
        val receipt = nativeEvent.sender.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceipt(receipt)
    }

    override suspend fun send(message: Message): MessageReceipt = reply(message)


    private class MetadataImpl(nativeEvent: NativeMiraiGroupTempMessageEvent) :
        MiraiMemberMessageEvent.Metadata, BaseMiraiSimbotEventMetadata<NativeMiraiGroupTempMessageEvent>(nativeEvent)
}