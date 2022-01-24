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

package love.forte.simbot.component.mirai

import love.forte.simbot.Api4J
import love.forte.simbot.CharSequenceID
import love.forte.simbot.ID
import love.forte.simbot.action.DeleteSupport
import love.forte.simbot.action.MessageReplyReceipt
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.MessageReceipt
import love.forte.simbot.utils.runInBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*


/**
 * @see net.mamoe.mirai.message.MessageReceipt
 */
public typealias NativeMiraiMessageReceipt<C> = net.mamoe.mirai.message.MessageReceipt<C>


/**
 * Mirai组件中，封装使用 [NativeMiraiMessageReceipt] 作为消息发送的回执对象。
 *
 *
 */
public interface SimbotMiraiMessageReceipt<C : Contact> : MessageReceipt, MessageReplyReceipt, DeleteSupport,
    ReplySupport {
    public val receipt: NativeMiraiMessageReceipt<C>

    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<Contact>
    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<Contact>
    @JvmSynthetic
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<Contact>
    @JvmSynthetic
    override suspend fun delete(): Boolean

    //// Impl

    @Api4J
    override fun deleteBlocking(): Boolean = runInBlocking { delete() }

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<Contact> = runInBlocking { reply(text) }

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<Contact> = runInBlocking { reply(message) }

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<Contact> =
        runInBlocking { reply(message) }


}


/**
 *
 * @see DeleteSupport
 * @see ReplySupport
 * @author ForteScarlet
 */
internal class SimbotMiraiMessageReceiptImpl<C : Contact>(
    override val receipt: NativeMiraiMessageReceipt<C>
) : SimbotMiraiMessageReceipt<C> {
    override val id: ID = receipt.source.ID
    override val isSuccess: Boolean get() = true
    override val isReplySuccess: Boolean get() = true

    /**
     * 删除/撤回这条消息.
     */
    @JvmSynthetic
    override suspend fun delete(): Boolean {
        receipt.recall()
        return true
    }

    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<Contact> {
        val quote = receipt.quote()
        val sendMessage = message.toNativeMiraiMessage(receipt.target)
        val newReceipt = receipt.target.sendMessage(quote + sendMessage)
        return SimbotMiraiMessageReceiptImpl(newReceipt)
    }

    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<Contact> {
        val quote = receipt.quote()
        val newReceipt = receipt.target.sendMessage(quote + text.toPlainText())
        return SimbotMiraiMessageReceiptImpl(newReceipt)
    }

    @JvmSynthetic
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<Contact> {
        val quote = receipt.quote()
        val sendMessage = message.messages.toNativeMiraiMessage(receipt.target)
        val newReceipt = receipt.target.sendMessage(quote + sendMessage)
        return SimbotMiraiMessageReceiptImpl(newReceipt)
    }
}


public object MessageSourceIDConstant {
    public const val ARRAY_SEPARATOR: String = "."
    public const val ELEMENT_SEPARATOR: String = ":"
}

/*
    三个定位属性 ids, internalId, time,
    然后botId和一个消息源类型
 */

public val MessageSource.ID: CharSequenceID
    get() {
        return buildString {
            ids.joinTo(this, MessageSourceIDConstant.ARRAY_SEPARATOR)
            append(MessageSourceIDConstant.ELEMENT_SEPARATOR)
            internalIds.joinTo(this, MessageSourceIDConstant.ARRAY_SEPARATOR)
            append(MessageSourceIDConstant.ELEMENT_SEPARATOR).append(time)
            append(MessageSourceIDConstant.ELEMENT_SEPARATOR).append(botId)
            append(MessageSourceIDConstant.ELEMENT_SEPARATOR).append(kind.ordinal)
        }.ID
    }


public inline fun ID.buildMessageSource(andThen: MessageSourceBuilder.() -> Unit = {}): MessageSource {
    val value = toString()
    val elements = value.split(MessageSourceIDConstant.ELEMENT_SEPARATOR)
    require(elements.size == 5) { "The number of elements in the ID must be 5, but ${elements.size}" }

    // ids
    val ids = elements[0].splitToSequence(MessageSourceIDConstant.ARRAY_SEPARATOR).map(String::toInt)

    // internal ids
    val internalIds = elements[1].splitToSequence(MessageSourceIDConstant.ARRAY_SEPARATOR).map(String::toInt)

    // time
    val time = elements[2].toInt()

    // botId
    val botId = elements[3].toLong()

    // kind
    val kind = MessageSourceKind.values()[(elements[4].toInt())]

    return MessageSourceBuilder().id(*ids.toList().toIntArray())
        .internalId(*internalIds.toList().toIntArray())
        .time(time).apply(andThen).build(botId, kind)

}