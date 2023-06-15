/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  This file is part of simbot-component-mirai.
 *
 *  simbot-component-mirai is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  simbot-component-mirai is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with simbot-component-mirai. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

@file:JvmName("MiraiMessageParserUtil")

package love.forte.simbot.component.mirai.message

import love.forte.simbot.ID
import love.forte.simbot.component.mirai.internal.InternalApi
import love.forte.simbot.component.mirai.message.MiraiAudio.Key.asSimbot
import love.forte.simbot.component.mirai.message.MiraiForwardMessage.Key.asSimbot
import love.forte.simbot.message.*
import love.forte.simbot.message.At
import love.forte.simbot.message.AtAll
import love.forte.simbot.message.Face
import love.forte.simbot.message.Message
import love.forte.simbot.message.PlainText
import love.forte.simbot.tryToLong
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.FlashImage
import net.mamoe.mirai.message.data.At as MiraiAtFunc
import net.mamoe.mirai.message.data.Audio as OriginalMiraiAudio
import net.mamoe.mirai.message.data.FlashImage as OriginalMiraiFlashImage
import net.mamoe.mirai.message.data.Image as OriginalMiraiImage
import net.mamoe.mirai.message.data.Message as OriginalMiraiMessage
import net.mamoe.mirai.message.data.MessageChain as OriginalMiraiMessageChain
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
 * **注意：通过此方式得到的 [Message] 不可参与序列化。**
 *
 * @see SimpleMiraiSendOnlyComputableMessage
 */
public fun simbotMessage(factory: (Contact) -> OriginalMiraiMessage): Message =
    SimpleMiraiSendOnlyComputableMessage(factory)

/**
 *
 * 提供一个 [OriginalMiraiMessage] 的计算函数并作为 [Message] 使用。
 *
 * **注意：通过此方式得到的 [Message] 不可参与序列化。**
 *
 * @see SimpleMiraiSendOnlyComputableMessage
 */
public fun simbotMessage(factory: (Contact, drop: Boolean) -> OriginalMiraiMessage): Message =
    SimpleMiraiSendOnlyComputableMessage(factory)


/**
 * 将一个 [Message] 转化为 [OriginalMiraiMessage] 以发送。
 */
@OptIn(InternalApi::class)
public suspend fun Message.toOriginalMiraiMessage(
    contact: Contact,
    isDropAction: Boolean = false,
): OriginalMiraiMessage {
    return when (this) {
        is OriginalMiraiDirectlySimbotMessage<*, *> -> originalMiraiMessage.takeIf { it !== EmptySingleMessage }
            ?: emptyMessageChain()

        is OriginalMiraiComputableSimbotMessage<*> -> originalMiraiMessage(
            contact,
            isDropAction
        ).takeIf { it !== EmptySingleMessage }
            ?: emptyMessageChain()

        else -> {
            val list = mutableListOf<OriginalMiraiMessage>()

            when (this) {
                is Message.Element<*> -> {
                    StandardParser.toMirai(this, contact, list)
                }

                is Messages -> {
                    this.forEach {
                        StandardParser.toMirai(it, contact, list)
                    }
                }
            }

            if (list.isEmpty()) emptyMessageChain() else list.toMessageChain()
        }
    }
}

public suspend fun Message.toOriginalMiraiMessageChain(
    contact: Contact,
    isDropAction: Boolean = false,
): OriginalMiraiMessageChain {
    val msg = toOriginalMiraiMessage(contact, isDropAction)
    return if (msg is OriginalMiraiMessageChain) msg else msg.toMessageChain()
}


internal interface MiraiMessageParser {
    @JvmSynthetic
    suspend fun toMirai(
        message: Message.Element<*>,
        contact: Contact,
        messages: MutableCollection<OriginalMiraiMessage>,
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
        messages: MutableCollection<OriginalMiraiMessage>,
    ) {
        when (message) {
            is StandardMessage<*> -> when (message) {
                is BaseStandardMessage<*> -> when (message) {
                    is At -> messages.add(MiraiAtFunc(message.target.tryToLong()))
                    is AtAll -> messages.add(net.mamoe.mirai.message.data.AtAll)
                    is Text -> messages.add(message.text.toPlainText())
                }

                is PlainText -> messages.add(message.text.toPlainText())
                is Image -> messages.add(message.toMirai(contact))
                is Emoji -> messages.add(":${message.id}:".toPlainText())
                is Face -> {
                    val miraiFace = net.mamoe.mirai.message.data.Face(message.id.tryToLong().toInt())
                    messages.add(miraiFace)
                }

                // ignore RemoteResource
                else -> {}
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
            is net.mamoe.mirai.message.data.Face -> Face(message.id.ID)
            is OriginalMiraiImage -> message.asSimbot()
            is OriginalMiraiFlashImage -> message.asSimbot()
            is OriginalMiraiAudio -> message.asSimbot()
            is ForwardMessage -> message.asSimbot()
            is QuoteReply -> message.asSimbot()

            // 消息类型被遗忘了？告诉我们：https://github.com/simple-robot/simbot-component-mirai/issues
            // other messages.
            else -> SimbotOriginalMiraiMessage(message)
        }
    }
}


private suspend fun Image<*>.toMirai(contact: Contact): OriginalMiraiMessage {
    val image: OriginalMiraiMessage = when (this) {
        is MiraiImage -> if (isFlash) FlashImage.from(originalImage) else originalImage
        is ResourceImage -> resource().uploadToImage(contact, false)
        is MiraiSendOnlyImage -> originalMiraiMessage(contact)
        else -> resource().uploadToImage(contact, false)
    }

    return image
}


internal fun OriginalMiraiMessageChain.toSimbot(): Messages {
    return map { StandardParser.toSimbot(it) }.toMessages()
}
