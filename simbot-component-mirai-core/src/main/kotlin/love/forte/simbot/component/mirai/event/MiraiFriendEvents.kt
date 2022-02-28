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
import love.forte.simbot.definition.*
import love.forte.simbot.event.*
import love.forte.simbot.message.*

/**
 * @see net.mamoe.mirai.event.events.FriendEvent
 */
public typealias NativeMiraiFriendEvent = net.mamoe.mirai.event.events.FriendEvent
/**
 * @see net.mamoe.mirai.event.events.FriendRemarkChangeEvent
 */
public typealias NativeMiraiFriendRemarkChangeEvent = net.mamoe.mirai.event.events.FriendRemarkChangeEvent
/**
 * @see net.mamoe.mirai.event.events.FriendAddEvent
 */
public typealias NativeMiraiFriendAddEvent = net.mamoe.mirai.event.events.FriendAddEvent
public typealias NativeMiraiFriendIncreaseEvent = NativeMiraiFriendAddEvent
/**
 * @see net.mamoe.mirai.event.events.FriendDeleteEvent
 */
public typealias NativeMiraiFriendDeleteEvent = net.mamoe.mirai.event.events.FriendDeleteEvent
public typealias NativeMiraiFriendDecreaseEvent = NativeMiraiFriendDeleteEvent
/**
 * @see net.mamoe.mirai.event.events.NewFriendRequestEvent
 */
public typealias NativeMiraiNewFriendRequestEvent = net.mamoe.mirai.event.events.NewFriendRequestEvent
public typealias NativeMiraiFriendRequestEvent = NativeMiraiNewFriendRequestEvent
/**
 * @see net.mamoe.mirai.event.events.FriendAvatarChangedEvent
 */
public typealias NativeMiraiFriendAvatarChangedEvent = net.mamoe.mirai.event.events.FriendAvatarChangedEvent
/**
 * @see net.mamoe.mirai.event.events.FriendNickChangedEvent
 */
public typealias NativeMiraiFriendNickChangedEvent = net.mamoe.mirai.event.events.FriendNickChangedEvent
/**
 * @see net.mamoe.mirai.event.events.FriendInputStatusChangedEvent
 */
public typealias NativeMiraiFriendInputStatusChangedEvent = net.mamoe.mirai.event.events.FriendInputStatusChangedEvent


/**
 *
 * mirai中与好友相关的事件在simbot下的事件父类型。
 *
 * 即存在于 `net.mamoe.mirai.event.events.friend.kt` 中的相关事件。
 *
 * @author ForteScarlet
 *
 * @see MiraiSimbotBotEvent
 * @see NativeMiraiFriendEvent
 * @see NativeMiraiFriendRemarkChangeEvent
 * @see NativeMiraiFriendAddEvent
 * @see NativeMiraiFriendDeleteEvent
 * @see NativeMiraiNewFriendRequestEvent
 * @see NativeMiraiFriendAvatarChangedEvent
 * @see NativeMiraiFriendNickChangedEvent
 * @see NativeMiraiFriendInputStatusChangedEvent
 */
public interface MiraiFriendEvent<E : NativeMiraiFriendEvent> :
    MiraiSimbotBotEvent<E>, FriendEvent {

    override val bot: MiraiBot
    override val key: Event.Key<out MiraiFriendEvent<*>>

    @OptIn(Api4J::class)
    override val friend: MiraiFriend

    // Impl

    @OptIn(Api4J::class)
    override val user: MiraiFriend
        get() = friend

    @JvmSynthetic
    override suspend fun user(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun friend(): MiraiFriend = friend

    /**
     * 一般来讲，与好友相关的事件，其可见性都只是当前这个bot自己。
     */
    override val visibleScope: Event.VisibleScope
        get() = Event.VisibleScope.PRIVATE

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
 * @see NativeMiraiFriendRemarkChangeEvent
 */
public interface MiraiFriendRemarkChangeEvent :
    MiraiFriendEvent<NativeMiraiFriendRemarkChangeEvent>,
    ChangedEvent<MiraiFriend, String, String> {

    /**
     * 变更前昵称
     */
    override val after: String

    /**
     * 变更后昵称
     */
    override val before: String

    //// Impl
    @JvmSynthetic
    override suspend fun after(): String = after
    @JvmSynthetic
    override suspend fun before(): String = before
    override val source: MiraiFriend get() = friend
    @JvmSynthetic
    override suspend fun source(): MiraiFriend = friend

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
 * @see NativeMiraiFriendAddEvent
 * @see MiraiFriendRequestEvent
 */
public interface MiraiFriendIncreaseEvent :
    MiraiFriendEvent<NativeMiraiFriendAddEvent>, FriendIncreaseEvent {
    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val friend: MiraiFriend

    //// Impl

    override val source: Bot get() = super.source
    override val after: MiraiFriend get() = friend
    override val target: MiraiFriend get() = friend
    override val before: MiraiFriend? get() = null
    @JvmSynthetic
    override suspend fun friend(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun after(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun source(): MiraiBot = bot
    @JvmSynthetic
    override suspend fun target(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun before(): MiraiFriend? = null

    /**
     * 正常来讲，bot自己的好友变化只有bot自己直到。
     */
    override val visibleScope: Event.VisibleScope
        get() = Event.VisibleScope.PRIVATE

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
 * @see NativeMiraiFriendDeleteEvent
 * @see FriendDecreaseEvent
 */
public interface MiraiFriendDecreaseEvent : MiraiFriendEvent<NativeMiraiFriendDeleteEvent>, FriendDecreaseEvent {
    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val friend: MiraiFriend

    //// Impl

    @JvmSynthetic
    override suspend fun friend(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun source(): MiraiBot = bot
    override val source: MiraiBot get() = bot
    override val after: MiraiFriend? get() = null
    override val before: MiraiFriend get() = friend
    override val target: MiraiFriend get() = friend
    @JvmSynthetic
    override suspend fun after(): Friend? = friend
    @JvmSynthetic
    override suspend fun before(): Friend = friend
    @JvmSynthetic
    override suspend fun target(): Friend = friend

    @OptIn(Api4J::class)
    override val user: MiraiFriend
        get() = friend

    @JvmSynthetic
    override suspend fun user(): MiraiFriend = friend

    /**
     * 正常来讲，bot自己的好友变化只有bot自己直到。
     */
    override val visibleScope: Event.VisibleScope
        get() = Event.VisibleScope.PRIVATE

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
 * @see  NativeMiraiFriendAvatarChangedEvent
 * @see ChangedEvent
 */
public interface MiraiFriendAvatarChangedEvent :
    MiraiFriendEvent<NativeMiraiFriendAvatarChangedEvent>,
    ChangedEvent<MiraiFriend, String?, String> {


    override val bot: MiraiBot
    override val friend: MiraiFriend

    //// Impl

    override val after: String get() = friend.avatar
    override val before: String? get() = null
    override val source: MiraiFriend get() = friend
    override val user: MiraiFriend get() = friend

    @JvmSynthetic
    override suspend fun friend(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun user(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun after(): String = after
    @JvmSynthetic
    override suspend fun before(): String? = null
    @JvmSynthetic
    override suspend fun source(): MiraiFriend = friend

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
 * @see NativeMiraiFriendNickChangedEvent
 * @see ChangedEvent
 */
public interface MiraiFriendNickChangedEvent :
    MiraiFriendEvent<NativeMiraiFriendNickChangedEvent>,
    ChangedEvent<MiraiFriend, String, String> {

    override val bot: MiraiBot
    override val friend: MiraiFriend
    override val before: String
    override val after: String

    //// Impl

    override val source: MiraiFriend get() = friend
    override val user: MiraiFriend get() = friend

    @JvmSynthetic
    override suspend fun after(): String = after
    @JvmSynthetic
    override suspend fun before(): String = before
    @JvmSynthetic
    override suspend fun source(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun user(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun friend(): MiraiFriend = friend

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
 * @see NativeMiraiFriendInputStatusChangedEvent
 * @see ChangedEvent
 */
public interface MiraiFriendInputStatusChangedEvent :
    MiraiFriendEvent<NativeMiraiFriendInputStatusChangedEvent>,
    ChangedEvent<MiraiFriend, Boolean, Boolean> {

    override val bot: MiraiBot
    override val friend: MiraiFriend
    override val before: Boolean
    override val after: Boolean

    override val source: MiraiFriend get() = friend
    override val user: MiraiFriend get() = friend

    @JvmSynthetic
    override suspend fun after(): Boolean = after
    @JvmSynthetic
    override suspend fun before(): Boolean = before
    @JvmSynthetic
    override suspend fun source(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun user(): MiraiFriend = friend
    @JvmSynthetic
    override suspend fun friend(): MiraiFriend = friend

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
 * @see NativeMiraiNewFriendRequestEvent
 * @see FriendAddRequestEvent
 */
public interface MiraiFriendRequestEvent :
    MiraiSimbotBotEvent<NativeMiraiNewFriendRequestEvent>,
    FriendAddRequestEvent {

    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val friend: RequestFriendInfo

    override val message: String

    //// Impl

    @OptIn(ExperimentalSimbotApi::class)
    @JvmSynthetic
    override suspend fun accept(): Boolean {
        nativeEvent.accept()
        return true
    }


    /**
     * 拒绝申请，并选择是否添加此人为黑名单。
     */
    @JvmSynthetic
    public suspend fun reject(blockList: Boolean): Boolean {
        nativeEvent.reject(blockList)
        return true
    }

    @Api4J
    public fun rejectBlocking(blockList: Boolean): Boolean = runBlocking { reject(blockList) }

    @Api4J
    public fun rejectAsync(blockList: Boolean) {
        bot.launch { reject(blockList) }
    }

    /**
     * 普通的拒绝本次申请。
     */
    @OptIn(ExperimentalSimbotApi::class)
    @JvmSynthetic
    override suspend fun reject(): Boolean = reject(false)

    @OptIn(Api4J::class)
    override val user: RequestFriendInfo
        get() = friend

    @OptIn(Api4J::class)
    override val requester: RequestFriendInfo
        get() = friend
    override val inviter: UserInfo? get() = null

    @JvmSynthetic
    override suspend fun friend(): RequestFriendInfo = friend
    @JvmSynthetic
    override suspend fun user(): RequestFriendInfo = friend
    @JvmSynthetic
    override suspend fun requester(): RequestFriendInfo = friend
    @JvmSynthetic
    override suspend fun inviter(): UserInfo? = null

    override val key: Event.Key<MiraiFriendRequestEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiFriendRequestEvent>(
        "mirai.friend_request", MiraiSimbotBotEvent, FriendAddRequestEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendRequestEvent? = doSafeCast(value)
    }

}
