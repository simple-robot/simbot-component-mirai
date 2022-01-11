package love.forte.simbot.component.mirai.event

import kotlinx.coroutines.runBlocking
import love.forte.simbot.Api4J
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.message.doSafeCast


public typealias NativeMiraiFriendMessageEvent = net.mamoe.mirai.event.events.FriendMessageEvent

/**
 * Mirai [NativeMiraiFriendMessageEvent] 事件对应的 [FriendMessageEvent] 事件类型。
 *
 * @author ForteScarlet
 */
public interface MiraiFriendMessageEvent :
    MiraiSimbotBotEvent<NativeMiraiFriendMessageEvent>,
    FriendMessageEvent {

    override val bot: MiraiBot
    override suspend fun friend(): MiraiFriend

    //// impl

    override suspend fun user(): MiraiFriend = friend()
    override suspend fun source(): MiraiFriend = friend()

    @Api4J
    override val friend: MiraiFriend
        get() = runBlocking { friend() }

    @Api4J
    override val source: MiraiFriend
        get() = friend

    @Api4J
    override val user: MiraiFriend
        get() = friend


    public companion object Key :
        BaseEventKey<MiraiFriendMessageEvent>(
            "mirai.friend_message",
            MiraiSimbotBotEvent, FriendMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiFriendMessageEvent? = doSafeCast(value)
    }
}