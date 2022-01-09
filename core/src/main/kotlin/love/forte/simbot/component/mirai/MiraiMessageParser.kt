@file:JvmName("MiraiMessageParserUtil")

package love.forte.simbot.component.mirai

import love.forte.simbot.message.At
import love.forte.simbot.message.AtAll
import love.forte.simbot.message.Message
import love.forte.simbot.message.Messages
import love.forte.simbot.tryToLongID
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toMessageChain
import java.util.concurrent.ConcurrentSkipListSet


/**
 * Mirai中的原生消息类型 [net.mamoe.mirai.message.data.Message].
 *
 * @see net.mamoe.mirai.message.data.Message
 *
 */
public typealias NativeMiraiMessage = net.mamoe.mirai.message.data.Message


/**
 * 将一个mirai原生的 [NativeMiraiMessage] 转化为Simbot（simbot-mirai组件下）的 [Message].
 */
public fun NativeMiraiMessage.toSimbotMessage(): Message = SimbotNativeMiraiMessage(this)

/**
 *
 * 提供一个 [NativeMiraiMessage] 的计算函数并作为 [Message] 使用。
 *
 * **注意：通过此方式得到的 [Message] 不可参与序列化。 **
 *
 * @see SimbotSendOnlyComputableMiraiMessage
 */
public fun simbotMessage(factory: (Contact) -> NativeMiraiMessage): Message =
    SimbotSendOnlyComputableMiraiMessage(factory)


/**
 * 将一个 [Message] 转化为 [NativeMiraiMessage] 以发送。
 */
public suspend fun Message.toNativeMiraiMessage(contact: Contact): NativeMiraiMessage {
    return when (this) {
        is SimbotNativeMiraiMessage -> nativeMiraiMessage
        is SimbotSendOnlyComputableMiraiMessage -> nativeMiraiMessage(contact)
        else -> {
            val list = mutableListOf<NativeMiraiMessage>()
            parsers.forEach { parser ->
                if (this is Message.Element<*>) {
                    parser(this, contact, list)
                } else if (this is Messages) {
                    this.forEach {
                        parser(it, contact, list)
                    }
                }
            }
            if (list.isEmpty()) EmptyMessageChain else list.toMessageChain()
        }
    }
}


// internal suspend fun Message.doParse(contact: Contact): NativeMiraiMessage {
//
// }


internal interface MiraiMessageParser {
    suspend operator fun invoke(
        message: Message.Element<*>,
        contact: Contact,
        messages: MutableCollection<NativeMiraiMessage>
    )
}

private val parsers = ConcurrentSkipListSet<MiraiMessageParser>().apply {
    add(AtParser)
}

/**
 * 将 [At] 转化为 [NativeMiraiMessage] 的转化器.
 */
private object AtParser : MiraiMessageParser {
    override suspend fun invoke(
        message: Message.Element<*>,
        contact: Contact,
        messages: MutableCollection<NativeMiraiMessage>
    ) {
        if (message is At) {
            val id = message.target.tryToLongID().number
            if (contact is NativeMiraiGroup) {
                // val member = contact.getMemberOrFail(id)
                messages.add(net.mamoe.mirai.message.data.At(id))
            } else {
                // 不是在一个群里, 例如私聊
                // 尝试得知此At对应人。
                if (id == contact.id && contact is UserOrBot) {
                    messages.add(net.mamoe.mirai.message.data.At(contact))
                } else {
                    messages.add(PlainText(message.originContent))
                }
            }
        } else if (message == AtAll) {
            messages.add(net.mamoe.mirai.message.data.AtAll)
        }
    }
}

