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
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.event.MiraiFriendMessageRecallEvent
import love.forte.simbot.component.mirai.event.MiraiGroupMessageRecallEvent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiGroupImpl
import love.forte.simbot.component.mirai.internal.MiraiMemberImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toSimbot
import love.forte.simbot.message.Messages
import love.forte.simbot.randomID
import net.mamoe.mirai.event.events.MessageRecallEvent


internal class MiraiFriendMessageRecallEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: MessageRecallEvent.FriendRecall,
) : MiraiFriendMessageRecallEvent() {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.now()
    private val _friend: MiraiFriend = originalEvent.author.asSimbot(bot)
    
    override val messages: Messages? =
        bot.recallMessageCacheStrategy.getFriendMessageCache(bot, originalEvent)?.toSimbot()
    
    override suspend fun friend(): MiraiFriend = _friend
}

internal class MiraiGroupMessageRecallEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: MessageRecallEvent.GroupRecall,
) : MiraiGroupMessageRecallEvent() {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.now()
    private val _group: MiraiGroupImpl = originalEvent.group.asSimbot(bot)
    override val author: MiraiMemberImpl = originalEvent.author.asSimbot(bot, _group)
    override val operator: MiraiMemberImpl? = originalEvent.operator?.asSimbot(bot, _group)
    
    override val messages: Messages? =
        bot.recallMessageCacheStrategy.getGroupMessageCache(bot, originalEvent)?.toSimbot()
    
    override suspend fun group(): MiraiGroup = _group
    
}


