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

import love.forte.simbot.ID
import love.forte.simbot.component.mirai.JSTP
import love.forte.simbot.component.mirai.MiraiContact
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.event.*
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.event.Event as OriginalMiraiEvent
import net.mamoe.mirai.event.events.BotEvent as OriginalMiraiBotEvent
import net.mamoe.mirai.event.events.MessageEvent as OriginalMiraiMessageEvent


/**
 * [MiraiEvent] 是所有在simbot中进行实现的事件类型的顶层类型。
 *
 * [MiraiEvent] 是 `sealed` 的，其只有两个实现：[MiraiSimbotEvent] 和 [UnsupportedMiraiEvent].
 *
 * [MiraiSimbotEvent] 是所有已提供针对性实现的mirai事件的父类型，而 [UnsupportedMiraiEvent] 是其他可能出现的一切未提供支持的事件的统一类型。
 *
 * @see MiraiSimbotEvent
 * @see UnsupportedMiraiEvent
 */
@BaseEvent
public sealed interface MiraiEvent : Event {
    
    override val bot: MiraiBot
    
    /**
     * 事件的唯一标识。
     * Mirai事件中并不一定存在id，当原始事件中没有ID的情况下，将会生成一个随机ID。
     */
    override val id: ID
    
    /**
     * 原始的mirai事件对象
     */
    public val originalEvent: OriginalMiraiEvent
    
    
    public companion object Key : BaseEventKey<MiraiEvent>("mirai.root") {
        override fun safeCast(value: Any): MiraiEvent? = doSafeCast(value)
    }
}

/**
 * Mirai在simbot中进行流转的标记接口。
 *
 * @see Event
 * @see OriginalMiraiEvent
 * @author ForteScarlet
 */
@BaseEvent
public interface MiraiSimbotEvent<E : OriginalMiraiEvent> : MiraiEvent {
    
    override val id: ID
    
    /**
     * 原始的mirai事件对象
     */
    override val originalEvent: E
    
    
    public companion object Key : BaseEventKey<MiraiSimbotBotEvent<*>>("mirai.event", MiraiEvent) {
        override fun safeCast(value: Any): MiraiSimbotBotEvent<*>? = doSafeCast(value)
    }
    
}


/**
 * 一切与 [OriginalMiraiBotEvent] 相关的事件类型，也是simbot中主要使用的事件类型。
 *
 * @see OriginalMiraiEvent
 */
@BaseEvent
public interface MiraiSimbotBotEvent<E : OriginalMiraiBotEvent> : MiraiSimbotEvent<E> {
    
    /**
     * 事件中的bot对象。
     */
    override val bot: MiraiBot
    
    
    public companion object Key : BaseEventKey<MiraiSimbotBotEvent<*>>("mirai.bot_event", setOf(MiraiSimbotEvent)) {
        override fun safeCast(value: Any): MiraiSimbotBotEvent<*>? = doSafeCast(value)
    }
    
}

/**
 * 与mirai的 [net.mamoe.mirai.contact.Contact] 相关的事件。
 */
@BaseEvent
public interface MiraiSimbotContactMessageEvent<E : OriginalMiraiMessageEvent> :
    MiraiSimbotBotEvent<E>,
    ContactMessageEvent {
    
    /**
     * 事件中的bot对象。
     */
    override val bot: MiraiBot
    
    /**
     * 此事件中涉及到的用户。
     */
    @JSTP
    override suspend fun user(): MiraiContact
    
    override val messageContent: MiraiReceivedMessageContent
    
    
    public companion object Key :
        BaseEventKey<MiraiSimbotContactMessageEvent<*>>("mirai.message_event", setOf(MiraiSimbotBotEvent)) {
        override fun safeCast(value: Any): MiraiSimbotContactMessageEvent<*>? = doSafeCast(value)
    }
}

/**
 * 在 mirai [net.mamoe.mirai.contact.Contact] 下与 [love.forte.simbot.definition.Contact] 相关的事件。
 */
@BaseEvent
public interface MiraiSimbotUserMessageEvent<E : OriginalMiraiMessageEvent> :
    MiraiSimbotContactMessageEvent<E> {
    
    /**
     * 事件中的bot对象。
     */
    override val bot: MiraiBot
    
    /**
     * 事件中涉及的用户。
     */
    @JSTP
    override suspend fun user(): MiraiContact
    
    override val messageContent: MiraiReceivedMessageContent
    
    
    public companion object Key :
        BaseEventKey<MiraiSimbotContactMessageEvent<*>>("mirai.message_event", setOf(MiraiSimbotBotEvent)) {
        override fun safeCast(value: Any): MiraiSimbotContactMessageEvent<*>? = doSafeCast(value)
    }
}

/**
 * 在 mirai [net.mamoe.mirai.contact.Contact] 下与 [love.forte.simbot.definition.Group] 相关的事件。
 */
@BaseEvent
public interface MiraiSimbotGroupMessageEvent<E : OriginalMiraiMessageEvent> :
    MiraiSimbotBotEvent<E>,
    ChatRoomMessageEvent {
    
    /**
     * 事件中的bot对象。
     */
    override val bot: MiraiBot
    
    /**
     * 此消息事件的发送人。
     */
    @JSTP
    override suspend fun author(): MiraiMember
    
    /**
     * 此事件发生的群。
     */
    @JSTP
    override suspend fun source(): MiraiGroup
    
    
    override val messageContent: MiraiReceivedMessageContent
    
    
    public companion object Key :
        BaseEventKey<MiraiSimbotContactMessageEvent<*>>("mirai.message_event", setOf(MiraiSimbotBotEvent)) {
        override fun safeCast(value: Any): MiraiSimbotContactMessageEvent<*>? = doSafeCast(value)
    }
}

