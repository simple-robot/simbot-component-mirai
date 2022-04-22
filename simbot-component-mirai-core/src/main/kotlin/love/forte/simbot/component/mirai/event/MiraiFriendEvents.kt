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

@file:Suppress("UnnecessaryOptInAnnotation")

package love.forte.simbot.component.mirai.event

import love.forte.simbot.Api4J
import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.definition.UserInfo
import love.forte.simbot.event.*
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.event.events.FriendAddEvent as OriginalMiraiFriendAddEvent
import net.mamoe.mirai.event.events.FriendAvatarChangedEvent as OriginalMiraiFriendAvatarChangedEvent
import net.mamoe.mirai.event.events.FriendDeleteEvent as OriginalMiraiFriendDeleteEvent
import net.mamoe.mirai.event.events.FriendEvent as OriginalMiraiFriendEvent
import net.mamoe.mirai.event.events.FriendInputStatusChangedEvent as OriginalMiraiFriendInputStatusChangedEvent
import net.mamoe.mirai.event.events.FriendNickChangedEvent as OriginalMiraiFriendNickChangedEvent
import net.mamoe.mirai.event.events.FriendRemarkChangeEvent as OriginalMiraiFriendRemarkChangeEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent as OriginalMiraiNewFriendRequestEvent


/**
 *
 * mirai中与好友相关的事件在simbot下的事件父类型。
 *
 * 即存在于 `net.mamoe.mirai.event.events.friend.kt` 中的相关事件。
 *
 * @author ForteScarlet
 *
 * @see MiraiSimbotBotEvent
 * @see OriginalMiraiFriendEvent
 * @see OriginalMiraiFriendRemarkChangeEvent
 * @see OriginalMiraiFriendAddEvent
 * @see OriginalMiraiFriendDeleteEvent
 * @see OriginalMiraiNewFriendRequestEvent
 * @see OriginalMiraiFriendAvatarChangedEvent
 * @see OriginalMiraiFriendNickChangedEvent
 * @see OriginalMiraiFriendInputStatusChangedEvent
 */
public interface MiraiFriendEvent<E : OriginalMiraiFriendEvent> :
    MiraiSimbotBotEvent<E>, FriendEvent {
    override val key: Event.Key<out MiraiFriendEvent<*>>

    /**
     * 涉及到的 [Mirai好友][MiraiFriend]。
     */
    @OptIn(Api4J::class)
    override val friend: MiraiFriend


    /**
     * 涉及到的 [Mirai好友][MiraiFriend]。
     */
    @OptIn(Api4J::class)
    override val user: MiraiFriend

    /**
     * 涉及到的 [Mirai好友][MiraiFriend]。
     */
    @JvmSynthetic
    override suspend fun user(): MiraiFriend

    /**
     * 涉及到的 [Mirai好友][MiraiFriend]。
     */
    @JvmSynthetic
    override suspend fun friend(): MiraiFriend


    public companion object Key : BaseEventKey<MiraiFriendEvent<*>>(
        "mirai.friend", MiraiSimbotBotEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendEvent<*>? = doSafeCast(value)
    }
}


/**
 * 好友昵称改变事件.
 *
 * [MiraiFriendRemarkChangeEvent] 事件属于一个 [已变更事件][ChangedEvent], 变更前后的类型为 [String], 变更源为 [MiraiFriend].
 *
 * @see OriginalMiraiFriendRemarkChangeEvent
 */
public interface MiraiFriendRemarkChangeEvent :
    MiraiFriendEvent<OriginalMiraiFriendRemarkChangeEvent>,
    ChangedEvent {

    /**
     * 变更后昵称
     */
    @OptIn(Api4J::class)
    override val after: String

    /**
     * 变更前昵称
     */
    @OptIn(Api4J::class)
    override val before: String

    /**
     * 变更后昵称
     */
    @JvmSynthetic
    override suspend fun after(): String

    /**
     * 变更前昵称
     */
    @JvmSynthetic
    override suspend fun before(): String


    @OptIn(Api4J::class)
    override val source: MiraiFriend

    @JvmSynthetic
    override suspend fun source(): MiraiFriend


    override val key: Event.Key<MiraiFriendRemarkChangeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiFriendRemarkChangeEvent>(
        "mirai.friend_remark_change", MiraiFriendEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendRemarkChangeEvent? = doSafeCast(value)
    }
}

/**
 * 好友 **已经** 增加事件。
 *
 * @see OriginalMiraiFriendAddEvent
 * @see MiraiFriendRequestEvent
 */
public interface MiraiFriendIncreaseEvent :
    MiraiFriendEvent<OriginalMiraiFriendAddEvent>, FriendIncreaseEvent {

    @OptIn(Api4J::class)
    override val source: MiraiBot

    @JvmSynthetic
    override suspend fun source(): MiraiBot


    @OptIn(Api4J::class)
    override val after: MiraiFriend

    @JvmSynthetic
    override suspend fun after(): MiraiFriend

    @OptIn(Api4J::class)
    override val friend: MiraiFriend

    override suspend fun friend(): MiraiFriend


    override val key: Event.Key<MiraiFriendIncreaseEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiFriendIncreaseEvent>(
        "mirai.friend_increase", MiraiFriendEvent, FriendIncreaseEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendIncreaseEvent? = doSafeCast(value)
    }
}

/**
 * 好友删除（减少）事件.
 *
 * @see OriginalMiraiFriendDeleteEvent
 * @see FriendDecreaseEvent
 */
public interface MiraiFriendDecreaseEvent : MiraiFriendEvent<OriginalMiraiFriendDeleteEvent>, FriendDecreaseEvent {

    @OptIn(Api4J::class)
    override val source: MiraiBot

    @JvmSynthetic
    override suspend fun source(): MiraiBot


    @OptIn(Api4J::class)
    override val before: MiraiFriend

    @JvmSynthetic
    override suspend fun before(): MiraiFriend


    @OptIn(Api4J::class)
    override val friend: MiraiFriend

    @JvmSynthetic
    override suspend fun friend(): MiraiFriend



    override val key: Event.Key<MiraiFriendDecreaseEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiFriendDecreaseEvent>(
        "mirai.friend_decrease", MiraiFriendEvent, FriendDecreaseEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendDecreaseEvent? = doSafeCast(value)
    }
}

/**
 * 好友头像变更事件。是已经修改完成的事件。
 *
 * 无法得到修改前的头像信息，因此 [before] 将为null。
 *
 * @see  OriginalMiraiFriendAvatarChangedEvent
 * @see ChangedEvent
 */
public interface MiraiFriendAvatarChangedEvent :
    MiraiFriendEvent<OriginalMiraiFriendAvatarChangedEvent>,
    ChangedEvent {
    @OptIn(Api4J::class)
    override val after: String

    @JvmSynthetic
    override suspend fun after(): String


    @OptIn(Api4J::class)
    override val source: MiraiFriend

    @JvmSynthetic
    override suspend fun source(): MiraiFriend


    @JvmSynthetic
    override suspend fun before(): Any? = null

    @OptIn(Api4J::class)
    override val before: String? get() = null


    override val key: Event.Key<MiraiFriendAvatarChangedEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiFriendAvatarChangedEvent>(
        "mirai.friend_avatar_changed", MiraiFriendEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendAvatarChangedEvent? = doSafeCast(value)
    }

}

/**
 * 好友昵称变更事件。是已经更改完成的事件。
 *
 * @see OriginalMiraiFriendNickChangedEvent
 * @see ChangedEvent
 */
public interface MiraiFriendNickChangedEvent :
    MiraiFriendEvent<OriginalMiraiFriendNickChangedEvent>,
    ChangedEvent {


    @OptIn(Api4J::class)
    override val after: String

    @JvmSynthetic
    override suspend fun after(): String


    @OptIn(Api4J::class)
    override val before: String


    @JvmSynthetic
    override suspend fun before(): String


    @OptIn(Api4J::class)
    override val source: MiraiFriend

    @JvmSynthetic
    override suspend fun source(): MiraiFriend


    override val key: Event.Key<MiraiFriendNickChangedEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiFriendNickChangedEvent>(
        "mirai.friend_nick_changed", MiraiFriendEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendNickChangedEvent? = doSafeCast(value)
    }

}

/**
 *
 * 好友输入状态变更的事件。变更主体 [before] 和 [after] 代表了当前事件所代表的好友是否是"正在输入"状态。
 *
 * @see OriginalMiraiFriendInputStatusChangedEvent
 * @see ChangedEvent
 */
public interface MiraiFriendInputStatusChangedEvent :
    MiraiFriendEvent<OriginalMiraiFriendInputStatusChangedEvent>,
    ChangedEvent {

    @OptIn(Api4J::class)
    override val before: Boolean


    @JvmSynthetic
    override suspend fun after(): Boolean

    @OptIn(Api4J::class)
    override val after: Boolean

    @JvmSynthetic
    override suspend fun before(): Boolean


    @OptIn(Api4J::class)
    override val source: MiraiFriend


    @JvmSynthetic
    override suspend fun source(): MiraiFriend

    override val key: Event.Key<MiraiFriendInputStatusChangedEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiFriendInputStatusChangedEvent>(
        "mirai.friend_input_status_changed", MiraiFriendEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendInputStatusChangedEvent? = doSafeCast(value)
    }

}


/**
 * 一个好友添加申请。
 *
 * @see OriginalMiraiNewFriendRequestEvent
 * @see FriendAddRequestEvent
 */
public interface MiraiFriendRequestEvent :
    MiraiSimbotBotEvent<OriginalMiraiNewFriendRequestEvent>,
    FriendAddRequestEvent {
    /**
     * 验证消息文本。不为null，但是可能为空。
     */
    override val message: String


    @OptIn(Api4J::class)
    override val requester: RequestFriendInfo

    @JvmSynthetic
    override suspend fun requester(): RequestFriendInfo

    @OptIn(Api4J::class)
    override val friend: RequestFriendInfo

    @JvmSynthetic
    override suspend fun friend(): RequestFriendInfo


    @OptIn(Api4J::class)
    override val user: RequestFriendInfo

    @JvmSynthetic
    override suspend fun user(): RequestFriendInfo = friend


    /**
     * 无法获取邀请者。
     */
    override val inviter: UserInfo? get() = null

    /**
     * 无法获取邀请者。
     */
    @JvmSynthetic
    override suspend fun inviter(): UserInfo? = null


    //// api

    /**
     * 同意申请。
     */
    @OptIn(ExperimentalSimbotApi::class)
    @JvmSynthetic
    override suspend fun accept(): Boolean


    /**
     * 拒绝申请，并选择是否添加此人为黑名单。
     */
    @JvmSynthetic
    public suspend fun reject(block: Boolean): Boolean

    /**
     * 拒绝申请，并选择是否添加此人为黑名单。
     */
    @Api4J
    public fun rejectBlocking(block: Boolean): Boolean

    /**
     * 异步的拒绝申请，并选择是否添加此人为黑名单。
     */
    @Api4J
    public fun rejectAsync(block: Boolean)

    /**
     * 拒绝申请。
     */
    @OptIn(ExperimentalSimbotApi::class)
    @JvmSynthetic
    override suspend fun reject(): Boolean


    override val key: Event.Key<MiraiFriendRequestEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiFriendRequestEvent>(
        "mirai.friend_request", MiraiSimbotBotEvent, FriendAddRequestEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendRequestEvent? = doSafeCast(value)
    }

}
