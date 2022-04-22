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
 */

package love.forte.simbot.component.mirai.event.impl

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.action.ActionType
import love.forte.simbot.component.mirai.MiraiGroup
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
    final override val group = originalEvent.group.asSimbot(bot)
    override val member = originalEvent.member.asSimbot(bot, group)

    override val user get() = member
    override val organization get() = group

    override suspend fun member() = member
    override suspend fun group() = group
    override suspend fun user() = user
    override suspend fun organization() = organization
}


internal class MiraiGroupTalkativeChangeEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiGroupTalkativeChangeEvent,
) : MiraiGroupTalkativeChangeEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val group = originalEvent.group.asSimbot(bot)
    override val before = originalEvent.previous.asSimbot(bot, group)
    override val after = originalEvent.now.asSimbot(bot, group)


    override val member get() = after
    override val user get() = after
    override val source get() = group
    override val organization get() = group

    override suspend fun member() = member
    override suspend fun user() = user
    override suspend fun group() = group
    override suspend fun before() = before
    override suspend fun after() = after
    override suspend fun source() = source
    override suspend fun organization() = organization
}

@OptIn(MiraiExperimentalApi::class)
internal class MiraiMemberHonorChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberHonorChangeEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberHonorChangeEvent>(bot, nativeEvent), MiraiMemberHonorChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val honorType: GroupHonorType = nativeEvent.honorType
    override val after: GroupHonorType? =
        if (nativeEvent is OriginalMiraiMemberHonorChangeEvent.Achieve) honorType else null
    override val before: GroupHonorType? =
        if (nativeEvent is OriginalMiraiMemberHonorChangeEvent.Lose) honorType else null

    override val source get() = member
    override suspend fun source() = source
}


internal class MiraiMemberUnmuteEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberUnmuteEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberUnmuteEvent>(bot, nativeEvent), MiraiMemberUnmuteEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val source get() = member
    override suspend fun source() = source
}

internal class MiraiMemberMuteEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberMuteEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberMuteEvent>(bot, nativeEvent), MiraiMemberMuteEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val durationSeconds: Int = nativeEvent.durationSeconds
    override val duration: Duration get() = durationSeconds.seconds
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, group)
    override val source get() = member
    override suspend fun source() = source
}

internal class MiraiMemberRoleChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberPermissionChangeEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberPermissionChangeEvent>(bot, nativeEvent),
    MiraiMemberRoleChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val before = nativeEvent.origin.simbotRole
    override val after = nativeEvent.new.simbotRole
    override val source get() = member

    override suspend fun before() = before
    override suspend fun after() = after
    override suspend fun source() = source
}

internal class MiraiMemberSpecialTitleChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberSpecialTitleChangeEvent,
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
    override val source get() = member

    override suspend fun before() = before
    override suspend fun after() = after
    override suspend fun source() = source
}

internal class MiraiMemberCardChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiMemberCardChangeEvent,
) : BaseMiraiGroupMemberEvent<OriginalMiraiMemberCardChangeEvent>(bot, nativeEvent), MiraiMemberCardChangeEvent {
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp get() = changedTime
    override val before: String = nativeEvent.origin
    override val after: String = nativeEvent.new
    override val source: MiraiMember get() = member

    override suspend fun before() = before
    override suspend fun after() = after
    override suspend fun source() = source
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

    override val user: RequestMemberInfo get() = requester

    override suspend fun inviter() = inviter
    override suspend fun requester() = requester
    override suspend fun user() = user


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


    //region 处理API
    //region 接受申请
    /** 接受申请 */
    @JvmSynthetic
    override suspend fun accept(): Boolean {
        originalEvent.accept()
        return true
    }

    //endregion

    //region 拒绝申请
    /** 拒绝申请 */
    @JvmSynthetic
    override suspend fun reject(): Boolean = reject(false, "")

    /**
     * 拒绝申请。
     * @param blockList 添加到黑名单
     * @param message 拒绝原因
     */
    @JvmSynthetic
    override suspend fun reject(blockList: Boolean, message: String): Boolean {
        originalEvent.reject(blockList, message)
        return true
    }

    @Api4J
    override fun rejectBlocking(blockList: Boolean, message: String): Boolean =
        runBlocking { reject(blockList, message) }

    @Api4J
    override fun rejectBlocking(blockList: Boolean): Boolean = runBlocking { reject(blockList, "") }

    @Api4J
    override fun rejectBlocking(message: String): Boolean = runBlocking { reject(false, "") }

    @Api4J
    override fun rejectAsync(blockList: Boolean, message: String) {
        bot.launch { reject(blockList, message) }
    }

    @Api4J
    override fun rejectAsync(blockList: Boolean) {
        bot.launch { reject(blockList, "") }
    }

    @Api4J
    override fun rejectAsync(message: String) {
        bot.launch { reject(false, "") }
    }
    //endregion
    //endregion


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
    @Suppress("UnnecessaryOptInAnnotation")
    override val operator: MiraiMember =
        if (nativeEvent is OriginalMiraiMemberLeaveEvent.Kick) nativeEvent.operatorOrBot.asSimbot(bot, group)
        else member

    override val source: MiraiGroup get() = group

    @OptIn(Api4J::class)
    @Suppress("UnnecessaryOptInAnnotation")
    override val before: MiraiMember get() = member

    override suspend fun source() = source
    override suspend fun before() = before
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
        if (nativeEvent is OriginalMiraiMemberJoinEvent.Invite) nativeEvent.invitor.asSimbot(bot, group) else null


    override val source: MiraiGroup get() = group

    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(Api4J::class)
    override val after: MiraiMember get() = member

    override suspend fun source() = source
    override suspend fun after() = after
}
