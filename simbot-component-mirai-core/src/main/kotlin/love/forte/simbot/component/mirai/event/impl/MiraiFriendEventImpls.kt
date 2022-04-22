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

import kotlinx.coroutines.launch
import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiFriendImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.event.RequestEvent
import love.forte.simbot.randomID
import love.forte.simbot.utils.runInBlocking
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
    override val friend: MiraiFriendImpl = originalEvent.friend.asSimbot(bot)
    override val user: MiraiFriend get() = friend
    override val timestamp: Timestamp = Timestamp.now()

    override suspend fun friend(): MiraiFriend = friend
    override suspend fun user(): MiraiFriend = friend
}


internal class MiraiFriendRequestEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiNewFriendRequestEvent,
) : MiraiFriendRequestEvent {
    override val id: ID = originalEvent.eventId.ID
    override val friend: RequestFriendInfo = originalEvent.toRequestUserInfo()
    override val timestamp: Timestamp = Timestamp.now()
    override val type: RequestEvent.Type get() = RequestEvent.Type.APPLICATION
    override val message: String = originalEvent.message

    override val requester: RequestFriendInfo = originalEvent.toRequestUserInfo()
    override val user: RequestFriendInfo get() = requester

    override suspend fun requester(): RequestFriendInfo = requester
    override suspend fun friend(): RequestFriendInfo = friend

    // api

    override suspend fun accept(): Boolean {
        originalEvent.accept()
        return true
    }

    override suspend fun reject(block: Boolean): Boolean {
        originalEvent.reject(block)
        return true
    }

    @Api4J
    override fun rejectBlocking(block: Boolean): Boolean {
        return runInBlocking { reject(block) }
    }

    @Api4J
    override fun rejectAsync(block: Boolean) {
        bot.launch { reject(block) }
    }

    override suspend fun reject(): Boolean = reject(false)
}


private fun OriginalMiraiNewFriendRequestEvent.toRequestUserInfo(): RequestFriendInfo {
    return RequestFriendInfo(
        fromId = fromId,
        fromGroupId = fromGroupId,
        fromGroupName = this.fromGroup?.name,
        fromNick = fromNick
    )
}


internal class MiraiFriendInputStatusChangedEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiFriendInputStatusChangedEvent,
) : MiraiFriendInputStatusChangedEvent,
    BaseMiraiFriendEventImpl<OriginalMiraiFriendInputStatusChangedEvent>(bot, nativeEvent) {
    override val after: Boolean = nativeEvent.inputting
    override val before: Boolean get() = !after
    override val changedTime: Timestamp = Timestamp.now()
    override val timestamp: Timestamp = Timestamp.now()
    override val source: MiraiFriend get() = friend

    override suspend fun after(): Boolean = after

    override suspend fun before(): Boolean = before

    override suspend fun source(): MiraiFriend = source
}

internal class MiraiFriendNickChangedEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendNickChangedEvent,
) : MiraiFriendNickChangedEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = originalEvent.friend.asSimbot(bot)
    override val before: String = originalEvent.from
    override val after: String = originalEvent.to
    override val user: MiraiFriend get() = friend
    override val source: MiraiFriend get() = friend

    override suspend fun user(): MiraiFriend = user
    override suspend fun friend(): MiraiFriend = friend
    override suspend fun after(): String = after
    override suspend fun before(): String = before
    override suspend fun source(): MiraiFriend = source

}

internal class MiraiFriendAvatarChangedEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendAvatarChangedEvent,
) : MiraiFriendAvatarChangedEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = originalEvent.friend.asSimbot(bot)
    override val user: MiraiFriend get() = friend
    override val after: String = originalEvent.friend.avatarUrl
    override val source: MiraiFriend get() = friend


    override suspend fun user(): MiraiFriend = user
    override suspend fun friend(): MiraiFriend = friend
    override suspend fun after(): String = after
    override suspend fun source(): MiraiFriend = source
}

internal class MiraiFriendDecreaseEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendDeleteEvent,
) : MiraiFriendDecreaseEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = originalEvent.friend.asSimbot(bot)

    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(Api4J::class)
    override val user: MiraiFriend get() = friend
    override val source: MiraiBot get() = bot
    override val before: MiraiFriend get() = friend


    override suspend fun user(): MiraiFriend = user
    override suspend fun source(): MiraiBot = source
    override suspend fun before(): MiraiFriend = before
    override suspend fun friend(): MiraiFriend = friend
}

internal class MiraiFriendIncreaseEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendAddEvent,
) : MiraiFriendIncreaseEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = originalEvent.friend.asSimbot(bot)

    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(Api4J::class)
    override val user: MiraiFriend get() = friend
    override val source: MiraiBot get() = bot
    override val after: MiraiFriend get() = friend

    override suspend fun user(): MiraiFriend = user
    override suspend fun source(): MiraiBot = source
    override suspend fun after(): MiraiFriend = after
    override suspend fun friend(): MiraiFriend = friend
}

internal class MiraiFriendRemarkChangeEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendRemarkChangeEvent,
) : MiraiFriendRemarkChangeEvent {
    override val id: ID = randomID()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = originalEvent.friend.asSimbot(bot)
    override val before: String = originalEvent.oldRemark
    override val after: String = originalEvent.newRemark

    override val user: MiraiFriend get() = friend
    override val source: MiraiFriend get() = friend

    override suspend fun user(): MiraiFriend = user
    override suspend fun friend(): MiraiFriend = friend
    override suspend fun after(): String = after
    override suspend fun before(): String = before
    override suspend fun source(): MiraiFriend = source
}