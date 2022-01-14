@file:JvmName("MiraiMessageParserUtil")

package love.forte.simbot.component.mirai.message

import love.forte.simbot.ID
import love.forte.simbot.component.mirai.internal.InternalApi
import love.forte.simbot.message.*
import love.forte.simbot.message.At
import love.forte.simbot.message.AtAll
import love.forte.simbot.message.Face
import love.forte.simbot.message.Message
import love.forte.simbot.message.PlainText
import love.forte.simbot.resources.IDResource
import love.forte.simbot.resources.StreamableResource
import love.forte.simbot.tryToLongID
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.At as MiraiAtFunc


/**
 * Mirai中的原生消息类型 [net.mamoe.mirai.message.data.Message].
 *
 * @see net.mamoe.mirai.message.data.Message
 *
 */
public typealias NativeMiraiMessage = net.mamoe.mirai.message.data.Message


/**
 * Mirai中的原生消息类型 [net.mamoe.mirai.message.data.SingleMessage].
 *
 * @see net.mamoe.mirai.message.data.SingleMessage
 *
 */
public typealias NativeMiraiSingleMessage = SingleMessage


@InternalApi
public object EmptySingleMessage : NativeMiraiSingleMessage {
    override fun contentToString(): String = "EmptySingleMessage"
    override fun toString(): String = "EmptySingleMessage()"

}


/**
 * 将一个mirai原生的 [NativeMiraiMessage] 转化为Simbot（simbot-mirai组件下）的 [Message].
 */
public fun NativeMiraiSingleMessage.asSimbotMessage(): Message.Element<*> =
    StandardParser.toSimbot(this)

/**
 *
 * 提供一个 [NativeMiraiMessage] 的计算函数并作为 [Message] 使用。
 *
 * **注意：通过此方式得到的 [Message] 不可参与序列化。 **
 *
 * @see SimpleMiraiSendOnlyComputableSimbotMessage
 */
public fun simbotMessage(factory: (Contact) -> NativeMiraiMessage): Message =
    SimpleMiraiSendOnlyComputableSimbotMessage(factory)


/**
 * 将一个 [Message] 转化为 [NativeMiraiMessage] 以发送。
 */
public suspend fun Message.toNativeMiraiMessage(contact: Contact): NativeMiraiMessage {
    return when (this) {
        is MiraiNativeDirectlySimbotMessage<*> -> nativeMiraiMessage
        is MiraiNativeComputableSimbotMessage<*> -> nativeMiraiMessage(contact)
        else -> {
            val list = mutableListOf<NativeMiraiMessage>()

            if (this is Message.Element<*>) {
                StandardParser.toMirai(this, contact, list)
            } else if (this is Messages) {
                this.forEach {
                    StandardParser.toMirai(it, contact, list)
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
    suspend fun toMirai(
        message: Message.Element<*>,
        contact: Contact,
        messages: MutableCollection<NativeMiraiMessage>
    )

    fun toSimbot(
        message: SingleMessage,
    ): Message.Element<*>
}

// private val parsers = ConcurrentSkipListSet<MiraiMessageParser>().apply {
//     add(StandardParser)
// }


private object StandardParser : MiraiMessageParser {
    override suspend fun toMirai(
        message: Message.Element<*>,
        contact: Contact,
        messages: MutableCollection<NativeMiraiMessage>
    ) {
        when (message) {
            is StandardMessage<*> -> when (message) {
                is BaseStandardMessage<*> -> when (message) {
                    is At -> messages.add(MiraiAtFunc(message.target.tryToLongID().number))
                    is AtAll -> messages.add(net.mamoe.mirai.message.data.AtAll)
                    is Text -> messages.add(message.text.toPlainText())
                }
                is PlainText -> messages.add(message.text.toPlainText())
                is love.forte.simbot.message.Image -> messages.add(message.toMirai(contact))
                is Emoji -> messages.add(":${message.id}:".toPlainText())
                is Face -> {
                    val miraiFace = net.mamoe.mirai.message.data.Face(message.id.tryToLongID().toInt())
                    messages.add(miraiFace)
                }
                is RemoteResource -> {
                    // not support?
                }
            }
            is MiraiNativeComputableSimbotMessage<*> -> messages.add(message.nativeMiraiMessage(contact))
        }
    }

    /**
     * mirai message 转化为 simbot message
     */
    override fun toSimbot(message: SingleMessage): Message.Element<*> {
        return when (message) {
            is net.mamoe.mirai.message.data.At -> At(message.target.ID)
            is net.mamoe.mirai.message.data.AtAll -> AtAll
            is net.mamoe.mirai.message.data.PlainText -> Text { message.content }
            is NativeMiraiImage -> message.asSimbot()
            is NativeMiraiAudio -> message.asSimbot()

            // other messages.
            else -> SimbotNativeMiraiMessage(message)
        }
    }
}


private suspend fun love.forte.simbot.message.Image<*>.toMirai(contact: Contact): NativeMiraiMessage {
    val id = id.toString()
    if (id.isNotEmpty()) {
        return Image(id)
    }

    val image: Image = when (val resource = resource()) {
        is IDResource -> Image(resource.id.toString())
        is StreamableResource -> {
            resource.use { r ->
                r.openStream().use { i -> contact.uploadImage(i) }
            }
        }
    }
    return image
}


internal fun MessageChain.toSimbot(): Messages {
    return map { StandardParser.toSimbot(it) }.toMessages()
}
