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
import love.forte.simbot.component.mirai.MiraiStranger
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.component.mirai.event.MiraiStrangerMessageEvent
import love.forte.simbot.component.mirai.event.toSimbotMessageContent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.randomID
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger
import net.mamoe.mirai.event.events.StrangerMessageEvent as OriginalMiraiStrangerMessageEvent


/**
 *
 * @author ForteScarlet
 */
internal data class MiraiStrangerMessageEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiStrangerMessageEvent,
) : MiraiStrangerMessageEvent {
    override val id: ID = randomID()
    
    override val timestamp: Timestamp = Timestamp.Companion.bySecond(originalEvent.time.toLong())
    private val _user: MiraiStranger = originalEvent.stranger.asSimbot(bot)
    override val messageContent: MiraiReceivedMessageContent = originalEvent.toSimbotMessageContent()
    
    override suspend fun user(): MiraiStranger = _user
    
    
    // region reply api
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger> {
        val miraiMessage = message.toOriginalMiraiMessage(originalEvent.stranger)
        val receipt = originalEvent.stranger.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger> {
        return SimbotMiraiMessageReceiptImpl(originalEvent.stranger.sendMessage(text))
    }
    
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        reply(message.messages)
    // endregion
    
    // region send api
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger> = reply(message)
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger> = reply(text)
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        send(message.messages)
    // endregion
}
