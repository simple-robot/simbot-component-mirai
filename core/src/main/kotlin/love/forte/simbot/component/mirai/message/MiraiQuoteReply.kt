package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import love.forte.simbot.Component
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.ComponentMirai
import love.forte.simbot.component.mirai.buildMessageSource
import love.forte.simbot.message.Message
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.QuoteReply
import kotlin.reflect.KClass


/**
 *
 * 使用消息的ID作为Mirai的引用回复对象。
 * 如果你有一个现成的 [QuoteReply] 对象希望直接发送，
 * 请考虑使用 [SimbotNativeMiraiMessage].
 *
 * [MiraiQuoteReply] 与 [SimbotNativeMiraiMessage] 类似，不会使用函数 [nativeMiraiMessage] 的参数 `contact`,
 * 你可以直接通过属性 [nativeMiraiMessage] 获取 [QuoteReply] 实例。
 *
 * @author ForteScarlet
 */
@SerialName("mirai.quoteReply")
@Serializable
public class MiraiQuoteReply(
    private val source: MessageSource // = id.buildMessageSource()
) : MiraiNativeComputableSimbotMessage<MiraiQuoteReply> {
    public constructor(id: ID) : this(id.buildMessageSource())

    @Suppress("MemberVisibilityCanBePrivate")
    @Transient
    private val quoteReply = QuoteReply(source)

    override val key: Message.Key<MiraiQuoteReply>
        get() = Key

    override fun equals(other: Any?): Boolean {
        if (other !is MiraiQuoteReply) return false
        return other.source == source
    }

    override fun toString(): String = "MiraiQuoteReply(source=$source)"
    override fun hashCode(): Int = source.hashCode()

    @Suppress("MemberVisibilityCanBePrivate")
    public val nativeMiraiMessage: QuoteReply
        get() = quoteReply

    /**
     * @see nativeMiraiMessage
     */
    override suspend fun nativeMiraiMessage(contact: Contact): QuoteReply = quoteReply

    public companion object Key : Message.Key<MiraiQuoteReply> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<MiraiQuoteReply>
            get() = MiraiQuoteReply::class
    }
}