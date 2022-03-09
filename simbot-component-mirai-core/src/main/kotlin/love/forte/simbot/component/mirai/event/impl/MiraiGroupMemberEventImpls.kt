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
import love.forte.simbot.definition.GroupInfo
import love.forte.simbot.event.*
import net.mamoe.mirai.data.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds


internal abstract class BaseMiraiGroupMemberEvent<E : OriginalMiraiGroupMemberEvent>(
    final override val bot: MiraiBotImpl,
    final override val originalEvent: E
) : MiraiGroupMemberEvent<E> {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.now()
    final override val group = originalEvent.group.asSimbot(bot)
    override val member: MiraiMember = originalEvent.member.asSimbot(bot, group)
}


internal class MiraiGroupTalkativeChangeEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiGroupTalkativeChangeEvent
) : MiraiGroupTalkativeChangeEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val group = originalEvent.group.asSimbot(bot)
    override val before: MiraiMember = originalEvent.previous.asSimbot(bot, group)
    override val after: MiraiMember = originalEvent.now.asSimbot(bot, group)
}

@OptIn(MiraiExperimentalApi::class)
internal class MiraiMemberHonorChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberHonorChangeEvent
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberHonorChangeEvent>(bot, nativeEvent), MiraiMemberHonorChangeEvent {
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
    nativeEvent: OriginalMiraiMemberUnmuteEvent
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberUnmuteEvent>(bot, nativeEvent), MiraiMemberUnmuteEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
}

internal class MiraiMemberMuteEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberMuteEvent
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberMuteEvent>(bot, nativeEvent), MiraiMemberMuteEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val durationSeconds: Int = nativeEvent.durationSeconds
    override val duration: Duration get() = durationSeconds.seconds
    override val operator: MiraiMember = nativeEvent.operatorOrBot.asSimbot(bot, group)
}

internal class MiraiMemberRoleChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberPermissionChangeEvent
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberPermissionChangeEvent>(bot, nativeEvent),
    MiraiMemberRoleChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val before: MemberRole = nativeEvent.origin.simbotRole
    override val after: MemberRole = nativeEvent.new.simbotRole
}

internal class MiraiMemberSpecialTitleChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberSpecialTitleChangeEvent
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberSpecialTitleChangeEvent>(bot, nativeEvent),
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
    nativeEvent: OriginalMiraiMemberCardChangeEvent
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberCardChangeEvent>(bot, nativeEvent), MiraiMemberCardChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val before: String = nativeEvent.origin
    override val after: String = nativeEvent.new
}

internal class MiraiMemberJoinRequestEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiMemberJoinRequestEvent
) : MiraiMemberJoinRequestEvent {
    override val id: ID = originalEvent.eventId.ID
    override val timestamp: Timestamp = Timestamp.now()
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.INTERNAL
    override val type: RequestEvent.Type =
        if (originalEvent.invitorId == null) RequestEvent.Type.APPLICATION
        else RequestEvent.Type.INVITATION

    override val group = originalEvent.group?.asSimbot(bot)
        ?: GroupInfoImpl(originalEvent.groupId, originalEvent.groupName)

    override val inviter: RequestMemberInviterInfo? = originalEvent.invitorId?.let { id ->
        RequestMemberInviterInfo(id.ID, originalEvent.invitor?.let { m ->
            if (group is MiraiGroupImpl) m.asSimbot(bot, group) else m.asSimbot(bot)
        })
    }
    override val message: String = originalEvent.message
    override val requester: RequestMemberInfo = RequestMemberInfo(
        originalEvent.fromId.ID, originalEvent.groupId,
        originalEvent.groupName, originalEvent.fromNick
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
    nativeEvent: OriginalMiraiMemberLeaveEvent
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberLeaveEvent>(bot, nativeEvent), MiraiMemberLeaveEvent {
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
    nativeEvent: OriginalMiraiMemberJoinEvent
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberJoinEvent>(bot, nativeEvent), MiraiMemberJoinEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.INTERNAL
    override val actionType: ActionType =
        if (nativeEvent is MemberJoinEvent.Invite) ActionType.PASSIVE else ActionType.PROACTIVE
    override val inviter: MiraiMember? =
        if (nativeEvent is MemberJoinEvent.Invite) nativeEvent.invitor.asSimbot(bot, group) else null

}
