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

package love.forte.simbot.component.mirai.event

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import love.forte.simbot.Api4J
import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.component.mirai.MemberRole
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.definition.GroupInfo
import love.forte.simbot.event.*
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.data.GroupHonorType
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


/**
 * 与 **群成员** 相关的mirai事件类型。
 *
 * 参考 `net.mamoe.mirai.event.events.group.kt`。
 */
public interface MiraiGroupMemberEvent<E : OriginalMiraiGroupMemberEvent> : MiraiSimbotBotEvent<E>, GroupEvent,
    MemberEvent {


    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val member: MiraiMember

    @OptIn(Api4J::class)
    override val group: MiraiGroup

    //// Impl


    @OptIn(Api4J::class)
    override val user: MiraiMember
        get() = member

    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group

    @JvmSynthetic
    override suspend fun member(): MiraiMember = member

    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun user(): MiraiMember = member

    public companion object Key : BaseEventKey<MiraiGroupMemberEvent<*>>(
        "mirai.group_member", MiraiSimbotBotEvent, GroupEvent, MemberEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupMemberEvent<*>? = doSafeCast(value)
    }
}


/**
 * Group 龙王改变时的事件. 此事件属于一种 [成员变动事件][MemberChangedEvent]。
 * @see OriginalMiraiGroupTalkativeChangeEvent
 * @see MemberChangedEvent
 */
public interface MiraiGroupTalkativeChangeEvent : MiraiSimbotBotEvent<OriginalMiraiGroupTalkativeChangeEvent>,
    MemberChangedEvent<MiraiMember, MiraiMember>, GroupEvent {

    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val group: MiraiGroup
    override val before: MiraiMember
    override val after: MiraiMember
    //// Impl

    override val source: MiraiGroup get() = group

    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group

    @OptIn(Api4J::class)
    override val operator: MiraiMember
        get() = after

    @JvmSynthetic
    override suspend fun after(): MiraiMember = after

    @JvmSynthetic
    override suspend fun before(): MiraiMember = before

    @JvmSynthetic
    override suspend fun source(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun operator(): MiraiMember = after

    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC

    override val key: Event.Key<MiraiGroupTalkativeChangeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiGroupTalkativeChangeEvent>(
        "mirai.group_talkative_change", MiraiSimbotBotEvent, MemberChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupTalkativeChangeEvent? = doSafeCast(value)
    }
}

/**
 * Member 荣誉改变时的事件, 目前只支持龙王.
 * 此属于一种 [变更事件][ChangedEvent], 变更源为一个群成员，变更前后为荣耀类型。
 * 理论上来讲，[before] 与 [after] 不会同时为null，当 [before] 为null时，代表对应成员得到了此荣耀，
 * 相反，如果 [before] 不为null但是 [after] 为null的时候，代表对应成员失去了此荣耀。
 *
 * @see OriginalMiraiMemberHonorChangeEvent
 */
@MiraiExperimentalApi
public interface MiraiMemberHonorChangeEvent : MiraiGroupMemberEvent<OriginalMiraiMemberHonorChangeEvent>,
    ChangedEvent<MiraiMember, GroupHonorType?, GroupHonorType?> {

    override val bot: MiraiBot
    override val group: MiraiGroup
    override val member: MiraiMember
    public val honorType: GroupHonorType
    override val after: GroupHonorType?
    override val before: GroupHonorType?

    //// Impl

    override val source: MiraiMember get() = member

    @JvmSynthetic
    override suspend fun source(): MiraiMember = member

    @JvmSynthetic
    override suspend fun after(): GroupHonorType? = after

    @JvmSynthetic
    override suspend fun before(): GroupHonorType? = before
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC

    override val key: Event.Key<out MiraiMemberHonorChangeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiMemberHonorChangeEvent>(
        "mirai.member_honor_change", MiraiGroupMemberEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberHonorChangeEvent? = doSafeCast(value)
    }

}

/**
 * 群成员禁言相关事件。
 *
 * 与bot相关的禁言事件可以参考 [MiraiBotMuteRelateEvent]
 *
 * @see MiraiMemberUnmuteEvent
 * @see MiraiMemberMuteEvent
 */
public interface MiraiMemberMuteRelateEvent<E : OriginalMiraiGroupMemberEvent> : MiraiGroupMemberEvent<E>,
    ChangedEvent<MiraiMember, Boolean, Boolean> {

    override val bot: MiraiBot
    override val member: MiraiMember
    override val group: MiraiGroup
    override val before: Boolean
    override val after: Boolean

    /**
     * 剩余禁言时间的时长。
     */
    public val duration: Duration

    /**
     * @see duration
     */
    public val durationSeconds: Int


    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC
    //// Impl

    @JvmSynthetic
    override suspend fun before(): Boolean = before

    @JvmSynthetic
    override suspend fun after(): Boolean = after

    @JvmSynthetic
    override suspend fun source(): MiraiMember = member
    override val source: MiraiMember get() = member

    override val key: Event.Key<out MiraiMemberMuteRelateEvent<*>>

    public companion object Key : BaseEventKey<MiraiMemberMuteRelateEvent<*>>(
        "mirai.member_mute_relate", MiraiGroupMemberEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberMuteRelateEvent<*>? = doSafeCast(value)
    }
}

/**
 * 群成员被取消禁言事件. 被禁言的成员不可能是机器人本人。
 *
 * 此事件属于 [已变更事件][ChangedEvent], [before] 永远为 `true`, [after] 永远为 `false`.
 *
 *
 * 对于bot被取消禁言事件可以参考 [MiraiBotUnmuteEvent].
 *
 * @see OriginalMiraiMemberUnmuteEvent
 * @see MiraiBotUnmuteEvent
 */
public interface MiraiMemberUnmuteEvent : MiraiMemberMuteRelateEvent<OriginalMiraiMemberUnmuteEvent> {

    override val bot: MiraiBot
    override val member: MiraiMember
    override val group: MiraiGroup
    override val duration: Duration get() = 0.seconds
    override val durationSeconds: Int get() = 0

    //// Impl

    override val before: Boolean get() = true
    override val after: Boolean get() = false


    override val key: Event.Key<MiraiMemberUnmuteEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiMemberUnmuteEvent>(
        "mirai.member_unmute", MiraiMemberMuteRelateEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberUnmuteEvent? = doSafeCast(value)
    }

}

/**
 * 群成员被禁言事件. 被禁言的成员都不可能是机器人本人.
 *
 *
 * 机器人禁言事件可以参考 [MiraiBotMuteEvent].
 *
 * @see OriginalMiraiMemberMuteEvent
 */
public interface MiraiMemberMuteEvent : MiraiMemberMuteRelateEvent<OriginalMiraiMemberMuteEvent> {

    override val bot: MiraiBot
    override val member: MiraiMember
    override val group: MiraiGroup
    override val duration: Duration
    override val durationSeconds: Int

    /**
     * 操作人可能会是bot自己。
     */
    public val operator: MiraiMember
    //// Impl

    override val before: Boolean get() = false
    override val after: Boolean get() = true

    override val key: Event.Key<MiraiMemberMuteEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiMemberMuteEvent>(
        "mirai.member_mute", MiraiMemberMuteRelateEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberMuteEvent? = doSafeCast(value)
    }

}

/**
 * 成员权限改变的事件. 成员不可能是机器人自己.
 *
 * 有关bot权限变更的事件可以参考 [MiraiBotGroupRoleChangeEvent].
 *
 * @see OriginalMiraiMemberPermissionChangeEvent
 * @see MiraiBotGroupRoleChangeEvent
 */
public interface MiraiMemberRoleChangeEvent : MiraiGroupMemberEvent<OriginalMiraiMemberPermissionChangeEvent>,
    ChangedEvent<MiraiMember, MemberRole, MemberRole> {

    override val bot: MiraiBot
    override val member: MiraiMember
    override val group: MiraiGroup
    override val before: MemberRole
    override val after: MemberRole

    // Impl

    override val source: MiraiMember get() = member
    override val organization: MiraiGroup get() = group

    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun after(): MemberRole = after

    @JvmSynthetic
    override suspend fun before(): MemberRole = before

    @JvmSynthetic
    override suspend fun source(): MiraiMember = member
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC

    override val key: Event.Key<MiraiMemberRoleChangeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiMemberRoleChangeEvent>(
        "mirai.member_role_change", MiraiGroupMemberEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberRoleChangeEvent? = doSafeCast(value)
    }
}

/**
 * 成员群特殊头衔改动. 一定为群主操作.
 *
 * > 由于服务器并不会告知特殊头衔的重置, 因此此事件在特殊头衔重置后只能由 mirai 在发现变动时才广播.
 *
 * @see OriginalMiraiMemberSpecialTitleChangeEvent
 */
public interface MiraiMemberSpecialTitleChangeEvent : MiraiGroupMemberEvent<OriginalMiraiMemberSpecialTitleChangeEvent>,
    ChangedEvent<MiraiMember, String, String> {
    override val bot: MiraiBot
    override val group: MiraiGroup
    override val member: MiraiMember
    override val before: String
    override val after: String

    /**
     * 操作人. 可能是群主. 可能与 [member] 引用相同, 此时为群员自己修改.
     * 可能是机器人的操作.
     */
    public val operator: MiraiMember

    //// Impl
    override val source: MiraiMember get() = member

    @JvmSynthetic
    override suspend fun after(): String = after

    @JvmSynthetic
    override suspend fun before(): String = before

    @JvmSynthetic
    override suspend fun source(): MiraiMember = member

    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.INTERNAL
    override val key: Event.Key<MiraiMemberSpecialTitleChangeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiMemberSpecialTitleChangeEvent>(
        "mirai.member_special_title_change", MiraiGroupMemberEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberSpecialTitleChangeEvent? = doSafeCast(value)
    }
}

/**
 * 成员群名片改动. 此事件广播前修改就已经完成.
 *
 * > 由于服务器并不会告知名片变动, 此事件只能由 mirai 在发现变动时才广播. 不要依赖于这个事件.
 * @see OriginalMiraiMemberCardChangeEvent
 */
public interface MiraiMemberCardChangeEvent : MiraiGroupMemberEvent<OriginalMiraiMemberCardChangeEvent>,
    ChangedEvent<MiraiMember, String, String> {
    override val bot: MiraiBot
    override val group: MiraiGroup
    override val member: MiraiMember
    override val before: String
    override val after: String

    //// Impl
    override val source: MiraiMember get() = member

    @JvmSynthetic
    override suspend fun after(): String = after

    @JvmSynthetic
    override suspend fun before(): String = before

    @JvmSynthetic
    override suspend fun source(): MiraiMember = member
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.INTERNAL
    override val key: Event.Key<MiraiMemberCardChangeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiMemberCardChangeEvent>(
        "mirai.member_card_change", MiraiGroupMemberEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberCardChangeEvent? = doSafeCast(value)
    }
}

/**
 * 一个账号请求加入群事件, Bot 在此群中是管理员或群主.
 * @see OriginalMiraiMemberJoinRequestEvent
 */
public interface MiraiMemberJoinRequestEvent :
    MiraiSimbotBotEvent<OriginalMiraiMemberJoinRequestEvent>,
    GroupJoinRequestEvent {
    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val group: GroupInfo


    /**
     * 邀请者信息。[inviter] 中的信息不保证是完全的。
     *
     * @see RequestMemberInviterInfo
     */
    override val inviter: RequestMemberInviterInfo?
    override val message: String

    /**
     * 申请者信息。
     */
    @OptIn(Api4J::class)
    override val requester: RequestMemberInfo


    //// Impl

    @JvmSynthetic
    override suspend fun group(): GroupInfo = group

    @JvmSynthetic
    override suspend fun requester(): RequestMemberInfo = requester

    @JvmSynthetic
    override suspend fun inviter(): RequestMemberInviterInfo? = inviter

    @JvmSynthetic
    override suspend fun user(): RequestMemberInfo = requester

    @OptIn(Api4J::class)
    override val user: RequestMemberInfo
        get() = requester

    /** 接收申请 */
    @ExperimentalSimbotApi
    @JvmSynthetic
    override suspend fun accept(): Boolean {
        originalEvent.accept()
        return true
    }


    /** 拒绝申请 */
    @ExperimentalSimbotApi
    @JvmSynthetic
    override suspend fun reject(): Boolean = reject(false, "")

    /**
     * 拒绝申请。
     * @param blockList 添加到黑名单
     * @param message 拒绝原因
     */
    @JvmSynthetic
    public suspend fun reject(blockList: Boolean, message: String): Boolean {
        originalEvent.reject(blockList, message)
        return true
    }

    @Api4J
    public fun rejectBlocking(blockList: Boolean, message: String): Boolean = runBlocking { reject(blockList, message) }

    @Api4J
    public fun rejectBlocking(blockList: Boolean): Boolean = runBlocking { reject(blockList, "") }

    @Api4J
    public fun rejectBlocking(message: String): Boolean = runBlocking { reject(false, "") }

    @Api4J
    public fun rejectAsync(blockList: Boolean, message: String) {
        bot.launch { reject(blockList, message) }
    }

    @Api4J
    public fun rejectAsync(blockList: Boolean) {
        bot.launch { reject(blockList, "") }
    }

    @Api4J
    public fun rejectAsync(message: String) {
        bot.launch { reject(false, "") }
    }


    override val key: Event.Key<MiraiMemberJoinRequestEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiMemberJoinRequestEvent>(
        "mirai.member_join_request", MiraiSimbotBotEvent, GroupJoinRequestEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberJoinRequestEvent? = doSafeCast(value)
    }
}

/**
 * 成员**已经**离开群的事件.
 *
 * @see OriginalMiraiMemberLeaveEvent
 */
public interface MiraiMemberLeaveEvent :
    MiraiGroupMemberEvent<OriginalMiraiMemberLeaveEvent>,
    MemberDecreaseEvent {
    override val bot: MiraiBot
    override val member: MiraiMember
    override val group: MiraiGroup

    /**
     * 如果是成员自己退出，则 [operator] === [member]
     */
    @OptIn(Api4J::class)
    override val operator: MiraiMember

    //// Impl

    override val source: MiraiGroup get() = group
    override val before: MiraiMember get() = member
    override val target: MiraiMember get() = member
    override val after: MiraiMember? get() = null
    override val user: MiraiMember get() = member

    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group

    @JvmSynthetic
    override suspend fun before(): MiraiMember = before

    @JvmSynthetic
    override suspend fun after(): MiraiMember? = null

    @JvmSynthetic
    override suspend fun operator(): MiraiMember = operator

    @JvmSynthetic
    override suspend fun source(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun target(): MiraiMember = member

    @JvmSynthetic
    override suspend fun member(): MiraiMember = member

    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun user(): MiraiMember = member

    override val key: Event.Key<MiraiMemberLeaveEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiMemberLeaveEvent>(
        "mirai.member_leave", MiraiGroupMemberEvent, MemberDecreaseEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberLeaveEvent? = doSafeCast(value)
    }
}

/**
 * 成员已经加入群的事件.
 *
 * @see OriginalMiraiMemberJoinEvent
 */
public interface MiraiMemberJoinEvent :
    MiraiGroupMemberEvent<OriginalMiraiMemberJoinEvent>,
    MemberIncreaseEvent {
    override val bot: MiraiBot
    override val member: MiraiMember
    override val group: MiraiGroup

    /**
     * 如果成员是被某个人邀请进入、不需要验证并且支持获取邀请者的情况下，
     * [inviter] 不为null。
     */
    public val inviter: MiraiMember?


    //// Impl

    /**
     * 无法得知操作者。
     * 如果你希望得到 "邀请者"，参考 [inviter].
     */
    @OptIn(Api4J::class)
    override val operator: MiraiMember?
        get() = null
    override val source: MiraiGroup get() = group
    override val after: MiraiMember get() = member
    override val before: MiraiMember? get() = null
    override val target: MiraiMember get() = member
    override val user: MiraiMember get() = member

    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group

    @JvmSynthetic
    override suspend fun before(): MiraiMember? = null

    @JvmSynthetic
    override suspend fun after(): MiraiMember = after

    @JvmSynthetic
    override suspend fun operator(): MiraiMember? = null

    @JvmSynthetic
    override suspend fun source(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun target(): MiraiMember = member

    @JvmSynthetic
    override suspend fun member(): MiraiMember = member

    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun user(): MiraiMember = member


    override val key: Event.Key<MiraiMemberJoinEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiMemberJoinEvent>(
        "mirai.member_join", MiraiGroupMemberEvent, MemberIncreaseEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberJoinEvent? = doSafeCast(value)
    }
}