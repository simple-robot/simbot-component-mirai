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

@file:JvmName("MiraiMessageParserUtil")

package love.forte.simbot.component.mirai.message

import love.forte.simbot.*
import love.forte.simbot.component.mirai.internal.*
import love.forte.simbot.message.*
import love.forte.simbot.message.At
import love.forte.simbot.message.AtAll
import love.forte.simbot.message.Face
import love.forte.simbot.message.Message
import love.forte.simbot.message.PlainText
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.*
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
@OptIn(InternalApi::class)
public suspend fun Message.toNativeMiraiMessage(contact: Contact): NativeMiraiMessage {
    return when (this) {
        is MiraiNativeDirectlySimbotMessage<*> -> nativeMiraiMessage.takeIf { it !== EmptySingleMessage }
            ?: EmptyMessageChain
        is MiraiNativeComputableSimbotMessage<*> -> nativeMiraiMessage(contact).takeIf { it !== EmptySingleMessage }
            ?: EmptyMessageChain
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
    @JvmSynthetic
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
    @OptIn(InternalApi::class)
    @JvmSynthetic
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
            is MiraiNativeComputableSimbotMessage<*> -> message.nativeMiraiMessage(contact)
                .takeIf { it !== EmptySingleMessage }?.also(messages::add)
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
            is NativeMiraiFlashImage -> message.asSimbot()
            is NativeMiraiAudio -> message.asSimbot()
            is net.mamoe.mirai.message.data.Face -> Face(message.id.ID)

            // other messages.
            else -> SimbotNativeMiraiMessage(message)
        }
    }
}


private suspend fun love.forte.simbot.message.Image<*>.toMirai(contact: Contact): NativeMiraiMessage {
    val id = id.literal
    if (id.isNotEmpty()) {
        return Image(id)
    }

    val image: NativeMiraiImage = when (this) {
        is MiraiImage -> nativeImage
        is MiraiSendOnlyImage -> when (val ntImg = nativeMiraiMessage(contact)) {
            is NativeMiraiImage -> ntImg
            is NativeMiraiFlashImage -> ntImg.image
            else -> throw IllegalStateException("Can not resolve type of img content in MiraiSendOnlyImage")
        }
        else -> resource().use { r -> r.openStream().use { i -> contact.uploadImage(i) } }
    }


    return image
}


internal fun MessageChain.toSimbot(): Messages {
    return map { StandardParser.toSimbot(it) }.toMessages()
}
