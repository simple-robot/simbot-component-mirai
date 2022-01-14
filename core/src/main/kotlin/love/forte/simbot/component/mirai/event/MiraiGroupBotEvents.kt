package love.forte.simbot.component.mirai.event

import love.forte.simbot.Api4J
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.definition.Organization
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.Event
import love.forte.simbot.event.GroupEvent
import love.forte.simbot.message.doSafeCast

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
 */
public interface MiraiGroupBotEvent<E : NativeMiraiGroupEvent> :
    MiraiSimbotBotEvent<E>, GroupEvent {

    override val key: Event.Key<out MiraiGroupBotEvent<*>>
    override val bot: MiraiBot
    @OptIn(Api4J::class)
    override val group: MiraiGroup


    @OptIn(Api4J::class)
    override val organization: MiraiGroup get() = group
    override suspend fun group(): MiraiGroup = group
    override suspend fun organization(): Organization = group

    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PRIVATE

    public companion object Key : BaseEventKey<MiraiGroupBotEvent<*>>(
        "mirai.group_bot", MiraiSimbotBotEvent, GroupEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupBotEvent<*>? = doSafeCast(value)
    }
}




