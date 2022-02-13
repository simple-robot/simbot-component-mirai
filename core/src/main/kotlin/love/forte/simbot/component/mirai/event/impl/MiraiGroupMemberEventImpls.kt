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

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.action.ActionType
import love.forte.simbot.component.mirai.MemberRole
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiGroupImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.simbotRole
import love.forte.simbot.definition.GroupInfo
import love.forte.simbot.event.Event
import love.forte.simbot.event.RequestEvent
import love.forte.simbot.randomID
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.event.events.MemberHonorChangeEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent
import net.mamoe.mirai.event.events.operatorOrBot
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


internal abstract class BaseMiraiGroupMemberEvent<E : NativeMiraiGroupMemberEvent>(
    final override val bot: MiraiBotImpl,
    final override val nativeEvent: E
) : MiraiGroupMemberEvent<E> {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.now()
    final override val group = nativeEvent.group.asSimbot(bot)
    override val member: MiraiMember = nativeEvent.member.asSimbot(bot, group)
}


internal class MiraiGroupTalkativeChangeEventImpl(
    override val bot: MiraiBotImpl,
    override val nativeEvent: NativeMiraiGroupTalkativeChangeEvent
) : MiraiGroupTalkativeChangeEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val group = nativeEvent.group.asSimbot(bot)
    override val before: MiraiMember = nativeEvent.previous.asSimbot(bot, group)
    override val after: MiraiMember = nativeEvent.now.asSimbot(bot, group)
}

@OptIn(MiraiExperimentalApi::class)
internal class MiraiMemberHonorChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: NativeMiraiMemberHonorChangeEvent
) : BaseMiraiGroupMemberEvent<NativeMiraiMemberHonorChangeEvent>(bot, nativeEvent), MiraiMemberHonorChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val honorType: GroupHonorType = nativeEvent.honorType
    override val after: GroupHonorType? =
        if (nativeEvent is MemberHonorChangeEvent.Achieve) honorType else null
    override val before: GroupHonorType? =
        if (nativeEvent is MemberHonorChangeEvent.Lose) honorType else null
}


internal class MiraiMemberUnmuteEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: NativeMiraiMemberUnmuteEvent
) : BaseMiraiGroupMemberEvent<NativeMiraiMemberUnmuteEvent>(bot, nativeEvent), MiraiMemberUnmuteEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
}

internal class MiraiMemberMuteEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: NativeMiraiMemberMuteEvent
) : BaseMiraiGroupMemberEvent<NativeMiraiMemberMuteEvent>(bot, nativeEvent), MiraiMemberMuteEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val durationSeconds: Int = nativeEvent.durationSeconds
    override val duration: Duration get() = durationSeconds.seconds
    override val operator: MiraiMember = nativeEvent.operatorOrBot.asSimbot(bot, group)
}

internal class MiraiMemberRoleChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: NativeMiraiMemberPermissionChangeEvent
) : BaseMiraiGroupMemberEvent<NativeMiraiMemberPermissionChangeEvent>(bot, nativeEvent),
    MiraiMemberRoleChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val before: MemberRole = nativeEvent.origin.simbotRole
    override val after: MemberRole = nativeEvent.new.simbotRole
}

internal class MiraiMemberSpecialTitleChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: NativeMiraiMemberSpecialTitleChangeEvent
) : BaseMiraiGroupMemberEvent<NativeMiraiMemberSpecialTitleChangeEvent>(bot, nativeEvent),
    MiraiMemberSpecialTitleChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val before: String = nativeEvent.origin
    override val after: String = nativeEvent.new

    /**
     * 操作人. 可能是群主. 可能与 [member] 引用相同, 此时为群员自己修改.
     * 可能是机器人的操作.
     */
    override val operator: MiraiMember = when {
        nativeEvent.operator === nativeEvent.member -> member
        else -> nativeEvent.operatorOrBot.asSimbot(bot, group)
    }
}

internal class MiraiMemberCardChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: NativeMiraiMemberCardChangeEvent
) : BaseMiraiGroupMemberEvent<NativeMiraiMemberCardChangeEvent>(bot, nativeEvent), MiraiMemberCardChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val before: String = nativeEvent.origin
    override val after: String = nativeEvent.new
}

internal class MiraiMemberJoinRequestEventImpl(
    override val bot: MiraiBotImpl,
    override val nativeEvent: NativeMiraiMemberJoinRequestEvent
) : MiraiMemberJoinRequestEvent {
    override val id: ID = nativeEvent.eventId.ID
    override val timestamp: Timestamp = Timestamp.now()
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.INTERNAL
    override val type: RequestEvent.Type =
        if (nativeEvent.invitorId == null) RequestEvent.Type.APPLICATION
        else RequestEvent.Type.INVITATION

    override val group = nativeEvent.group?.asSimbot(bot)
        ?: GroupInfoImpl(nativeEvent.groupId, nativeEvent.groupName)

    override val inviter: RequestMemberInviterInfo? = nativeEvent.invitorId?.let { id ->
        RequestMemberInviterInfo(id.ID, nativeEvent.invitor?.let { m ->
            if (group is MiraiGroupImpl) m.asSimbot(bot, group) else m.asSimbot(bot)
        })
    }
    override val message: String = nativeEvent.message
    override val requester: RequestMemberInfo = RequestMemberInfo(
        nativeEvent.fromId.ID, nativeEvent.groupId,
        nativeEvent.groupName, nativeEvent.fromNick
    )

    private data class GroupInfoImpl(
        val groupId: Long,
        override val name: String,
    ) : GroupInfo {
        override val id: ID = groupId.ID
        override val createTime: Timestamp get() = Timestamp.NotSupport
        override val currentMember: Int get() = -1
        override val description: String get() = ""
        override val icon: String
            get() = "https://p.qlogo.cn/gh/$groupId/$groupId/640"
        override val maximumMember: Int get() = -1
        override val ownerId: ID get() = EMPTY_ID

        private companion object {
            private val EMPTY_ID = "".ID
        }
    }

}


internal class MiraiMemberLeaveEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: NativeMiraiMemberLeaveEvent
) : BaseMiraiGroupMemberEvent<NativeMiraiMemberLeaveEvent>(bot, nativeEvent), MiraiMemberLeaveEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.INTERNAL
    override val actionType: ActionType =
        if (nativeEvent is MemberLeaveEvent.Kick) ActionType.PASSIVE else ActionType.PROACTIVE
    override val operator: MiraiMember =
        if (nativeEvent is MemberLeaveEvent.Kick) nativeEvent.operatorOrBot.asSimbot(bot, group)
        else member
}

internal class MiraiMemberJoinEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: NativeMiraiMemberJoinEvent
) : BaseMiraiGroupMemberEvent<NativeMiraiMemberJoinEvent>(bot, nativeEvent), MiraiMemberJoinEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.INTERNAL
    override val actionType: ActionType =
        if (nativeEvent is MemberJoinEvent.Invite) ActionType.PASSIVE else ActionType.PROACTIVE
    override val inviter: MiraiMember? =
        if (nativeEvent is MemberJoinEvent.Invite) nativeEvent.invitor.asSimbot(bot, group) else null

}
