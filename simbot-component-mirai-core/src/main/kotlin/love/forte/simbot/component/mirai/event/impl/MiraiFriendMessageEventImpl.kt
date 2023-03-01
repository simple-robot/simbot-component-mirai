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

import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.event.MiraiFriendMessageEvent
import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.component.mirai.event.toSimbotMessageContent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.randomID
import love.forte.simbot.utils.runInBlocking
import java.time.Instant
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend
import net.mamoe.mirai.event.events.FriendMessageEvent as OriginalMiraiFriendMessageEvent


internal class MiraiFriendMessageEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendMessageEvent,
) : MiraiFriendMessageEvent {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.byInstant(Instant.ofEpochSecond(originalEvent.time.toLong()))
    override val messageContent: MiraiReceivedMessageContent = originalEvent.toSimbotMessageContent()
    
    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(Api4J::class)
    private val _friend = originalEvent.friend.asSimbot(bot)
    
    override suspend fun friend(): MiraiFriend = _friend
    
    
    // region send api
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend> = reply(message)
    
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend> = reply(text)
    
    
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend> =
        send(message.messages)
    // endregion
    
    
    // region reply api
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend> {
        val miraiMessage = message.toOriginalMiraiMessage(originalEvent.friend)
        val receipt = originalEvent.friend.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend> {
        val receipt = originalEvent.friend.sendMessage(text)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend> =
        runInBlocking { reply(message.messages) }
    // endregion
    
    
}
