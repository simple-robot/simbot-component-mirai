@file:Suppress("NOTHING_TO_INLINE")

package love.forte.simbot.component.mirai.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.CharSequenceID
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.ID
import love.forte.simbot.component.mirai.buildMessageSource
import love.forte.simbot.component.mirai.event.MiraiMessageMetadata.Companion.of
import love.forte.simbot.component.mirai.message.asSimbotMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.Messages
import love.forte.simbot.message.ReceivedMessageContent
import love.forte.simbot.message.toMessages
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.source


/**
 * 通过 [MessageChain] 将Mirai的消息链解析为simbot的 [ReceivedMessageContent].
 */
@Suppress("MemberVisibilityCanBePrivate")
public open class MiraiReceivedMessageContent internal constructor(
    @Suppress("CanBeParameter")
    public val nativeMessageChain: MessageChain,
    public val messageSource: MessageSource
) : ReceivedMessageContent() {

    override val messages: Messages by lazy(
        LazyThreadSafetyMode.PUBLICATION,
        nativeMessageChain.map(SingleMessage::asSimbotMessage)::toMessages
    )

    override val metadata: MiraiMessageMetadata = miraiMessageMetadata(messageSource)
}

internal fun MessageChain.toSimbotMessageContent(): MiraiReceivedMessageContent =
    MiraiReceivedMessageContent(this, this.source)

internal fun NativeMiraiMessageEvent.toSimbotMessageContent(): MiraiReceivedMessageContent = this.message.toSimbotMessageContent()

/**
 * 基于mirai的 [MessageSource] 的 [Message.Metadata] 实现。
 *
 * @see of
 */
@SerialName("mirai.message.metadata")
@Serializable
public abstract class MiraiMessageMetadata : Message.Metadata() {

    /**
     * mirai的原生对象 [MessageSource].
     */
    public abstract val source: MessageSource

    /**
     * 三个定位属性 [ids][MessageSource.ids], [internalIds][MessageSource.internalIds], [time][MessageSource.time],
     * 还有两个构建用属性 [botId][MessageSource.botId], [kind][net.mamoe.mirai.message.data.MessageSourceKind],
     * 通过 `:` 拼接为字符ID。
     */
    override val id: CharSequenceID by lazy(LazyThreadSafetyMode.PUBLICATION) {
        source.ID
    }

    public companion object {

        /**
         * 根据ID解析为 [MiraiMessageMetadata].
         */
        @JvmStatic
        public fun of(id: ID): MiraiMessageMetadata {
            return miraiMessageMetadata(id.buildMessageSource())
        }


        /**
         * 直接根据一个 [MessageSource] 构建 [MiraiMessageMetadata].
         */
        @JvmStatic
        public fun of(source: MessageSource): MiraiMessageMetadata = MiraiMessageMetadataImpl(source)
    }
}


public inline fun miraiMessageMetadata(source: MessageSource): MiraiMessageMetadata = of(source)
public inline fun miraiMessageMetadata(id: ID): MiraiMessageMetadata = of(id)


@Suppress("MemberVisibilityCanBePrivate")
@SerialName("mirai.message.metadata")
@Serializable
private class MiraiMessageMetadataImpl(override val source: MessageSource) : MiraiMessageMetadata()


