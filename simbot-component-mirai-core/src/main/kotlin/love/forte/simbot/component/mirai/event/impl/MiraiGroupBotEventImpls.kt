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
 *
 */

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.action.ActionType
import love.forte.simbot.component.mirai.MemberRole
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.simbotRole
import love.forte.simbot.randomID
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
    override val group: MiraiGroup = originalEvent.group.asSimbot(bot)
    override val before: MemberRole = originalEvent.origin.simbotRole
    override val after: MemberRole = originalEvent.new.simbotRole

    override val organization: MiraiGroup get() = group
    override val source: MiraiBot get() = bot


    override suspend fun group(): MiraiGroup = group
    override suspend fun organization(): MiraiGroup = organization
    override suspend fun before(): MemberRole = before
    override suspend fun after(): MemberRole = after
    override suspend fun source(): MiraiBot = source
}

/***/
internal class MiraiBotJoinGroupEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotJoinGroupEvent,
) : MiraiBotJoinGroupEvent {
    override val id: ID = randomID()
    override val changedTime = Timestamp.now()
    override val group = originalEvent.group.asSimbot(bot)

    @OptIn(MiraiExperimentalApi::class)
    override val operator = if (originalEvent is OriginalMiraiBotJoinGroupEvent.Invite) {
        originalEvent.invitor.asSimbot(bot, group)
    } else {
        null
    }
    override val user: MiraiMember = originalEvent.group.botAsMember.asSimbot(bot, group)

    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(Api4J::class)
    override val organization: MiraiGroup get() = group
    override val member: MiraiMember get() = user
    override val source: MiraiGroup get() = group
    override val after: MiraiMember get() = user

    override suspend fun member(): MiraiMember = member
    override suspend fun group(): MiraiGroup = group
    override suspend fun organization(): MiraiGroup = group
    override suspend fun user(): MiraiMember = user
    override suspend fun source(): MiraiGroup = group
    override suspend fun after(): MiraiMember = after
}

/***/
internal class MiraiBotUnmuteEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotUnmuteEvent,
) : MiraiBotUnmuteEvent {
    override val id: ID = randomID()
    override val changedTime = Timestamp.now()
    override val group = originalEvent.group.asSimbot(bot)
    override val operator = originalEvent.operator.asSimbot(bot, group)
    override val organization: MiraiGroup get() = group
    override val duration: Duration = 0.seconds
    override val durationSeconds: Int get() = 0
    override val source: MiraiGroup get() = group


    override suspend fun group(): MiraiGroup = group
    override suspend fun organization(): MiraiGroup = organization
    override suspend fun after(): Boolean = after
    override suspend fun before(): Boolean = before
    override suspend fun source(): MiraiGroup = source
}

/***/
internal class MiraiBotMuteEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotMuteEvent,
) : MiraiBotMuteEvent {
    override val id: ID = randomID()
    override val changedTime = Timestamp.now()
    override val group = originalEvent.group.asSimbot(bot)
    override val operator = originalEvent.operator.asSimbot(bot, group)
    override val durationSeconds: Int = originalEvent.durationSeconds
    override val duration: Duration get() = durationSeconds.seconds
    override val organization: MiraiGroup get() = group
    override val source: MiraiGroup get() = group

    override suspend fun group(): MiraiGroup = group
    override suspend fun organization(): MiraiGroup = organization
    override suspend fun after(): Boolean = after
    override suspend fun before(): Boolean = before
    override suspend fun source(): MiraiGroup = source
}

/***/
internal class MiraiBotLeaveEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotLeaveEvent,
) : MiraiBotLeaveEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val group = originalEvent.group.asSimbot(bot)
    override val member = originalEvent.group.botAsMember.asSimbot(bot, group)


    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(Api4J::class)
    override val organization: MiraiGroup get() = group
    override val user: MiraiMember get() = member
    override val source: MiraiGroup get() = group
    override val before: MiraiMember get() = member

    @OptIn(MiraiExperimentalApi::class)
    override val operator: MiraiMember = when (originalEvent) {
        is OriginalMiraiBotLeaveEvent.Kick -> originalEvent.operator.asSimbot(bot, group)
        is OriginalMiraiBotLeaveEvent.Disband -> originalEvent.operator.asSimbot(bot, group)
        else -> member
    }

    @OptIn(MiraiExperimentalApi::class)
    override val actionType: ActionType = when (originalEvent) {
        is OriginalMiraiBotLeaveEvent.Kick -> ActionType.PASSIVE
        is OriginalMiraiBotLeaveEvent.Disband -> ActionType.PASSIVE
        else -> ActionType.PROACTIVE
    }

    override suspend fun group(): MiraiGroup = group
    override suspend fun organization(): MiraiGroup = organization
    override suspend fun member(): MiraiMember = member
    override suspend fun user(): MiraiMember = user
    override suspend fun operator(): MiraiMember = operator
    override suspend fun source(): MiraiGroup = source
    override suspend fun before(): MiraiMember = before
}