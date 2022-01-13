package love.forte.simbot.component.mirai.event

import love.forte.simbot.Api4J
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.MiraiStranger
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.Event
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.message.doSafeCast

/**
 * @see net.mamoe.mirai.event.events.FriendMessageEvent
 */
public typealias NativeMiraiFriendMessageEvent = net.mamoe.mirai.event.events.FriendMessageEvent

/**
 * @see net.mamoe.mirai.event.events.StrangerMessageEvent
 */
public typealias NativeMiraiStrangerMessageEvent = net.mamoe.mirai.event.events.StrangerMessageEvent

/**
 * 好友消息事件。
 *
 * Mirai [NativeMiraiFriendMessageEvent] 事件对应的 [FriendMessageEvent] 事件类型。
 *
 * @see NativeMiraiFriendMessageEvent
 * @author ForteScarlet
 */
public interface MiraiFriendMessageEvent :
    MiraiSimbotContactMessageEvent<NativeMiraiFriendMessageEvent>,
    MiraiFriendEvent<NativeMiraiFriendMessageEvent>,
    FriendMessageEvent {

    override val bot: MiraiBot
    override val metadata: Metadata

    @OptIn(Api4J::class)
    override val friend: MiraiFriend
    //// impl

    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PRIVATE
    override val key: Event.Key<MiraiFriendMessageEvent> get() = Key
    override suspend fun user(): MiraiFriend = friend
    override suspend fun source(): MiraiFriend = friend
    override suspend fun friend(): MiraiFriend = friend


    @OptIn(Api4J::class)
    override val source: MiraiFriend
        get() = friend

    @OptIn(Api4J::class)
    override val user: MiraiFriend
        get() = friend

    /**
     * Metadata for [MiraiFriendMessageEvent].
     */
    public interface Metadata : MiraiSimbotEvent.Metadata<NativeMiraiFriendMessageEvent>

    public companion object Key :
        BaseEventKey<MiraiFriendMessageEvent>(
            "mirai.friend_message",
            MiraiSimbotContactMessageEvent, MiraiFriendEvent, FriendMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiFriendMessageEvent? = doSafeCast(value)
    }
}


/**
 * Mirai陌生人消息事件。
 */
public interface MiraiStrangerMessageEvent :
    MiraiSimbotContactMessageEvent<NativeMiraiStrangerMessageEvent>,
    ContactMessageEvent {

    override val bot: MiraiBot
    override val key: Event.Key<MiraiStrangerMessageEvent> get() = Key
    override val metadata: Metadata

    @OptIn(Api4J::class)
    override val user: MiraiStranger

    override suspend fun user(): MiraiStranger = user



    /**
     * Metadata for [MiraiStrangerMessageEvent].
     */
    public interface Metadata : MiraiSimbotEvent.Metadata<NativeMiraiStrangerMessageEvent>

    public companion object Key :
        BaseEventKey<MiraiStrangerMessageEvent>(
            "mirai.stranger_message",
            MiraiSimbotContactMessageEvent, ContactMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiStrangerMessageEvent? = doSafeCast(value)
    }
}