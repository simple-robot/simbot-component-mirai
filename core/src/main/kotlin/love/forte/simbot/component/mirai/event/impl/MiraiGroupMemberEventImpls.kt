package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.action.ActionType
import love.forte.simbot.component.mirai.MemberPermission
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiGroupImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.simbotRole
import love.forte.simbot.definition.GroupInfo
import love.forte.simbot.event.Event
import love.forte.simbot.event.RequestEvent
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
    nativeEvent: E
) : MiraiGroupMemberEvent<E> {
    override val timestamp: Timestamp = Timestamp.now()
    final override val group = nativeEvent.group.asSimbot(bot)
    override val member: MiraiMember = nativeEvent.member.asSimbot(bot, group)
    override val metadata = nativeEvent.toSimpleMetadata()
}


internal class MiraiGroupTalkativeChangeEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiGroupTalkativeChangeEvent
) : MiraiGroupTalkativeChangeEvent {
    override val metadata = nativeEvent.toSimpleMetadata()
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
}

internal class MiraiMemberPermissionChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: NativeMiraiMemberPermissionChangeEvent
) : BaseMiraiGroupMemberEvent<NativeMiraiMemberPermissionChangeEvent>(bot, nativeEvent),
    MiraiMemberPermissionChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val before: MemberPermission = nativeEvent.origin.simbotRole.permission
    override val after: MemberPermission = nativeEvent.new.simbotRole.permission
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
    private val nativeEvent: NativeMiraiMemberJoinRequestEvent
) : MiraiMemberJoinRequestEvent {
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
    override val metadata = nativeEvent.toSimpleMetadata(nativeEvent.eventId.ID)

    private data class GroupInfoImpl(
        val groupId: Long,
        override val name: String,
    ) : GroupInfo {
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
