@file:Suppress("NOTHING_TO_INLINE")

package love.forte.simbot.component.mirai.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.CharSequenceID
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.event.MiraiMessageMetadata.Companion.of
import love.forte.simbot.component.mirai.message.asSimbotMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.Messages
import love.forte.simbot.message.ReceivedMessageContent
import love.forte.simbot.message.toMessages
import net.mamoe.mirai.message.data.*


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
     * 还有两个构建用属性 [botId][MessageSource.botId], [kind][MessageSource.kind],
     * 通过 `:` 拼接为字符ID。
     */
    override val id: CharSequenceID by lazy(LazyThreadSafetyMode.PUBLICATION) {
        with(source) {
            buildString {
                ids.joinTo(this, ARRAY_SEPARATOR)
                append(ELEMENT_SEPARATOR)
                internalIds.joinTo(this, ARRAY_SEPARATOR)
                append(ELEMENT_SEPARATOR).append(time)
                append(ELEMENT_SEPARATOR).append(botId)
                append(ELEMENT_SEPARATOR).append(kind)
            }
        }.ID
    }

    public companion object {
        private const val ARRAY_SEPARATOR = "."
        private const val ELEMENT_SEPARATOR = ":"

        /**
         * 根据ID解析为 [MiraiMessageMetadata].
         */
        @JvmStatic
        public fun of(id: ID): MiraiMessageMetadata {
            val elements = id.toString().split(ELEMENT_SEPARATOR)
            require(elements.size == 5) { "The number of elements in the ID must be 5, but ${elements.size}" }

            // ids
            val ids = elements[0].splitToSequence(ARRAY_SEPARATOR).map(String::toInt)

            // internal ids
            val internalIds = elements[1].splitToSequence(ARRAY_SEPARATOR).map(String::toInt)

            // time
            val time = elements[2].toInt()

            // botId
            val botId = elements[3].toLong()

            // kind
            val kind = MessageSourceKind.valueOf(elements[4])

            val source = MessageSourceBuilder().id(*ids.toList().toIntArray())
                .internalId(*internalIds.toList().toIntArray())
                .time(time).build(botId, kind)

            return miraiMessageMetadata(source)
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


