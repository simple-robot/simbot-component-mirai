/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU Affero通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
 *
 *
 */

package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import love.forte.simbot.ID
import love.forte.simbot.SimbotIllegalArgumentException
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.component.mirai.event.MiraiReceivedNudgeMessageContent
import love.forte.simbot.component.mirai.message.MiraiQuoteReply.Key
import love.forte.simbot.component.mirai.tryBuildMessageSource
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.SingleMessageReceipt
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.QuoteReply


/**
 *
 * 使用 [QuoteReply] 作为Mirai的引用回复对象。
 * 如果你有一个现成的 [QuoteReply] 对象希望直接发送，
 * 请考虑使用 [SimbotOriginalMiraiMessage].
 *
 * [MiraiQuoteReply] 与 [SimbotOriginalMiraiMessage] 类似，不会使用函数 [originalMiraiMessage] 的参数 `contact`,
 * 你可以直接通过属性 [originalMiraiMessage] 获取 [QuoteReply] 实例。
 *
 * @see QuoteReply
 *
 * @author ForteScarlet
 * @constructor 直接使用 [MessageSource] 构建一个 [MiraiQuoteReply].
 * _如果希望使用更多类型构建 [MiraiQuoteReply], 参考[伴生对象][Key]中更多的工厂方法。_
 */
@SerialName("mirai.quoteReply")
@Serializable
public class MiraiQuoteReply(
    private val source: MessageSource, // = id.buildMessageSource()
) : OriginalMiraiDirectlySimbotMessage<QuoteReply, MiraiQuoteReply> {

    /**
     * 通过消息回执的ID [SingleMessageReceipt.id][love.forte.simbot.message.SingleMessageReceipt.id] (或者更推荐为 [SimbotMiraiMessageReceipt.fullId]) 构建一个引用回复。
     * 对ID的判断基于是否由 `{` 和 `}` 进行包裹。
     *
     * ***注：如果你使用了 [SimbotMiraiMessageReceipt.id] 而不是 [SimbotMiraiMessageReceipt.fullId], 则此引用回复会丢失部分信息，可能会导致预期外的效果。***
     *
     * _如果希望使用更多类型构建 [MiraiQuoteReply], 参考[伴生对象][Key]中更多的工厂方法。_
     */
    public constructor(id: ID) : this(id.tryBuildMessageSource())

    /**
     * 直接使用一个原生的 [QuoteReply] 对象构建 [MiraiQuoteReply]. [originalMiraiMessage] 将会直接使用此 [quoteReply].
     *
     * _如果希望使用更多类型构建 [MiraiQuoteReply], 参考[伴生对象][Key]中更多的工厂方法。_
     */
    public constructor(quoteReply: QuoteReply) : this(quoteReply.source) {
        this._quoteReply = quoteReply
    }

    @Transient
    private var _quoteReply: QuoteReply? = null

    @Suppress("MemberVisibilityCanBePrivate")
    private val quoteReply: QuoteReply
        get() = _quoteReply ?: synchronized(this) {
            _quoteReply ?: QuoteReply(source).also {
                _quoteReply = it
            }
        }

    override val key: Message.Key<MiraiQuoteReply>
        get() = Key

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MiraiQuoteReply) return false
        return other.quoteReply == quoteReply
    }

    override fun toString(): String = "MiraiQuoteReply(quoteReply=$quoteReply)"
    override fun hashCode(): Int = quoteReply.hashCode()

    /**
     * 当前消息对象中将被实际使用的 [QuoteReply].
     */
    override val originalMiraiMessage: QuoteReply
        get() = quoteReply


    public companion object Key : Message.Key<MiraiQuoteReply> {
        override fun safeCast(value: Any): MiraiQuoteReply? = doSafeCast(value)

        /**
         * 尝试通过 [SingleMessageReceipt] 中的  构造 [MiraiQuoteReply].
         *
         * 如果 [messageReceipt] 为 [SimbotMiraiMessageReceipt] 类型，则会直接使用 [SimbotMiraiMessageReceipt.receipt] 中的可用属性直接构造，不会发生序列化过程。
         * 如果是其他类型，则会尝试通过 [SingleMessageReceipt.id] 进行构造。但是当为这种可能性时，构造的成功率会很低——基本不会有可支持的其他类型，因此大概率会引发异常。
         *
         * @see SimbotMiraiMessageReceipt
         */
        @JvmStatic
        public fun create(messageReceipt: SingleMessageReceipt): MiraiQuoteReply {
            if (messageReceipt is SimbotMiraiMessageReceipt<*>) {
                return MiraiQuoteReply(messageReceipt.receipt.source)
            }

            return MiraiQuoteReply(messageReceipt.id)
        }

        /**
         * 尝试通过 [MessageContent] 构造 [MiraiQuoteReply].
         *
         * 当 [MessageContent] 为 [MiraiReceivedMessageContent]、[MiraiMessageChainContent] 且其中存在 `messageSource` 时可以构建为 [MiraiQuoteReply].
         * ([MiraiReceivedNudgeMessageContent] 也无法被引用回复，同样会引发异常)
         *
         * @throws IllegalArgumentException 当 [messageContent] 不可获取 [MessageSource] 时
         */
        @JvmStatic
        public fun create(messageContent: MessageContent): MiraiQuoteReply {
            val source = when (messageContent) {
                is MiraiMessageContent -> when (messageContent) {
                    is MiraiReceivedMessageContent -> messageContent.messageSourceOrNull
                    is MiraiMessageChainContent -> messageContent.messageSourceOrNull
                    is MiraiReceivedNudgeMessageContent -> throw SimbotIllegalArgumentException("Nudge message can not be reply")
                    // try to use the full message id
                    else -> return MiraiQuoteReply(messageContent.fullMessageId)
                }
                // unknown
                else -> throw SimbotIllegalArgumentException("The type of messageContent is not a mirai-related type")
            } ?: throw SimbotIllegalArgumentException("There is no MessageSource in messageContent")

            return MiraiQuoteReply(source)
        }

    }
}

/**
 * 将 [QuoteReply] 包装为 [MiraiQuoteReply]
 */
public fun QuoteReply.asSimbot(): MiraiQuoteReply = MiraiQuoteReply(this)

/**
 * 通过当前 [SimbotMiraiMessageReceipt] 构造 [MiraiQuoteReply]。
 */
public fun SimbotMiraiMessageReceipt<*>.quoteReply(): MiraiQuoteReply = MiraiQuoteReply.create(this)
