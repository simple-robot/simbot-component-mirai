package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot


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

    private class MetadataImpl(nativeEvent: NativeMiraiGroupTempMessageEvent) :
        MiraiMemberMessageEvent.Metadata, BaseMiraiSimbotEventMetadata<NativeMiraiGroupTempMessageEvent>(nativeEvent)
}