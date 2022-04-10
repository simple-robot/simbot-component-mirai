
@file:JvmName("CatCodeMessageUtil")
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


private val parsers = mutableMapOf<String, CatCodeDecoder>()


/**
 * 将一个 [Neko] 实例转化为 [Messages].
 *
 * 可以提供一个基准消息链 [baseMessageChain] 作为参数，对于部分catcode类型来讲，
 * 提供额外的消息链可以有一定程度的优化（例如图片类型的catcode）。
 *
 * @param baseMessageChain 当前事件上下文中的消息链
 *
 * @return 解析得到的消息元素。
 */
@JvmOverloads
public fun Neko.toMessage(baseMessageChain: MessageChain? = null): Message.Element<*> {
    return parsers[type]?.decode(this)
        ?: when (type) {
            "text", "message" -> TextDecoder.decode(this, baseMessageChain)
            "at" -> AtDecoder.decode(this, baseMessageChain)
            "atall", "atAll" -> AtAll
            "face" -> FaceDecoder.decode(this, baseMessageChain)
            // 戳一戳，窗口抖动
            "poke", "shake" -> PokeDecoder.decode(this, baseMessageChain)
            // 头像抖动
            "nudge" -> NudgeDecoder.decode(this, baseMessageChain)
            // 图片
            "image", "img" -> ImageDecoder.decode(this, baseMessageChain)
            // 语音
            "voice", "audio", "record" -> VoiceDecoder.decode(this, baseMessageChain)

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
