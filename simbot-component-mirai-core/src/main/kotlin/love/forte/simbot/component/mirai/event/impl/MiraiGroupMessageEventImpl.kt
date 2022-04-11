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

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent
import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.component.mirai.event.toSimbotMessageContent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiGroupImpl
import love.forte.simbot.component.mirai.internal.MiraiMemberImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.randomID
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup
import net.mamoe.mirai.event.events.GroupMessageEvent as OriginalMiraiGroupMessageEvent


/**
 */
internal class MiraiGroupMessageEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiGroupMessageEvent
) : MiraiGroupMessageEvent {
    override val id: ID = randomID()
    override val messageContent: MiraiReceivedMessageContent = originalEvent.toSimbotMessageContent()
    override val author: MiraiMemberImpl = originalEvent.sender.asSimbot(bot)
    override val group: MiraiGroupImpl = originalEvent.group.asSimbot(bot)
    override suspend fun delete(): Boolean {
        return try {
            messageContent.messageSource.recall()
            true
        } catch (illegalState: IllegalStateException) {
            false
        }
    }


    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val miraiMessage = message.toOriginalMiraiMessage(originalEvent.group)
        val receipt = originalEvent.group.sendMessage(QuoteReply(originalEvent.source) + miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val receipt = originalEvent.group.sendMessage(QuoteReply(originalEvent.source) + text.toPlainText())
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val miraiMessage = message.toOriginalMiraiMessage(originalEvent.group)
        val receipt = originalEvent.group.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val receipt = originalEvent.group.sendMessage(text)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }


    override val timestamp: Timestamp = Timestamp.bySecond(originalEvent.time.toLong())

}