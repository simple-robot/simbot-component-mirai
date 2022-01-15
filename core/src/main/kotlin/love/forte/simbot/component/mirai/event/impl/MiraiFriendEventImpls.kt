package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiFriendImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.event.Event
import love.forte.simbot.event.RequestEvent

/**
 * @see MiraiFriendEvent
 */
internal abstract class BaseMiraiFriendEventImpl<E : NativeMiraiFriendEvent>(
    final override val bot: MiraiBotImpl,
    nativeEvent: E
) : MiraiFriendEvent<E> {
    override val metadata: MiraiSimbotEvent.Metadata<E> = nativeEvent.toSimpleMetadata()
    override val friend: MiraiFriendImpl = nativeEvent.friend.asSimbot(bot)
}

internal class MiraiFriendRequestEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiNewFriendRequestEvent
) : MiraiFriendRequestEvent {
    override val metadata = nativeEvent.toSimpleMetadata(nativeEvent.eventId.ID)
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
    nativeEvent: NativeMiraiFriendNickChangedEvent
) : MiraiFriendNickChangedEvent {
    override val metadata: MiraiSimbotEvent.Metadata<NativeMiraiFriendNickChangedEvent> = nativeEvent.toSimpleMetadata()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = nativeEvent.friend.asSimbot(bot)
    override val before: String = nativeEvent.from
    override val after: String = nativeEvent.to
}

internal class MiraiFriendAvatarChangedEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiFriendAvatarChangedEvent
) : MiraiFriendAvatarChangedEvent {
    override val metadata: MiraiSimbotEvent.Metadata<NativeMiraiFriendAvatarChangedEvent> =
        nativeEvent.toSimpleMetadata()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = nativeEvent.friend.asSimbot(bot)
}

internal class MiraiFriendDecreaseEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiFriendDeleteEvent
) : MiraiFriendDecreaseEvent {
    override val metadata: MiraiSimbotEvent.Metadata<NativeMiraiFriendDeleteEvent> = nativeEvent.toSimpleMetadata()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = nativeEvent.friend.asSimbot(bot)
}

internal class MiraiFriendIncreaseEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiFriendAddEvent
) : MiraiFriendIncreaseEvent {
    override val metadata: MiraiSimbotEvent.Metadata<NativeMiraiFriendAddEvent> = nativeEvent.toSimpleMetadata()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = nativeEvent.friend.asSimbot(bot)
}

internal class MiraiFriendRemarkChangeEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiFriendRemarkChangeEvent
) : MiraiFriendRemarkChangeEvent {
    override val metadata: MiraiSimbotEvent.Metadata<NativeMiraiFriendRemarkChangeEvent> =
        nativeEvent.toSimpleMetadata()
    override val changedTime: Timestamp = Timestamp.now()
    override val friend = nativeEvent.friend.asSimbot(bot)
    override val before: String = nativeEvent.oldRemark
    override val after: String = nativeEvent.newRemark
}