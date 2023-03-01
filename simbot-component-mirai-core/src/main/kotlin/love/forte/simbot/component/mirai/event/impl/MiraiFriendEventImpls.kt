/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  This file is part of simbot-component-mirai.
 *
 *  simbot-component-mirai is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  simbot-component-mirai is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with simbot-component-mirai. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiFriendImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.event.RequestEvent
import love.forte.simbot.randomID
import net.mamoe.mirai.event.events.FriendAddEvent as OriginalMiraiFriendAddEvent
import net.mamoe.mirai.event.events.FriendAvatarChangedEvent as OriginalMiraiFriendAvatarChangedEvent
import net.mamoe.mirai.event.events.FriendDeleteEvent as OriginalMiraiFriendDeleteEvent
import net.mamoe.mirai.event.events.FriendEvent as OriginalMiraiFriendEvent
import net.mamoe.mirai.event.events.FriendInputStatusChangedEvent as OriginalMiraiFriendInputStatusChangedEvent
import net.mamoe.mirai.event.events.FriendNickChangedEvent as OriginalMiraiFriendNickChangedEvent
import net.mamoe.mirai.event.events.FriendRemarkChangeEvent as OriginalMiraiFriendRemarkChangeEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent as OriginalMiraiNewFriendRequestEvent

/**
 * @see MiraiFriendEvent
 */
internal abstract class BaseMiraiFriendEventImpl<E : OriginalMiraiFriendEvent>(
    final override val bot: MiraiBotImpl,
    final override val originalEvent: E,
) : MiraiFriendEvent<E> {
    override val id: ID = randomID()
    private val _friend: MiraiFriendImpl = originalEvent.friend.asSimbot(bot)
    
    override val timestamp: Timestamp = Timestamp.now()
    
    override suspend fun friend(): MiraiFriend = _friend
}


internal class MiraiFriendRequestEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiNewFriendRequestEvent,
) : MiraiFriendRequestEvent {
    override val id: ID = originalEvent.eventId.ID
    private val _friend: RequestFriendInfo = originalEvent.toRequestUserInfo()
    override val timestamp: Timestamp = Timestamp.now()
    override val type: RequestEvent.Type get() = RequestEvent.Type.APPLICATION
    override val message: String = originalEvent.message
    
    override suspend fun friend(): RequestFriendInfo = _friend
    
    // api
    
    override suspend fun accept(): Boolean {
        originalEvent.accept()
        return true
    }
    
    override suspend fun reject(block: Boolean): Boolean {
        originalEvent.reject(block)
        return true
    }
}


private fun OriginalMiraiNewFriendRequestEvent.toRequestUserInfo(): RequestFriendInfo {
    return RequestFriendInfo(
        fromId = fromId, fromGroupId = fromGroupId, fromGroupName = this.fromGroup?.name, fromNick = fromNick
    )
}


internal class MiraiFriendInputStatusChangedEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiFriendInputStatusChangedEvent,
) : MiraiFriendInputStatusChangedEvent,
    BaseMiraiFriendEventImpl<OriginalMiraiFriendInputStatusChangedEvent>(bot, nativeEvent) {
    private val inputting = nativeEvent.inputting
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp = Timestamp.now()
    override suspend fun after(): Boolean = inputting
    override suspend fun before(): Boolean = !inputting
}

internal class MiraiFriendNickChangedEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendNickChangedEvent,
) : MiraiFriendNickChangedEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    private val _friend = originalEvent.friend.asSimbot(bot)
    
    override suspend fun friend(): MiraiFriend = _friend
    override suspend fun before(): String = originalEvent.from
    override suspend fun after(): String = originalEvent.to
}

internal class MiraiFriendAvatarChangedEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendAvatarChangedEvent,
) : MiraiFriendAvatarChangedEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    private val _friend = originalEvent.friend.asSimbot(bot)
    
    override suspend fun friend(): MiraiFriend = _friend
    override suspend fun after(): String = originalEvent.friend.avatarUrl
}

internal class MiraiFriendDecreaseEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendDeleteEvent,
) : MiraiFriendDecreaseEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    private val _friend = originalEvent.friend.asSimbot(bot)
    
    override suspend fun friend(): MiraiFriend = _friend
}

internal class MiraiFriendIncreaseEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendAddEvent,
) : MiraiFriendIncreaseEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    private val _friend = originalEvent.friend.asSimbot(bot)
    
    override suspend fun friend(): MiraiFriend = _friend
}

internal class MiraiFriendRemarkChangeEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendRemarkChangeEvent,
) : MiraiFriendRemarkChangeEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    private val _friend = originalEvent.friend.asSimbot(bot)
    
    override suspend fun friend(): MiraiFriend = _friend
    override suspend fun before(): String = originalEvent.oldRemark
    override suspend fun after(): String = originalEvent.newRemark
}
