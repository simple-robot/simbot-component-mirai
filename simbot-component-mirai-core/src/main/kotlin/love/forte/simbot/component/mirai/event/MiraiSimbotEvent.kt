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

