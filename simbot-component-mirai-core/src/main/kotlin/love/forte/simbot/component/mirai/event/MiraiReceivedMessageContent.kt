/*
 *  Copyright (c) 2022-2022 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU 通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU 通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU 通用公共许可证的复本。如果没有，请看:
 *  https://www.gnu.org/licenses
 *  https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *  https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 *
 *
 */

@file:Suppress("NOTHING_TO_INLINE")

package love.forte.simbot.component.mirai.event

import kotlinx.serialization.*
import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.event.MiraiMessageMetadata.Companion.of
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.message.*
import love.forte.simbot.message.Message
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
        // 消息链中不追加source. 如果需要, 使用 [nativeMessageChain] 或者 [messageSource]
        nativeMessageChain.filter { it !is MessageSource }.map(SingleMessage::asSimbotMessage)::toMessages
    )

    override val messageId: ID by lazy(LazyThreadSafetyMode.PUBLICATION) { messageSource.ID }
    // public val metadata: MiraiMessageMetadata = miraiMessageMetadata(messageSource)

    override fun toString(): String = "MiraiReceivedMessageContent(content=$nativeMessageChain)"
}

internal fun MessageChain.toSimbotMessageContent(): MiraiReceivedMessageContent =
    MiraiReceivedMessageContent(this, this.source)

internal fun NativeMiraiMessageEvent.toSimbotMessageContent(): MiraiReceivedMessageContent =
    this.message.toSimbotMessageContent()

/**
 * 基于mirai的 [MessageSource] 的 [Message.Metadata] 实现。
 *
 * @see of
 */
@SerialName("mirai.message.metadata")
@Serializable
public abstract class MiraiMessageMetadata {

    /**
     * mirai的原生对象 [MessageSource].
     */
    public abstract val source: MessageSource

    // /**
    //  * 三个定位属性 [ids][MessageSource.ids], [internalIds][MessageSource.internalIds], [time][MessageSource.time],
    //  * 还有两个构建用属性 [botId][MessageSource.botId], [kind][net.mamoe.mirai.message.data.MessageSourceKind],
    //  * 通过 `:` 拼接为字符ID。
    //  */
    // public val id: CharSequenceID by lazy(LazyThreadSafetyMode.PUBLICATION) {
    //     source.ID
    // }

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


