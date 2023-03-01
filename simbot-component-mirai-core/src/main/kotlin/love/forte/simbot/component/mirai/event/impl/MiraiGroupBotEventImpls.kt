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
import love.forte.simbot.action.ActionType
import love.forte.simbot.component.mirai.MemberRole
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.simbotRole
import love.forte.simbot.randomID
import net.mamoe.mirai.event.events.GroupOperableEvent
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.mamoe.mirai.event.events.BotGroupPermissionChangeEvent as OriginalMiraiBotGroupPermissionChangeEvent
import net.mamoe.mirai.event.events.BotJoinGroupEvent as OriginalMiraiBotJoinGroupEvent
import net.mamoe.mirai.event.events.BotLeaveEvent as OriginalMiraiBotLeaveEvent
import net.mamoe.mirai.event.events.BotMuteEvent as OriginalMiraiBotMuteEvent
import net.mamoe.mirai.event.events.BotUnmuteEvent as OriginalMiraiBotUnmuteEvent


/***/
internal class MiraiBotGroupRoleChangeEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotGroupPermissionChangeEvent,
) : MiraiBotGroupRoleChangeEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    private val _group: MiraiGroup = originalEvent.group.asSimbot(bot)
    private val _before: MemberRole = originalEvent.origin.simbotRole
    private val _after: MemberRole = originalEvent.new.simbotRole
    
    
    override suspend fun group(): MiraiGroup = _group
    override suspend fun before(): MemberRole = _before
    override suspend fun after(): MemberRole = _after
}

/***/
internal class MiraiBotJoinGroupEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotJoinGroupEvent,
) : MiraiBotJoinGroupEvent {
    override val id: ID = randomID()
    override val changedTime = Timestamp.now()
    private val _group = originalEvent.group.asSimbot(bot)
    
    @OptIn(MiraiExperimentalApi::class)
    private val _operator = if (originalEvent is OriginalMiraiBotJoinGroupEvent.Invite) {
        originalEvent.invitor.asSimbot(bot, _group)
    } else {
        null
    }
    private val _member: MiraiMember = originalEvent.group.botAsMember.asSimbot(bot, _group)
    
    override suspend fun member(): MiraiMember = _member
    override suspend fun operator(): MiraiMember? = _operator
    override suspend fun group(): MiraiGroup = _group
}

/***/
internal class MiraiBotUnmuteEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotUnmuteEvent,
) : MiraiBotUnmuteEvent {
    override val id: ID = randomID()
    override val changedTime = Timestamp.now()
    private val _group = originalEvent.group.asSimbot(bot)
    override val operator = originalEvent.operator.asSimbot(bot, _group)
    override val duration: Duration = 0.seconds
    override val durationSeconds: Int get() = 0
    
    override suspend fun group(): MiraiGroup = _group
}

/***/
internal class MiraiBotMuteEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotMuteEvent,
) : MiraiBotMuteEvent {
    override val id: ID = randomID()
    override val changedTime = Timestamp.now()
    private val _group = originalEvent.group.asSimbot(bot)
    override val operator = originalEvent.operator.asSimbot(bot, _group)
    override val durationSeconds: Int = originalEvent.durationSeconds
    override val duration: Duration = durationSeconds.seconds
    
    override suspend fun group(): MiraiGroup = _group
}

/***/
internal class MiraiBotLeaveEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotLeaveEvent,
) : MiraiBotLeaveEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    private val _group = originalEvent.group.asSimbot(bot)
    private val _member = originalEvent.group.botAsMember.asSimbot(bot, _group)
    
    private val _operator: MiraiMember? = when (originalEvent) {
        is GroupOperableEvent -> originalEvent.operator?.asSimbot(bot, _group)
        else -> null
    }
    
    override val actionType: ActionType = when (originalEvent) {
        is GroupOperableEvent -> if (originalEvent.operator != null) ActionType.PASSIVE else ActionType.PROACTIVE
        else -> ActionType.PROACTIVE
    }
    
    override suspend fun group(): MiraiGroup = _group
    override suspend fun member(): MiraiMember = _member
    override suspend fun operator(): MiraiMember? = _operator
}
