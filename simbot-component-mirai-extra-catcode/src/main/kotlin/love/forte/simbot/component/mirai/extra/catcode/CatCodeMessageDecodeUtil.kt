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

@file:JvmName("CatCodeMessageUtil")
@file:JvmMultifileClass

package love.forte.simbot.component.mirai.extra.catcode

import catcode.CAT_HEAD
import catcode.CatCodeUtil
import catcode.Neko
import catcode.codes.Nyanko
import catcode.deCatText
import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.*
import net.mamoe.mirai.message.data.MessageChain


private val decoders = mutableMapOf<String, CatCodeDecoder>()


/**
 * 添加一个自定义的解析器。
 * 解析器是全局的，设置后将会立即生效。
 *
 * **Kotlin**
 * ```kotlin
 * addDecoders("target_type") { neko, chain ->
 *      // do decoding
 *      return ...
 *  }
 * ```
 *
 * **Java**
 * ```java
 * CatCodeMessageUtil.addDecoders("image", (neko, chain) -> {
 *    // do decoding
 *     return ...;
 * });
 * ```
 *
 * ### 优先级
 * 额外添加的自定义解析器优先级高于默认解析器，会优先使用。
 *
 * @param type 想要解析的猫猫码类型。
 * @param decoder 解析器实例。
 *
 * @return 如果type对应解析器已经存在，返回旧的解析器。
 *
 */
@Synchronized
public fun addDecoders(type: String, decoder: CatCodeDecoder): CatCodeDecoder? {
    return decoders.put(type, decoder)
}


/**
 * 将一个 [Neko] 实例转化为 [Messages].
 *
 * 可以提供一个基准消息链 [baseMessageChain] 作为参数，对于部分catcode类型来讲，
 * 提供额外的消息链可以有一定程度的优化（例如图片类型的catcode）。
 *
 * @param baseMessageChain 当前事件上下文中的消息链
 *
 * @throws love.forte.simbot.SimbotIllegalArgumentException 参数缺失或无效时
 * @throws RuntimeException 可能存在解析过程中的各种异常。
 * @return 解析得到的消息元素。
 */
@JvmOverloads
public fun Neko.toMessage(baseMessageChain: MessageChain? = null): Message.Element<*> {
    return decoders[type]?.decode(this, baseMessageChain)
        ?: when (type) {
            "text", "message" -> TextCatCodeSerializer.decode(this, baseMessageChain)
            "at" -> AtCatCodeSerializer.decode(this, baseMessageChain)
            "atall", "atAll" -> AtAllCatCodeSerializer.decode(this, baseMessageChain)
            "face" -> FaceCatCodeSerializer.decode(this, baseMessageChain)
            "marketFace" -> MarketFaceCatCodeSerializer.decode(this, baseMessageChain)
            "vipFace" -> VipFaceCatCodeSerializer.decode(this, baseMessageChain)
            // 戳一戳，窗口抖动
            "poke", "shake" -> PokeCatCodeSerializer.decode(this, baseMessageChain)
            // 头像抖动
            "nudge" -> NudgeCatCodeSerializer.decode(this, baseMessageChain)
            // 图片
            "image", "img" -> ImageCatCodeSerializer.decode(this, baseMessageChain)
            // 语音
            "audio", "voice", "record" -> AudioCatCodeSerializer.decode(this, baseMessageChain)
            // 群文件上传
            "file" -> FileCatCodeSerializer.decode(this, baseMessageChain)
            // 分享
            "share" -> ShareCatCodeSerializer.decode(this, baseMessageChain)
            // 卡片相关
            "rich" -> RichCatCodeSerializer.decode(this, baseMessageChain)
            "app", "json" -> AppJsonCatCodeSerializer.decode(this, baseMessageChain)
            // 骰子
            "dice" -> DiceCatCodeSerializer.decode(this, baseMessageChain)
            "xml" -> XmlCatCodeSerializer.decode(this, baseMessageChain)
            // 音乐分享
            "music", "musicShare" -> MusicShareCatCodeSerializer.decode(this, baseMessageChain)
            // 引用回复
            "quote" -> QuoteCatCodeSerializer.decode(this, baseMessageChain)
            // 未知消息
            "unsupported" -> UnsupportedCatCodeSerializer.decode(this, baseMessageChain)
            else -> this.toString().toText()
        }
}


/**
 * 将一个可能包含0到多个catcode的消息字符串转化为 [Message].
 *
 * 可以提供一个基准消息链 [baseMessageChain] 作为参数，对于部分catcode类型来讲，
 * 提供额外的消息链可以有一定程度的优化（例如图片类型的catcode）。
 *
 * @param baseMessageChain 当前事件上下文中的消息链
 *
 * @return 解析得到的消息元素。
 */
@JvmOverloads
public fun catCodeToMessage(code: String, baseMessageChain: MessageChain? = null): Message {
    val messageElements = CatCodeUtil.split(code) {
        if (startsWith(CAT_HEAD)) {
            Nyanko.byCode(code).toMessage(baseMessageChain)
        } else {
            this.deCatText().toText()
        }
    }

    return messageElements.toMessages()
}

/**
 * 将一个 [Neko] 实例转化为 [Message.Element].
 *
 * 可以提供一个基准消息内容 [messageContent] 作为参数，
 * 如果这个消息内容的类型为 [MiraiReceivedMessageContent],
 * 那么便会提供其中的 [MiraiReceivedMessageContent.originalMessageChain]
 * 来尝试为解析提供可能的优化空间。
 *
 * 对于部分catcode类型来讲，
 * 提供额外的消息链可以有一定程度的优化（例如图片类型的catcode）。
 *
 * @param messageContent 基准消息内容
 *
 * @return 解析得到的消息元素。
 */
public fun Neko.toMessage(messageContent: MessageContent): Message.Element<*> {
    if (messageContent is MiraiReceivedMessageContent) {
        return toMessage(messageContent.originalMessageChain)
    }
    return toMessage()
}

/**
 * 将一个 [Neko] 实例转化为 [Message.Element].
 *
 * 可以提供一个基准消息事件 [messageEvent] 作为参数，如果这个消息事件中的 [messageContent][MessageEvent.messageContent]
 * 类型为 [MiraiReceivedMessageContent], 那么便会提供其中的 [MiraiReceivedMessageContent.originalMessageChain]
 * 来尝试为解析提供可能的优化空间。
 *
 * 对于部分catcode类型来讲，
 * 提供额外的消息链可以有一定程度的优化（例如图片类型的catcode）。
 *
 * @param messageEvent 基准消息事件
 *
 * @return 解析得到的消息元素。
 */
public fun Neko.toMessage(messageEvent: MessageEvent): Message.Element<*> {
    val content = messageEvent.messageContent
    if (content is MiraiReceivedMessageContent) {
        return toMessage(content)
    }

    return toMessage()
}

/**
 * 将一个可能包含0到多个catcode的消息字符串转化为 [Message].
 *
 * 可以提供一个基准消息内容 [messageContent] 作为参数，
 * 如果这个消息内容的类型为 [MiraiReceivedMessageContent],
 * 那么便会提供其中的 [MiraiReceivedMessageContent.originalMessageChain]
 * 来尝试为解析提供可能的优化空间。
 *
 * 对于部分catcode类型来讲，
 * 提供额外的消息链可以有一定程度的优化（例如图片类型的catcode）。
 *
 * @param messageContent 基准消息内容
 *
 * @return 解析得到的消息对象。
 */
public fun catCodeToMessage(code: String, messageContent: MessageContent): Message {
    if (messageContent is MiraiReceivedMessageContent) {
        return catCodeToMessage(code, messageContent.originalMessageChain)
    }
    return catCodeToMessage(code)
}

/**
 * 将一个可能包含0到多个catcode的消息字符串转化为 [Message].
 *
 * 可以提供一个基准消息事件 [messageEvent] 作为参数，如果这个消息事件中的 [messageContent][MessageEvent.messageContent]
 * 类型为 [MiraiReceivedMessageContent], 那么便会提供其中的 [MiraiReceivedMessageContent.originalMessageChain]
 * 来尝试为解析提供可能的优化空间。
 *
 * 对于部分catcode类型来讲，
 * 提供额外的消息链可以有一定程度的优化（例如图片类型的catcode）。
 *
 * @param messageEvent 基准消息事件
 *
 * @return 解析得到的消息。
 */
public fun catCodeToMessage(code: String, messageEvent: MessageEvent): Message {
    val content = messageEvent.messageContent
    if (content is MiraiReceivedMessageContent) {
        return catCodeToMessage(code, content)
    }

    return catCodeToMessage(code)
}
