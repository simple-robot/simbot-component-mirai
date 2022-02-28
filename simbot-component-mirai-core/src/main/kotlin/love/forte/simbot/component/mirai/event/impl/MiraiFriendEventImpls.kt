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
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.*
import love.forte.simbot.event.*

/**
 * @see MiraiFriendEvent
 */
internal abstract class BaseMiraiFriendEventImpl<E : NativeMiraiFriendEvent>(
    final override val bot: MiraiBotImpl,
    final override val nativeEvent: E
) : MiraiFriendEvent<E> {
    override val id: ID = randomID()
    override val friend: MiraiFriendImpl = nativeEvent.friend.asSimbot(bot)
}

internal class MiraiFriendRequestEventImpl(
    override val bot: MiraiBotImpl,
    override val nativeEvent: NativeMiraiNewFriendRequestEvent
) : MiraiFriendRequestEvent {
    override val id: ID = nativeEvent.eventId.ID
    override val friend: RequestFriendInfo = nativeEvent.toRequestUserInfo()
    override val timestamp: Timestamp = Timestamp.now()
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PRIVATE
    override val type: RequestEvent.Type get() = RequestEvent.Type.APPLICATION
    override val message: String = nativeEvent.message
}


private fun NativeMiraiNewFriendRequestEvent.toRequestUserInfo(): RequestFriendInfo {
    return RequestFriendInfo(
        fromId = fromId,
        fromGroupId = fromGroupId,
        fromGroupName = this.fromGroup?.name,
        fromNick = fromNick
    )
}


internal class MiraiFriendInputStatusChangedEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: NativeMiraiFriendInputStatusChangedEvent
) : MiraiFriendInputStatusChangedEvent,
    BaseMiraiFriendEventImpl<NativeMiraiFriendInputStatusChangedEvent>(bot, nativeEvent) {
    override val after: Boolean = nativeEvent.inputting
    override val before: Boolean get() = !after
    override val changedTime: Timestamp = Timestamp.now()
}

internal class MiraiFriendNickChangedEventImpl(
    override val bot: MiraiBotImpl,
    override val nativeEvent: NativeMiraiFriendNickChangedEvent
) : MiraiFriendNickChangedEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = nativeEvent.friend.asSimbot(bot)
    override val before: String = nativeEvent.from
    override val after: String = nativeEvent.to
}

internal class MiraiFriendAvatarChangedEventImpl(
    override val bot: MiraiBotImpl,
    override val nativeEvent: NativeMiraiFriendAvatarChangedEvent
) : MiraiFriendAvatarChangedEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = nativeEvent.friend.asSimbot(bot)
}

internal class MiraiFriendDecreaseEventImpl(
    override val bot: MiraiBotImpl,
    override val nativeEvent: NativeMiraiFriendDeleteEvent
) : MiraiFriendDecreaseEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = nativeEvent.friend.asSimbot(bot)
}

internal class MiraiFriendIncreaseEventImpl(
    override val bot: MiraiBotImpl,
    override val nativeEvent: NativeMiraiFriendAddEvent
) : MiraiFriendIncreaseEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = nativeEvent.friend.asSimbot(bot)
}

internal class MiraiFriendRemarkChangeEventImpl(
    override val bot: MiraiBotImpl,
    override val nativeEvent: NativeMiraiFriendRemarkChangeEvent
) : MiraiFriendRemarkChangeEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = nativeEvent.friend.asSimbot(bot)
    override val before: String = nativeEvent.oldRemark
    override val after: String = nativeEvent.newRemark
}