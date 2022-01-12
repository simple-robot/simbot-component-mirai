package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiGroupImpl
import love.forte.simbot.component.mirai.internal.MiraiMemberImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import net.mamoe.mirai.message.data.MessageSource.Key.recall


/**
 */
internal class MiraiGroupMessageEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiGroupMessageEvent
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

    override val timestamp: Timestamp = Timestamp.bySecond(nativeEvent.time.toLong())

    private class MetadataImpl(nativeEvent: NativeMiraiGroupMessageEvent) :
        MiraiGroupMessageEvent.Metadata, BaseMiraiSimbotEventMetadata<NativeMiraiGroupMessageEvent>(nativeEvent)
}