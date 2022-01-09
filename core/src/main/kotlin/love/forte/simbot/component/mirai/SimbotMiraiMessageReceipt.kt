package love.forte.simbot.component.mirai

import love.forte.simbot.ID
import love.forte.simbot.action.DeleteSupport
import love.forte.simbot.action.MessageReplyReceipt
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageReceipt
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageSource


public typealias NativeMiraiMessageReceipt<C> = net.mamoe.mirai.message.MessageReceipt<C>

/**
 *
 * @see DeleteSupport
 * @see ReplySupport
 * @author ForteScarlet
 */
internal class SimbotMiraiMessageReceipt<C : Contact>(
    private val receipt: NativeMiraiMessageReceipt<C>
) : MessageReceipt, MessageReplyReceipt, DeleteSupport, ReplySupport {
    override val id: ID = receipt.source.ID
    override val isSuccess: Boolean get() = true
    override val isReplySuccess: Boolean get() = true

    /**
     * 删除/撤回这条消息.
     */
    override suspend fun delete(): Boolean {
        receipt.recall()
        return true
    }

    /**
     *
     */
    override suspend fun reply(message: Message): MessageReplyReceipt {
        val quote = receipt.quote()
        val sendMessage = message.toNativeMiraiMessage(receipt.target)
        val newReceipt = receipt.target.sendMessage(quote + sendMessage)
        return SimbotMiraiMessageReceipt(newReceipt)
    }
}


/*
三个定位属性 ids, internalId, time
 */
public val MessageSource.ID: ID
    get() {
        val idsValue = ids.joinToString(",")
        val internalIdValue = internalIds.joinToString(",")
        val timeValue = time
        return "$idsValue:$internalIdValue:$timeValue".ID
    }