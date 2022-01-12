package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiStranger
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot


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

    private class MetadataImpl(nativeEvent: NativeMiraiStrangerMessageEvent) :
        MiraiStrangerMessageEvent.Metadata, BaseMiraiSimbotEventMetadata<NativeMiraiStrangerMessageEvent>(nativeEvent)

}