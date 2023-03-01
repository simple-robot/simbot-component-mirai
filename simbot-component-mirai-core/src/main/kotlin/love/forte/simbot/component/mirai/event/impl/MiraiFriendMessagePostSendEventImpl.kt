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
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.event.MiraiFriendMessagePostSendEvent
import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.component.mirai.event.toSimbotMessageContent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.randomID
import net.mamoe.mirai.event.events.source
import net.mamoe.mirai.event.events.FriendMessagePostSendEvent as OriginalMiraiFriendMessagePostSendEvent

/**
 *
 * @author ForteScarlet
 */
internal data class MiraiFriendMessagePostSendEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendMessagePostSendEvent,
) : MiraiFriendMessagePostSendEvent {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.now()
    override val messageContent: MiraiReceivedMessageContent =
        originalEvent.message.toSimbotMessageContent(originalEvent.source)
    private val _friend = originalEvent.target.asSimbot(bot)
    
    override suspend fun friend(): MiraiFriend = _friend
}
