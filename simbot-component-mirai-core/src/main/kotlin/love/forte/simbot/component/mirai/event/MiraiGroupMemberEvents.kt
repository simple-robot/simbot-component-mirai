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

package love.forte.simbot.component.mirai.event

import kotlinx.coroutines.*
import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.definition.GroupInfo
import love.forte.simbot.event.*
import love.forte.simbot.message.*
import net.mamoe.mirai.data.*
import net.mamoe.mirai.utils.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

//region typealias
/**
 * @see net.mamoe.mirai.event.events.GroupMemberEvent
 */
public typealias NativeMiraiGroupMemberEvent = net.mamoe.mirai.event.events.GroupMemberEvent

/**
 * @see net.mamoe.mirai.event.events.GroupTalkativeChangeEvent
 */
public typealias NativeMiraiGroupTalkativeChangeEvent = net.mamoe.mirai.event.events.GroupTalkativeChangeEvent
/**
 * @see net.mamoe.mirai.event.events.MemberHonorChangeEvent
 */
@MiraiExperimentalApi
public typealias NativeMiraiMemberHonorChangeEvent = net.mamoe.mirai.event.events.MemberHonorChangeEvent
/**
 * @see net.mamoe.mirai.event.events.MemberUnmuteEvent
 */
public typealias NativeMiraiMemberUnmuteEvent = net.mamoe.mirai.event.events.MemberUnmuteEvent
/**
 * @see net.mamoe.mirai.event.events.MemberMuteEvent
 */
public typealias NativeMiraiMemberMuteEvent = net.mamoe.mirai.event.events.MemberMuteEvent
/**
 * @see net.mamoe.mirai.event.events.MemberPermissionChangeEvent
 */
public typealias NativeMiraiMemberPermissionChangeEvent = net.mamoe.mirai.event.events.MemberPermissionChangeEvent
/**
 * @see net.mamoe.mirai.event.events.MemberSpecialTitleChangeEvent
 */
public typealias NativeMiraiMemberSpecialTitleChangeEvent = net.mamoe.mirai.event.events.MemberSpecialTitleChangeEvent
/**
 * @see net.mamoe.mirai.event.events.MemberCardChangeEvent
 */
public typealias NativeMiraiMemberCardChangeEvent = net.mamoe.mirai.event.events.MemberCardChangeEvent
/**
 * @see net.mamoe.mirai.event.events.MemberJoinRequestEvent
 */
public typealias NativeMiraiMemberJoinRequestEvent = net.mamoe.mirai.event.events.MemberJoinRequestEvent
/**
 * @see net.mamoe.mirai.event.events.MemberLeaveEvent
 */
public typealias NativeMiraiMemberLeaveEvent = net.mamoe.mirai.event.events.MemberLeaveEvent
/**
 * @see net.mamoe.mirai.event.events.MemberJoinEvent
 */
public typealias NativeMiraiMemberJoinEvent = net.mamoe.mirai.event.events.MemberJoinEvent
//endregion


/**
 * 与 **群成员** 相关的mirai事件类型。
 *
 * 参考 `net.mamoe.mirai.event.events.group.kt`。
 */
public interface MiraiGroupMemberEvent<E : NativeMiraiGroupMemberEvent> : MiraiSimbotBotEvent<E>, GroupEvent,
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
 * @see NativeMiraiGroupTalkativeChangeEvent
 * @see MemberChangedEvent
 */
public interface MiraiGroupTalkativeChangeEvent : MiraiSimbotBotEvent<NativeMiraiGroupTalkativeChangeEvent>,
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
 * @see NativeMiraiMemberHonorChangeEvent
 */
@MiraiExperimentalApi
public interface MiraiMemberHonorChangeEvent : MiraiGroupMemberEvent<NativeMiraiMemberHonorChangeEvent>,
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
public interface MiraiMemberMuteRelateEvent<E : NativeMiraiGroupMemberEvent> : MiraiGroupMemberEvent<E>,
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
 * @see NativeMiraiMemberUnmuteEvent
 * @see MiraiBotUnmuteEvent
 */
public interface MiraiMemberUnmuteEvent : MiraiMemberMuteRelateEvent<NativeMiraiMemberUnmuteEvent> {

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
 * @see NativeMiraiMemberMuteEvent
 */
public interface MiraiMemberMuteEvent : MiraiMemberMuteRelateEvent<NativeMiraiMemberMuteEvent> {

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
 * @see NativeMiraiMemberPermissionChangeEvent
 * @see MiraiBotGroupRoleChangeEvent
 */
public interface MiraiMemberRoleChangeEvent : MiraiGroupMemberEvent<NativeMiraiMemberPermissionChangeEvent>,
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
 * @see NativeMiraiMemberSpecialTitleChangeEvent
 */
public interface MiraiMemberSpecialTitleChangeEvent : MiraiGroupMemberEvent<NativeMiraiMemberSpecialTitleChangeEvent>,
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
 * @see NativeMiraiMemberCardChangeEvent
 */
public interface MiraiMemberCardChangeEvent : MiraiGroupMemberEvent<NativeMiraiMemberCardChangeEvent>,
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
 * @see NativeMiraiMemberJoinRequestEvent
 */
public interface MiraiMemberJoinRequestEvent :
    MiraiSimbotBotEvent<NativeMiraiMemberJoinRequestEvent>,
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
        nativeEvent.accept()
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
        nativeEvent.reject(blockList, message)
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
 * @see NativeMiraiMemberLeaveEvent
 */
public interface MiraiMemberLeaveEvent :
    MiraiGroupMemberEvent<NativeMiraiMemberLeaveEvent>,
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
 * @see NativeMiraiMemberJoinEvent
 */
public interface MiraiMemberJoinEvent :
    MiraiGroupMemberEvent<NativeMiraiMemberJoinEvent>,
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