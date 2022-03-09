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

import love.forte.simbot.*
import love.forte.simbot.action.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds


/***/
internal class MiraiBotGroupRoleChangeEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotGroupPermissionChangeEvent
) : MiraiBotGroupRoleChangeEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val group: MiraiGroup = originalEvent.group.asSimbot(bot)
    override val before: MemberRole = originalEvent.origin.simbotRole
    override val after: MemberRole = originalEvent.new.simbotRole
}

/***/
internal class MiraiBotJoinGroupEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotJoinGroupEvent
) : MiraiBotJoinGroupEvent {
    override val id: ID = randomID()
    override val changedTime = Timestamp.now()
    override val group = originalEvent.group.asSimbot(bot)

    @OptIn(MiraiExperimentalApi::class)
    override val operator = if (originalEvent is BotJoinGroupEvent.Invite) {
        originalEvent.invitor.asSimbot(bot, group)
    } else {
        null
    }
    override val target: MiraiMember = originalEvent.group.botAsMember.asSimbot(bot, group)
}

/***/
internal class MiraiBotUnmuteEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotUnmuteEvent
) : MiraiBotUnmuteEvent {
    override val id: ID = randomID()
    override val changedTime = Timestamp.now()
    override val group = originalEvent.group.asSimbot(bot)
    override val operator = originalEvent.operator.asSimbot(bot, group)
}

/***/
internal class MiraiBotMuteEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotMuteEvent
) : MiraiBotMuteEvent {
    override val id: ID = randomID()
    override val changedTime = Timestamp.now()
    override val group = originalEvent.group.asSimbot(bot)
    override val operator = originalEvent.operator.asSimbot(bot, group)
    override val durationSeconds: Int = originalEvent.durationSeconds
    override val duration: Duration get() = durationSeconds.seconds
}

/***/
internal class MiraiBotLeaveEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotLeaveEvent
) : MiraiBotLeaveEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val group = originalEvent.group.asSimbot(bot)
    override val target = originalEvent.group.botAsMember.asSimbot(bot, group)

    @OptIn(MiraiExperimentalApi::class)
    override val operator: MiraiMember = when (originalEvent) {
        is BotLeaveEvent.Kick -> originalEvent.operator.asSimbot(bot, group)
        is BotLeaveEvent.Disband -> originalEvent.operator.asSimbot(bot, group)
        else -> target
    }

    @OptIn(MiraiExperimentalApi::class)
    override val actionType: ActionType = when (originalEvent) {
        is BotLeaveEvent.Kick -> ActionType.PASSIVE
        is BotLeaveEvent.Disband -> ActionType.PASSIVE
        else -> ActionType.PROACTIVE
    }
}