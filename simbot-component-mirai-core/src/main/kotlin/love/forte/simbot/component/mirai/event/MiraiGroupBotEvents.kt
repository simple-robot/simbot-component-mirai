/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
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

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.ID
import love.forte.simbot.action.ActionType
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.definition.GroupInfo
import love.forte.simbot.definition.UserInfo
import love.forte.simbot.event.*
import love.forte.simbot.message.doSafeCast
import kotlin.time.Duration
import net.mamoe.mirai.contact.User as OriginalMiraiUser
import net.mamoe.mirai.event.events.BotGroupPermissionChangeEvent as OriginalMiraiBotGroupPermissionChangeEvent
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent as OriginalMiraiBotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.BotJoinGroupEvent as OriginalMiraiBotJoinGroupEvent
import net.mamoe.mirai.event.events.BotLeaveEvent as OriginalMiraiBotLeaveEvent
import net.mamoe.mirai.event.events.BotMuteEvent as OriginalMiraiBotMuteEvent
import net.mamoe.mirai.event.events.BotUnmuteEvent as OriginalMiraiBotUnmuteEvent
import net.mamoe.mirai.event.events.GroupEvent as OriginalMiraiGroupEvent


/**
 * simbot中与 [OriginalMiraiGroupEvent] 相关的事件中，与 bot 相关的事件。
 *
 * 一般代表了在 `net.mamoe.mirai.event.events.group.kt` 中与 bot 有直接关系的事件，比较简单的判断标准为这个mirai事件是否为 `Bot` 开头的。
 *
 *
 *
 * @see OriginalMiraiBotLeaveEvent
 * @see MiraiBotGroupRoleChangeEvent
 * @see MiraiBotMuteEvent
 * @see MiraiBotUnmuteEvent
 * @see MiraiBotJoinGroupEvent
 * @see MiraiBotInvitedJoinGroupRequestEvent
 */
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public interface MiraiGroupBotEvent<E : OriginalMiraiGroupEvent> : MiraiSimbotBotEvent<E>, GroupEvent {
    
    override val key: Event.Key<out MiraiGroupBotEvent<*>>
    override val bot: MiraiBot
    
    /**
     * 事件涉及的群。同 [organization]。
     */
    override suspend fun group(): MiraiGroup
    
    /**
     * 事件涉及的群。同 [group]。
     */
    override suspend fun organization(): MiraiGroup = group()
    
    
    public companion object Key :
        BaseEventKey<MiraiGroupBotEvent<*>>("mirai.group_bot", MiraiSimbotBotEvent, GroupEvent) {
        override fun safeCast(value: Any): MiraiGroupBotEvent<*>? = doSafeCast(value)
    }
}


/**
 * 机器人被踢出群或在其他客户端主动退出一个群.
 * 在事件广播前 Bot.groups 就已删除这个群.
 *
 * 此事件属于一个 [群成员减少事件][MemberDecreaseEvent], 减少的这个成员便是bot自己。
 *
 * @see OriginalMiraiBotLeaveEvent
 * @see MemberDecreaseEvent
 */
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public interface MiraiBotLeaveEvent : MiraiGroupBotEvent<OriginalMiraiBotLeaveEvent>, MemberDecreaseEvent {
    override val actionType: ActionType
    
    /**
     * 退群前的成员，即当前bot在群众作为的成员。
     */
    override suspend fun member(): MiraiMember
    
    /**
     * 退群前的成员，即当前bot在群众作为的成员。同 [member]。
     */
    override suspend fun user(): MiraiMember = member()
    
    /**
     * 操作者。如果bot为自行退出则不存在操作者。
     */
    override suspend fun operator(): MiraiMember?
    
    /**
     * 事件涉及的群。同 [group]。
     */
    override suspend fun source(): MiraiGroup = group()
    
    /**
     * 退群前的成员，即当前bot在群众作为的成员。同 [member]。
     */
    override suspend fun before(): MiraiMember = member()
    
    
    override val key: Event.Key<MiraiBotLeaveEvent> get() = Key
    
    public companion object Key :
        BaseEventKey<MiraiBotLeaveEvent>("mirai.bot_leave", MiraiGroupBotEvent, MemberDecreaseEvent) {
        override fun safeCast(value: Any): MiraiBotLeaveEvent? = doSafeCast(value)
    }
}

/**
 * Bot 在群里的权限(角色)被改变。
 * 操作人一定是群主。
 *
 * 此事件属于一个 [已变动事件][ChangedEvent], [变动源][source] 即当前bot，
 * 变动前后为bot在群里的权限。
 *
 * @see OriginalMiraiBotGroupPermissionChangeEvent
 */
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public interface MiraiBotGroupRoleChangeEvent : MiraiGroupBotEvent<OriginalMiraiBotGroupPermissionChangeEvent>,
    ChangedEvent {
    /**
     * 变更前的角色。
     */
    override suspend fun before(): MemberRole
    
    /**
     * 变更后的角色。
     */
    override suspend fun after(): MemberRole
    
    /**
     * 此事件的bot。
     */
    override suspend fun source(): MiraiBot = bot
    
    override val key: Event.Key<MiraiBotGroupRoleChangeEvent> get() = Key
    
    public companion object Key :
        BaseEventKey<MiraiBotGroupRoleChangeEvent>("mirai.bot_group_role_change", MiraiGroupBotEvent, ChangedEvent) {
        override fun safeCast(value: Any): MiraiBotGroupRoleChangeEvent? = doSafeCast(value)
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
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public sealed interface MiraiBotMuteRelateEvent<E : OriginalMiraiGroupEvent> : MiraiGroupBotEvent<E>, ChangedEvent {
    
    /**
     * 剩余禁言时间的时长。
     */
    @get:JvmSynthetic
    public val duration: Duration
    
    /**
     * 剩余禁言时间的时长的秒值。
     */
    public val durationSeconds: Int
    
    /**
     * 操作人。
     */
    public val operator: MiraiMember
    
    /**
     * 变更前是否被禁言。与 [after] 互反。
     */
    override suspend fun before(): Boolean
    
    /**
     * 变更后是否被禁言。与 [before] 互反。
     */
    override suspend fun after(): Boolean
    
    /**
     * 事件涉及的群。同 [group]。
     */
    override suspend fun source(): MiraiGroup = group()
    
    
    public companion object Key :
        BaseEventKey<MiraiBotMuteRelateEvent<*>>("mirai.bot_mute_relate", MiraiGroupBotEvent, ChangedEvent) {
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
 * @see OriginalMiraiBotMuteEvent
 */
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public interface MiraiBotMuteEvent : MiraiGroupBotEvent<OriginalMiraiBotMuteEvent>,
    MiraiBotMuteRelateEvent<OriginalMiraiBotMuteEvent> {
    /**
     * 被禁言前，恒为 `false`。
     */
    override suspend fun before(): Boolean = false
    
    /**
     * 被禁言后，恒为 `true`。
     */
    override suspend fun after(): Boolean = true
    
    override val key: Event.Key<MiraiBotMuteEvent> get() = Key
    
    public companion object Key :
        BaseEventKey<MiraiBotMuteEvent>("mirai.bot_mute", MiraiGroupBotEvent, MiraiBotMuteRelateEvent) {
        override fun safeCast(value: Any): MiraiBotMuteEvent? = doSafeCast(value)
    }
}

/**
 * Bot 被取消禁言事件。
 *
 * 属于一个变化事件 [ChangedEvent], 其类型代表事件前后的禁言状态。
 * [before] 永远为true，[after] 永远为false。
 * @see OriginalMiraiBotUnmuteEvent
 */
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public interface MiraiBotUnmuteEvent : MiraiGroupBotEvent<OriginalMiraiBotUnmuteEvent>,
    MiraiBotMuteRelateEvent<OriginalMiraiBotUnmuteEvent> {
    
    /**
     * 被取消禁言前，恒为 `true`。
     */
    override suspend fun before(): Boolean = true
    
    /**
     * 被取消禁言后，恒为 `false`。
     */
    override suspend fun after(): Boolean = false
    
    override val key: Event.Key<MiraiBotUnmuteEvent> get() = Key
    
    public companion object Key :
        BaseEventKey<MiraiBotUnmuteEvent>("mirai.bot_unmute", MiraiGroupBotEvent, MiraiBotMuteRelateEvent) {
        override fun safeCast(value: Any): MiraiBotUnmuteEvent? = doSafeCast(value)
    }
}

/**
 * Bot 成功加入了一个新群.
 *
 * 此事件属于 [MemberIncreaseEvent], 这个增加的成员即bot自身。
 * @see OriginalMiraiBotJoinGroupEvent
 */
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public interface MiraiBotJoinGroupEvent : MiraiGroupBotEvent<OriginalMiraiBotJoinGroupEvent>,
    MemberIncreaseEvent {
    /**
     * 事件涉及的成员。
     */
    override suspend fun member(): MiraiMember
    
    /**
     * 事件涉及的成员。同 [member]。
     */
    override suspend fun user(): MiraiMember = member()
    
    /**
     * 操作人，Mirai中唯一能获取到的操作人即当事件为**被邀请**时候的邀请人。
     * 其他情况无法得知操作者。
     */
    override suspend fun operator(): MiraiMember?
    
    /**
     * 事件涉及的群，同 [group]。
     */
    override suspend fun source(): MiraiGroup = group()
    
    /**
     * 加入的群成员。同 [member]。
     */
    override suspend fun after(): MiraiMember = member()
    
    /**
     * 暂时仅定为 "被动的"。
     */
    override val actionType: ActionType
        get() = ActionType.PASSIVE
    
    override val key: Event.Key<MiraiBotJoinGroupEvent> get() = Key
    
    public companion object Key :
        BaseEventKey<MiraiBotJoinGroupEvent>("mirai.bot_join_group", MiraiGroupBotEvent, MemberIncreaseEvent) {
        override fun safeCast(value: Any): MiraiBotJoinGroupEvent? = doSafeCast(value)
    }
}

/**
 * Bot 被邀请加入一个群事件。
 *
 * [MiraiBotInvitedJoinGroupRequestEvent] 在含义上类似于 [MiraiGroupBotEvent],
 * 但是 [OriginalMiraiBotInvitedJoinGroupRequestEvent] 不属于 [OriginalMiraiGroupEvent], 因此当前事件类型不属于 [MiraiGroupBotEvent].
 *
 *
 * @see OriginalMiraiBotInvitedJoinGroupRequestEvent
 */
public interface MiraiBotInvitedJoinGroupRequestEvent :
    MiraiSimbotBotEvent<OriginalMiraiBotInvitedJoinGroupRequestEvent>, GroupJoinRequestEvent {
    /**
     * bot被邀请的时候没有附加信息, 始终得到null。
     */
    override val message: String? get() = null
    
    /**
     * 涉及的群的信息。
     */
    @JSTP
    override suspend fun group(): GroupInfo
    
    /**
     * 申请人，即bot自己，同 [bot]。
     */
    @JSTP
    override suspend fun requester(): MiraiBot = bot
    
    /**
     * 申请人，即bot自己，同 [bot]。
     */
    @JSTP
    override suspend fun user(): MiraiBot = bot
    
    /**
     * 邀请人的信息，同 [user]。
     * @see InvitorUserInfo
     */
    @JSTP
    override suspend fun inviter(): InvitorUserInfo
    
    /**
     * 同意请求。
     */
    @OptIn(ExperimentalSimbotApi::class)
    @JST
    override suspend fun accept(): Boolean
    
    /**
     * 拒绝即代表忽略。
     *
     * @see OriginalMiraiBotInvitedJoinGroupRequestEvent
     */
    @OptIn(ExperimentalSimbotApi::class)
    @JST
    override suspend fun reject(): Boolean
    
    override val type: RequestEvent.Type get() = RequestEvent.Type.INVITATION
    
    override val key: Event.Key<MiraiBotInvitedJoinGroupRequestEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiBotInvitedJoinGroupRequestEvent>(
        "mirai.bot_invited_join_group",
        MiraiSimbotBotEvent,
        GroupJoinRequestEvent
    ) {
        override fun safeCast(value: Any): MiraiBotInvitedJoinGroupRequestEvent? = doSafeCast(value)
    }
}


/**
 * [MiraiBotInvitedJoinGroupRequestEvent] 事件中的邀请人信息。
 *
 * 由于 [OriginalMiraiBotInvitedJoinGroupRequestEvent] 没有直接提供用户对象，
 * 因此不能保证一定能够获取到 [originalInvitor] 实例。
 *
 */
public data class InvitorUserInfo(
    public val originalInvitor: OriginalMiraiUser?,
    private val invitorId: Long,
    private val invitorNick: String,
) : UserInfo {
    override val id: ID = invitorId.ID
    override val avatar: String
        get() = "https://q1.qlogo.cn/g?b=qq&nk=$invitorId&s=640"
    override val username: String get() = invitorNick
}
