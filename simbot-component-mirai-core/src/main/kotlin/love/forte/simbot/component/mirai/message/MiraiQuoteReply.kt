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

package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.buildMessageSource
import love.forte.simbot.message.Message
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.QuoteReply


/**
 *
 * 使用消息的ID作为Mirai的引用回复对象。
 * 如果你有一个现成的 [QuoteReply] 对象希望直接发送，
 * 请考虑使用 [SimbotOriginalMiraiMessage].
 *
 * [MiraiQuoteReply] 与 [SimbotOriginalMiraiMessage] 类似，不会使用函数 [originalMiraiMessage] 的参数 `contact`,
 * 你可以直接通过属性 [originalMiraiMessage] 获取 [QuoteReply] 实例。
 *
 * @author ForteScarlet
 */
@SerialName("mirai.quoteReply")
@Serializable
public class MiraiQuoteReply(
    private val source: MessageSource, // = id.buildMessageSource()
) : OriginalMiraiComputableSimbotMessage<MiraiQuoteReply> {
    public constructor(id: ID) : this(id.buildMessageSource())
    
    @Suppress("MemberVisibilityCanBePrivate")
    @Transient
    private val quoteReply = QuoteReply(source)
    
    override val key: Message.Key<MiraiQuoteReply>
        get() = Key
    
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MiraiQuoteReply) return false
        return other.source == source
    }
    
    override fun toString(): String = "MiraiQuoteReply(source=$source)"
    override fun hashCode(): Int = source.hashCode()
    
    @Suppress("MemberVisibilityCanBePrivate")
    public val originalMiraiMessage: QuoteReply
        get() = quoteReply
    
    /**
     * @see originalMiraiMessage
     */
    @JvmSynthetic
    override suspend fun originalMiraiMessage(contact: Contact, isDropAction: Boolean): QuoteReply = quoteReply
    
    public companion object Key : Message.Key<MiraiQuoteReply> {
        override fun safeCast(value: Any): MiraiQuoteReply? = doSafeCast(value)
    }
}