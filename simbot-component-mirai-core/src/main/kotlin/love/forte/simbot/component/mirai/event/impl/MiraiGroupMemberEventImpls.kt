/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU Affero通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
 *
 *
 */

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.action.ActionType
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiGroupImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.simbotRole
import love.forte.simbot.definition.GroupInfo
import love.forte.simbot.event.RequestEvent
import love.forte.simbot.randomID
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.event.events.operatorOrBot
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.mamoe.mirai.event.events.GroupMemberEvent as OriginalMiraiGroupMemberEvent
import net.mamoe.mirai.event.events.GroupTalkativeChangeEvent as OriginalMiraiGroupTalkativeChangeEvent
import net.mamoe.mirai.event.events.MemberCardChangeEvent as OriginalMiraiMemberCardChangeEvent
import net.mamoe.mirai.event.events.MemberHonorChangeEvent as OriginalMiraiMemberHonorChangeEvent
import net.mamoe.mirai.event.events.MemberJoinEvent as OriginalMiraiMemberJoinEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent as OriginalMiraiMemberJoinRequestEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent as OriginalMiraiMemberLeaveEvent
import net.mamoe.mirai.event.events.MemberMuteEvent as OriginalMiraiMemberMuteEvent
import net.mamoe.mirai.event.events.MemberPermissionChangeEvent as OriginalMiraiMemberPermissionChangeEvent
import net.mamoe.mirai.event.events.MemberSpecialTitleChangeEvent as OriginalMiraiMemberSpecialTitleChangeEvent
import net.mamoe.mirai.event.events.MemberUnmuteEvent as OriginalMiraiMemberUnmuteEvent


internal abstract class BaseMiraiGroupMemberEvent<E : OriginalMiraiGroupMemberEvent>(
    final override val bot: MiraiBotImpl,
    final override val originalEvent: E,
) : MiraiGroupMemberEvent<E> {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.now()
    protected val groupInternal = originalEvent.group.asSimbot(bot)
    protected val memberInternal = originalEvent.member.asSimbot(bot, groupInternal)
    
    override suspend fun member() = memberInternal
    override suspend fun group() = groupInternal
}


internal class MiraiGroupTalkativeChangeEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiGroupTalkativeChangeEvent,
) : MiraiGroupTalkativeChangeEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    private val _group = originalEvent.group.asSimbot(bot)
    private val _before = originalEvent.previous.asSimbot(bot, _group)
    private val _after = originalEvent.now.asSimbot(bot, _group)
    
    override suspend fun group() = _group
    override suspend fun before() = _before
    override suspend fun after() = _after
    
    override suspend fun member() = after()
    override suspend fun user() = after()
}

@OptIn(MiraiExperimentalApi::class)
internal class MiraiMemberHonorChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberHonorChangeEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberHonorChangeEvent>(bot, nativeEvent), MiraiMemberHonorChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val honorType: GroupHonorType = nativeEvent.honorType
    private val _before: GroupHonorType? =
        if (nativeEvent is OriginalMiraiMemberHonorChangeEvent.Lose) honorType else null
    private val _after: GroupHonorType? =
        if (nativeEvent is OriginalMiraiMemberHonorChangeEvent.Achieve) honorType else null
    
    override suspend fun before(): GroupHonorType? = _before
    
    override suspend fun after(): GroupHonorType? = _after
}


internal class MiraiMemberUnmuteEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberUnmuteEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberUnmuteEvent>(bot, nativeEvent), MiraiMemberUnmuteEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
}

internal class MiraiMemberMuteEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberMuteEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberMuteEvent>(bot, nativeEvent), MiraiMemberMuteEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val durationSeconds: Int = nativeEvent.durationSeconds
    override val duration: Duration get() = durationSeconds.seconds
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, groupInternal)
}

internal class MiraiMemberRoleChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberPermissionChangeEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberPermissionChangeEvent>(bot, nativeEvent),
    MiraiMemberRoleChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    private val _before = nativeEvent.origin.simbotRole
    private val _after = nativeEvent.new.simbotRole
    
    override suspend fun before() = _before
    override suspend fun after() = _after
}

internal class MiraiMemberSpecialTitleChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberSpecialTitleChangeEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberSpecialTitleChangeEvent>(bot, nativeEvent),
    MiraiMemberSpecialTitleChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    private val _before: String = nativeEvent.origin
    private val _after: String = nativeEvent.new
    
    
    /**
     * 操作人. 可能是群主. 可能与 [member] 引用相同, 此时为群员自己修改.
     * 可能是机器人的操作.
     */
    override val operator: MiraiMember = when {
        nativeEvent.operator === nativeEvent.member -> memberInternal
        else -> nativeEvent.operatorOrBot.asSimbot(bot, groupInternal)
    }
    
    override suspend fun before() = _before
    override suspend fun after() = _after
}

internal class MiraiMemberCardChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberCardChangeEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberCardChangeEvent>(bot, nativeEvent), MiraiMemberCardChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    private val _before: String = nativeEvent.origin
    private val _after: String = nativeEvent.new
    
    override suspend fun before() = _before
    override suspend fun after() = _after
}

internal class MiraiMemberJoinRequestEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiMemberJoinRequestEvent,
) : MiraiMemberJoinRequestEvent {
    override val id: ID = originalEvent.eventId.ID
    override val timestamp: Timestamp = Timestamp.now()
    override val type: RequestEvent.Type =
        if (originalEvent.invitorId == null) RequestEvent.Type.APPLICATION
        else RequestEvent.Type.INVITATION
    
    private val _group = originalEvent.group?.asSimbot(bot)
        ?: GroupInfoImpl(originalEvent.groupId, originalEvent.groupName)
    
    private val _inviter: RequestMemberInviterInfo? = originalEvent.invitorId?.let { id ->
        RequestMemberInviterInfo(id.ID, originalEvent.invitor?.let { m ->
            if (_group is MiraiGroupImpl) m.asSimbot(bot, _group) else m.asSimbot(bot)
        })
    }
    override val message: String = originalEvent.message
    
    private val _requester: RequestMemberInfo = RequestMemberInfo(
        originalEvent.fromId.ID, originalEvent.groupId,
        originalEvent.groupName, originalEvent.fromNick
    )
    
    
    override suspend fun inviter() = _inviter
    override suspend fun requester() = _requester
    override suspend fun group(): GroupInfo = _group
    
    
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
    
    
    /** 接受申请 */
    @JvmSynthetic
    override suspend fun accept(): Boolean {
        originalEvent.accept()
        return true
    }
    
    /** 拒绝申请 */
    override suspend fun reject(): Boolean = reject(false, "")
    
    /**
     * 拒绝申请。
     * @param blockList 添加到黑名单
     * @param message 拒绝原因
     */
    override suspend fun reject(blockList: Boolean, message: String): Boolean {
        originalEvent.reject(blockList, message)
        return true
    }
}


internal class MiraiMemberLeaveEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberLeaveEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberLeaveEvent>(bot, nativeEvent), MiraiMemberLeaveEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val actionType: ActionType =
        if (nativeEvent is OriginalMiraiMemberLeaveEvent.Kick) ActionType.PASSIVE else ActionType.PROACTIVE
    
    @OptIn(Api4J::class)
    override val operator: MiraiMember =
        if (nativeEvent is OriginalMiraiMemberLeaveEvent.Kick) nativeEvent.operatorOrBot.asSimbot(bot, groupInternal)
        else memberInternal
    
    override suspend fun operator() = operator
}

internal class MiraiMemberJoinEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberJoinEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberJoinEvent>(bot, nativeEvent), MiraiMemberJoinEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val actionType: ActionType =
        if (nativeEvent is OriginalMiraiMemberJoinEvent.Invite) ActionType.PASSIVE else ActionType.PROACTIVE
    override val inviter: MiraiMember? =
        if (nativeEvent is OriginalMiraiMemberJoinEvent.Invite) nativeEvent.invitor.asSimbot(
            bot,
            groupInternal
        ) else null
    
}
