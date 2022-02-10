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

import love.forte.simbot.Api4J
import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.ID
import love.forte.simbot.action.ActionType
import love.forte.simbot.component.mirai.*
import love.forte.simbot.definition.GroupInfo
import love.forte.simbot.definition.Organization
import love.forte.simbot.definition.UserInfo
import love.forte.simbot.event.*
import love.forte.simbot.message.doSafeCast
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

//region typealias
/**
 * @see net.mamoe.mirai.event.events.BotLeaveEvent
 */
public typealias NativeMiraiBotLeaveEvent = net.mamoe.mirai.event.events.BotLeaveEvent
/**
 * @see net.mamoe.mirai.event.events.BotGroupPermissionChangeEvent
 */
public typealias NativeMiraiBotGroupPermissionChangeEvent = net.mamoe.mirai.event.events.BotGroupPermissionChangeEvent
/**
 * @see net.mamoe.mirai.event.events.BotMuteEvent
 */
public typealias NativeMiraiBotMuteEvent = net.mamoe.mirai.event.events.BotMuteEvent
/**
 * @see net.mamoe.mirai.event.events.BotUnmuteEvent
 */
public typealias NativeMiraiBotUnmuteEvent = net.mamoe.mirai.event.events.BotUnmuteEvent
/**
 * @see net.mamoe.mirai.event.events.BotJoinGroupEvent
 */
public typealias NativeMiraiBotJoinGroupEvent = net.mamoe.mirai.event.events.BotJoinGroupEvent

/**
 * @see net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
 */
public typealias NativeMiraiBotInvitedJoinGroupRequestEvent = net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
//endregion


/**
 * simbot中与 [NativeMiraiGroupEvent] 相关的事件中，与 bot 相关的事件。
 *
 * 一般代表了在 `net.mamoe.mirai.event.events.group.kt` 中与 bot 有直接关系的事件，比较简单的判断标准为这个mirai事件是否为 `Bot` 开头的。
 *
 *
 *
 * @see MiraiBotLeaveEvent
 * @see MiraiBotGroupPermissionChangeEvent
 * @see MiraiBotMuteEvent
 * @see MiraiBotUnmuteEvent
 * @see MiraiBotJoinGroupEvent
 * @see MiraiBotInvitedJoinGroupRequestEvent
 */
public interface MiraiGroupBotEvent<E : NativeMiraiGroupEvent> :
    MiraiSimbotBotEvent<E>, GroupEvent {

    override val key: Event.Key<out MiraiGroupBotEvent<*>>
    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val group: MiraiGroup


    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group

    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group
    @JvmSynthetic
    override suspend fun organization(): Organization = group

    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PRIVATE

    public companion object Key : BaseEventKey<MiraiGroupBotEvent<*>>(
        "mirai.group_bot", MiraiSimbotBotEvent, GroupEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupBotEvent<*>? = doSafeCast(value)
    }
}


/**
 * 机器人被踢出群或在其他客户端主动退出一个群.
 * 在事件广播前 Bot.groups 就已删除这个群.
 *
 * 此事件属于一个 [群成员减少事件][MemberDecreaseEvent], 减少的这个成员便是bot自己。
 *
 * @see NativeMiraiBotLeaveEvent
 * @see MemberDecreaseEvent
 */
public interface MiraiBotLeaveEvent :
    MiraiGroupBotEvent<NativeMiraiBotLeaveEvent>,
    MemberDecreaseEvent {
    override val bot: MiraiBot
    override val group: MiraiGroup
    override val target: MiraiMember

    @OptIn(Api4J::class)
    override val operator: MiraiMember?
    override val actionType: ActionType

    // Impl

    override val source: MiraiGroup get() = group
    override val before: MiraiMember get() = target
    override val after: MiraiMember? get() = null

    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group

    @JvmSynthetic
    override suspend fun target(): MiraiMember = target
    @JvmSynthetic
    override suspend fun operator(): MiraiMember? = operator
    @JvmSynthetic
    override suspend fun source(): MiraiGroup = group
    @JvmSynthetic
    override suspend fun before(): MiraiMember = target
    @JvmSynthetic
    override suspend fun after(): MiraiMember? = null
    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group
    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group

    override val key: Event.Key<MiraiBotLeaveEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiBotLeaveEvent>(
        "mirai.bot_leave", MiraiGroupBotEvent, MemberDecreaseEvent
    ) {
        override fun safeCast(value: Any): MiraiBotLeaveEvent? = doSafeCast(value)
    }
}

/**
 * Bot 在群里的权限被改变。
 * 操作人一定是群主。
 *
 * 此事件属于一个 [已变动事件][ChangedEvent], [变动源][source] 即当前bot，
 * 变动前后为bot在群里的权限。
 *
 * @see NativeMiraiBotGroupPermissionChangeEvent
 */
public interface MiraiBotGroupPermissionChangeEvent :
    MiraiGroupBotEvent<NativeMiraiBotGroupPermissionChangeEvent>,
    ChangedEvent<MiraiBot, MemberPermission, MemberPermission> {
    override val bot: MiraiBot
    override val group: MiraiGroup
    override val before: MemberPermission
    override val after: MemberPermission

    // Impl

    override val source: MiraiBot get() = bot
    override val organization: MiraiGroup get() = group
    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group
    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group
    @JvmSynthetic
    override suspend fun after(): MemberPermission = after
    @JvmSynthetic
    override suspend fun before(): MemberPermission = before
    @JvmSynthetic
    override suspend fun source(): MiraiBot = bot

    override val key: Event.Key<MiraiBotGroupPermissionChangeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiBotGroupPermissionChangeEvent>(
        "mirai.bot_group_permission_change", MiraiGroupBotEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiBotGroupPermissionChangeEvent? = doSafeCast(value)
    }
}


/**
 * 与bot禁言有关系的相关事件。
 *
 * 属于一个变化事件 [ChangedEvent], 其类型代表事件前后的禁言状态。
 *
 * @see MiraiBotMuteEvent
 * @see MiraiBotUnmuteEvent
 */
public sealed interface MiraiBotMuteRelateEvent<E : NativeMiraiGroupEvent> :
    MiraiGroupBotEvent<E>,
    ChangedEvent<MiraiGroup, Boolean, Boolean> {

    override val bot: MiraiBot
    override val group: MiraiGroup

    /**
     * 剩余禁言时间的时长。
     */
    public val duration: Duration

    /**
     * @see duration
     */
    public val durationSeconds: Int

    /**
     * 操作人。
     */
    public val operator: MiraiMember

    //// Impl

    override val source: MiraiGroup get() = group
    @JvmSynthetic
    override suspend fun after(): Boolean = after
    @JvmSynthetic
    override suspend fun before(): Boolean = before
    @JvmSynthetic
    override suspend fun source(): MiraiGroup = source
    override val organization: MiraiGroup get() = group
    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group
    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group

    override val visibleScope: Event.VisibleScope
        get() = Event.VisibleScope.PRIVATE


    public companion object Key : BaseEventKey<MiraiBotMuteRelateEvent<*>>(
        "mirai.bot_mute_relate", MiraiGroupBotEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiBotMuteRelateEvent<*>? = doSafeCast(value)
    }
}

/**
 * Bot 被禁言.
 *
 * 属于一个变化事件 [ChangedEvent], 其类型代表事件前后的禁言状态。
 * [before] 永远为false，[after] 永远为true。
 *
 * 额外提供了 [duration] 属性来代表禁言的持续时间。
 *
 * @see NativeMiraiBotMuteEvent
 */
public interface MiraiBotMuteEvent :
    MiraiGroupBotEvent<NativeMiraiBotMuteEvent>, MiraiBotMuteRelateEvent<NativeMiraiBotMuteEvent> {
    override val duration: Duration
    override val durationSeconds: Int
    //// Impl

    override val before: Boolean get() = false
    override val after: Boolean get() = true

    override val key: Event.Key<MiraiBotMuteEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiBotMuteEvent>(
        "mirai.bot_mute", MiraiGroupBotEvent, MiraiBotMuteRelateEvent
    ) {
        override fun safeCast(value: Any): MiraiBotMuteEvent? = doSafeCast(value)
    }
}

/**
 * Bot 被取消禁言事件。
 *
 * 属于一个变化事件 [ChangedEvent], 其类型代表事件前后的禁言状态。
 * [before] 永远为true，[after] 永远为false。
 * @see NativeMiraiBotUnmuteEvent
 */
public interface MiraiBotUnmuteEvent :
    MiraiGroupBotEvent<NativeMiraiBotUnmuteEvent>,
    MiraiBotMuteRelateEvent<NativeMiraiBotUnmuteEvent> {
    override val duration: Duration get() = 0.seconds
    override val durationSeconds: Int get() = 0

    override val bot: MiraiBot
    override val group: MiraiGroup

    //// Impl

    override val before: Boolean get() = true
    override val after: Boolean get() = false

    override val key: Event.Key<MiraiBotUnmuteEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiBotUnmuteEvent>(
        "mirai.bot_unmute", MiraiGroupBotEvent, MiraiBotMuteRelateEvent
    ) {
        override fun safeCast(value: Any): MiraiBotUnmuteEvent? = doSafeCast(value)
    }
}

/**
 * Bot 成功加入了一个新群.
 *
 * 此事件属于 [MemberIncreaseEvent], 这个增加的成员即bot自身。
 * @see NativeMiraiBotJoinGroupEvent
 */
public interface MiraiBotJoinGroupEvent :
    MiraiGroupBotEvent<NativeMiraiBotJoinGroupEvent>,
    MemberIncreaseEvent {
    override val bot: MiraiBot
    override val group: MiraiGroup

    /**
     * 操作人，Mirai中唯一能获取到的操作人即当事件为**被邀请**时候的邀请人。
     * 其他情况无法得知操作者。
     */
    @OptIn(Api4J::class)
    override val operator: MiraiMember?

    /**
     * 即bot自己。
     */
    override val target: MiraiMember

    //// Impl

    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PRIVATE
    @JvmSynthetic
    override suspend fun operator(): MiraiMember? = operator
    override val source: MiraiGroup get() = group
    @JvmSynthetic
    override suspend fun source(): MiraiGroup = group
    @JvmSynthetic
    override suspend fun target(): MiraiMember = target

    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group
    override val after: MiraiMember get() = target
    override val before: MiraiMember? get() = null
    @JvmSynthetic
    override suspend fun before(): MiraiMember? = null
    @JvmSynthetic
    override suspend fun after(): MiraiMember = after
    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group
    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group

    /**
     * 暂时仅定为 "被动的"。
     */
    override val actionType: ActionType
        get() = ActionType.PASSIVE

    override val key: Event.Key<MiraiBotJoinGroupEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiBotJoinGroupEvent>(
        "mirai.bot_join_group", MiraiGroupBotEvent, MemberIncreaseEvent
    ) {
        override fun safeCast(value: Any): MiraiBotJoinGroupEvent? = doSafeCast(value)
    }
}

/**
 * Bot 被邀请加入一个群事件。
 *
 * [MiraiBotInvitedJoinGroupRequestEvent] 在含义上类似于 [MiraiGroupBotEvent],
 * 但是 [NativeMiraiBotInvitedJoinGroupRequestEvent] 不属于 [NativeMiraiGroupEvent], 因此当前事件类型不属于 [MiraiGroupBotEvent].
 *
 *
 * @see NativeMiraiBotInvitedJoinGroupRequestEvent
 */
public interface MiraiBotInvitedJoinGroupRequestEvent :
    MiraiSimbotBotEvent<NativeMiraiBotInvitedJoinGroupRequestEvent>,
    GroupJoinRequestEvent {
    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val group: GroupInfo

    /**
     * 邀请人信息.
     * @see InvitorUserInfo
     */
    override val inviter: InvitorUserInfo

    //// Impl

    /**
     * bot被邀请的时候没有附加信息。
     */
    override val message: String? get() = null

    @OptIn(Api4J::class)
    override val requester: MiraiBot
        get() = bot

    @JvmSynthetic
    override suspend fun requester(): MiraiBot = bot
    @JvmSynthetic
    override suspend fun inviter(): InvitorUserInfo = inviter

    /**
     * 邀请人.
     */
    @OptIn(Api4J::class)
    override val user: InvitorUserInfo
        get() = inviter

    @JvmSynthetic
    override suspend fun user(): InvitorUserInfo = inviter
    @JvmSynthetic
    override suspend fun group(): GroupInfo = group

    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PRIVATE

    override val type: RequestEvent.Type get() = RequestEvent.Type.INVITATION

    @ExperimentalSimbotApi
    @JvmSynthetic
    override suspend fun accept(): Boolean {
        nativeEvent.accept()
        return true
    }

    /**
     * 拒绝即代表忽略。
     *
     * @see NativeMiraiBotInvitedJoinGroupRequestEvent
     */
    @ExperimentalSimbotApi
    @JvmSynthetic
    override suspend fun reject(): Boolean {
        nativeEvent.ignore()
        return true
    }

    override val key: Event.Key<MiraiBotInvitedJoinGroupRequestEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiBotInvitedJoinGroupRequestEvent>(
        "mirai.bot_invited_join_group", MiraiSimbotBotEvent, GroupJoinRequestEvent
    ) {
        override fun safeCast(value: Any): MiraiBotInvitedJoinGroupRequestEvent? = doSafeCast(value)
    }
}


/**
 * [MiraiBotInvitedJoinGroupRequestEvent] 事件中的邀请人信息。
 *
 * 由于 [NativeMiraiBotInvitedJoinGroupRequestEvent] 没有直接提供用户对象，
 * 因此不能保证一定能够获取到 [nativeInvitor] 实例。
 *
 */
public data class InvitorUserInfo(
    public val nativeInvitor: NativeMiraiUser?,
    private val invitorId: Long,
    private val invitorNick: String,
) : UserInfo {
    override val id: ID = invitorId.ID
    override val avatar: String
        get() = "https://q1.qlogo.cn/g?b=qq&nk=$invitorId&s=640"
    override val username: String get() = invitorNick

}