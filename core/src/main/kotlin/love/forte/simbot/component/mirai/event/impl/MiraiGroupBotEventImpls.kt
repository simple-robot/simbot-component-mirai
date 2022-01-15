package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.action.ActionType
import love.forte.simbot.component.mirai.MemberPermission
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.simbotRole
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/***/
internal class MiraiBotGroupPermissionChangeEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiBotGroupPermissionChangeEvent
) : MiraiBotGroupPermissionChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val metadata: MiraiSimbotEvent.Metadata<NativeMiraiBotGroupPermissionChangeEvent> =
        nativeEvent.toSimpleMetadata()
    override val group: MiraiGroup = nativeEvent.group.asSimbot(bot)
    override val before: MemberPermission = nativeEvent.origin.simbotRole.permission
    override val after: MemberPermission = nativeEvent.new.simbotRole.permission
}

/***/
internal class MiraiBotJoinGroupEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiBotJoinGroupEvent
) : MiraiBotJoinGroupEvent {
    override val changedTime = Timestamp.now()
    override val metadata = nativeEvent.toSimpleMetadata()
    override val group = nativeEvent.group.asSimbot(bot)

    @OptIn(MiraiExperimentalApi::class)
    override val operator = if (nativeEvent is BotJoinGroupEvent.Invite) {
        nativeEvent.invitor.asSimbot(bot, group)
    } else {
        null
    }
    override val target: MiraiMember = nativeEvent.group.botAsMember.asSimbot(bot, group)
}

/***/
internal class MiraiBotUnmuteEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiBotUnmuteEvent
) : MiraiBotUnmuteEvent {
    override val changedTime = Timestamp.now()
    override val metadata = nativeEvent.toSimpleMetadata()
    override val group = nativeEvent.group.asSimbot(bot)
    override val operator = nativeEvent.operator.asSimbot(bot, group)
}

/***/
internal class MiraiBotMuteEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiBotMuteEvent
) : MiraiBotMuteEvent {
    override val changedTime = Timestamp.now()
    override val metadata = nativeEvent.toSimpleMetadata()
    override val group = nativeEvent.group.asSimbot(bot)
    override val operator = nativeEvent.operator.asSimbot(bot, group)
    override val durationSeconds: Int = nativeEvent.durationSeconds
    override val duration: Duration get() = durationSeconds.seconds
}

/***/
internal class MiraiBotLeaveEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiBotLeaveEvent
) : MiraiBotLeaveEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val metadata: MiraiSimbotEvent.Metadata<NativeMiraiBotLeaveEvent> = nativeEvent.toSimpleMetadata()
    override val group = nativeEvent.group.asSimbot(bot)
    override val target = nativeEvent.group.botAsMember.asSimbot(bot, group)

    @OptIn(MiraiExperimentalApi::class)
    override val operator: MiraiMember = when (nativeEvent) {
        is BotLeaveEvent.Kick -> nativeEvent.operator.asSimbot(bot, group)
        is BotLeaveEvent.Disband -> nativeEvent.operator.asSimbot(bot, group)
        else -> target
    }

    @OptIn(MiraiExperimentalApi::class)
    override val actionType: ActionType = when (nativeEvent) {
        is BotLeaveEvent.Kick -> ActionType.PASSIVE
        is BotLeaveEvent.Disband -> ActionType.PASSIVE
        else -> ActionType.PROACTIVE
    }
}