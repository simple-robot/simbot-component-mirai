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
 */

@file:JvmName("MiraiMessageParserUtil")

package love.forte.simbot.component.mirai.message

import love.forte.simbot.ID
import love.forte.simbot.component.mirai.internal.InternalApi
import love.forte.simbot.component.mirai.message.MiraiAudio.Key.asSimbot
import love.forte.simbot.literal
import love.forte.simbot.message.*
import love.forte.simbot.message.At
import love.forte.simbot.message.Message
import love.forte.simbot.tryToLongID
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.message.data.At as MiraiAtFunc
import net.mamoe.mirai.message.data.Audio as OriginalMiraiAudio
import net.mamoe.mirai.message.data.FlashImage as OriginalMiraiFlashImage
import net.mamoe.mirai.message.data.Image as OriginalMiraiImage
import net.mamoe.mirai.message.data.Message as OriginalMiraiMessage
import net.mamoe.mirai.message.data.SingleMessage as OriginalMiraiSingleMessage


@InternalApi
public object EmptySingleMessage : OriginalMiraiSingleMessage {
    override fun contentToString(): String = "EmptySingleMessage"
    override fun toString(): String = "EmptySingleMessage()"

    public val simbotMessage: Message.Element<*> = this.asSimbotMessage()
}


/**
 * 将一个mirai原生的 [OriginalMiraiMessage] 转化为Simbot（simbot-mirai组件下）的 [Message].
 */
public fun OriginalMiraiSingleMessage.asSimbotMessage(): Message.Element<*> =
    StandardParser.toSimbot(this)

/**
 *
 * 提供一个 [OriginalMiraiMessage] 的计算函数并作为 [Message] 使用。
 *
 * **注意：通过此方式得到的 [Message] 不可参与序列化。 **
 *
 * @see SimpleMiraiSendOnlyComputableMessage
 */
public fun simbotMessage(factory: (Contact) -> OriginalMiraiMessage): Message =
    SimpleMiraiSendOnlyComputableMessage(factory)


/**
 * 将一个 [Message] 转化为 [OriginalMiraiMessage] 以发送。
 */
@OptIn(InternalApi::class)
public suspend fun Message.toOriginalMiraiMessage(contact: Contact): OriginalMiraiMessage {
    return when (this) {
        is OriginalMiraiDirectlySimbotMessage<*> -> originalMiraiMessage.takeIf { it !== EmptySingleMessage }
            ?: EmptyMessageChain
        is OriginalMiraiComputableSimbotMessage<*> -> originalMiraiMessage(contact).takeIf { it !== EmptySingleMessage }
            ?: EmptyMessageChain
        else -> {
            val list = mutableListOf<OriginalMiraiMessage>()

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
        messages: MutableCollection<OriginalMiraiMessage>
    )

    fun toSimbot(
        message: OriginalMiraiSingleMessage,
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
        messages: MutableCollection<OriginalMiraiMessage>
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
            is OriginalMiraiComputableSimbotMessage<*> -> message.originalMiraiMessage(contact)
                .takeIf { it !== EmptySingleMessage }?.also(messages::add)
        }
    }

    /**
     * mirai message 转化为 simbot message
     */
    override fun toSimbot(message: OriginalMiraiSingleMessage): Message.Element<*> {
        return when (message) {
            is net.mamoe.mirai.message.data.At -> At(message.target.ID)
            is net.mamoe.mirai.message.data.AtAll -> AtAll
            is net.mamoe.mirai.message.data.PlainText -> Text { message.content }
            is OriginalMiraiImage -> message.asSimbot()
            is OriginalMiraiFlashImage -> message.asSimbot()
            is OriginalMiraiAudio -> message.asSimbot()
            is net.mamoe.mirai.message.data.Face -> Face(message.id.ID)

            // other messages.
            else -> SimbotOriginalMiraiMessage(message)
        }
    }
}


private suspend fun love.forte.simbot.message.Image<*>.toMirai(contact: Contact): OriginalMiraiMessage {
    val id = id.literal
    if (id.isNotEmpty()) {
        return OriginalMiraiImage(id)
    }

    val image: OriginalMiraiImage = when (this) {
        is MiraiImage -> originalImage
        is MiraiSendOnlyImage -> when (val ntImg = originalMiraiMessage(contact)) {
            is OriginalMiraiImage -> ntImg
            is OriginalMiraiFlashImage -> ntImg.image
            else -> throw IllegalStateException("Can not resolve type of img content in MiraiSendOnlyImage")
        }
        else -> resource().use { r -> r.openStream().use { i -> contact.uploadImage(i) } }
    }


    return image
}


internal fun MessageChain.toSimbot(): Messages {
    return map { StandardParser.toSimbot(it) }.toMessages()
}
