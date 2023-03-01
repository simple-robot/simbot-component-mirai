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
import love.forte.simbot.message.MessageContent
import love.forte.simbot.randomID
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup
import net.mamoe.mirai.event.events.GroupMessageEvent as OriginalMiraiGroupMessageEvent


/**
 */
internal data class MiraiGroupMessageEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiGroupMessageEvent,
) : MiraiGroupMessageEvent {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.bySecond(originalEvent.time.toLong())
    override val messageContent: MiraiReceivedMessageContent = originalEvent.toSimbotMessageContent()
    private val _author: MiraiMemberImpl = originalEvent.sender.asSimbot(bot)
    private val _group: MiraiGroupImpl = originalEvent.group.asSimbot(bot)
    
    override suspend fun author() = _author
    override suspend fun group() = _group
    
    //// api
    
    override suspend fun recall(): Boolean {
        return try {
            messageContent.messageSourceOrNull?.recall() ?: return false
            true
        } catch (illegalState: IllegalStateException) {
            false
        }
    }
    
    
    // region reply api
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val miraiMessage = message.toOriginalMiraiMessage(originalEvent.group)
        val receipt = originalEvent.group.sendMessage(QuoteReply(originalEvent.source) + miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val receipt = originalEvent.group.sendMessage(QuoteReply(originalEvent.source) + text.toPlainText())
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiGroup> =
        reply(message.messages)
    // endregion
    
    
    // region send api
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val miraiMessage = message.toOriginalMiraiMessage(originalEvent.group)
        val receipt = originalEvent.group.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val receipt = originalEvent.group.sendMessage(text)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiGroup> =
        send(message.messages)
    // endregion
    
    
}
