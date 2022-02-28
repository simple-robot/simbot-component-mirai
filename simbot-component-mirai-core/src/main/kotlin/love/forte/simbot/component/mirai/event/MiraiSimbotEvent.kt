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

import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.event.*
import love.forte.simbot.message.*

//region typealias
/**
 * Mirai的原生事件类型。
 *
 * @see net.mamoe.mirai.event.Event
 */
public typealias NativeMiraiEvent = net.mamoe.mirai.event.Event

/**
 * Mirai的原生事件类型。
 *
 * @see net.mamoe.mirai.event.events.BotEvent
 */
public typealias NativeMiraiBotEvent = net.mamoe.mirai.event.events.BotEvent
/**
 * Mirai的原生事件类型。
 *
 * @see net.mamoe.mirai.event.events.GroupEvent
 */
public typealias NativeMiraiGroupEvent = net.mamoe.mirai.event.events.GroupEvent

/**
 * Mirai的原生事件类型。
 *
 * @see net.mamoe.mirai.event.events.MessageEvent
 */
public typealias NativeMiraiMessageEvent = net.mamoe.mirai.event.events.MessageEvent


/**
 * Mirai的原生事件类型。
 *
 * @see net.mamoe.mirai.event.events.UserMessageEvent
 */
public typealias NativeMiraiUserMessageEvent = net.mamoe.mirai.event.events.UserMessageEvent
//endregion


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
public sealed interface MiraiEvent : Event {

    override val bot: MiraiBot

    /**
     * 事件的唯一标识。
     * Mirai事件中并不一定存在id，当原始事件中没有ID的情况下，将会生成一个随机ID。
     */
    override val id: ID

    /**
     * 原生的mirai事件对象
     */
    public val nativeEvent: NativeMiraiEvent


    public companion object Key : BaseEventKey<MiraiEvent>("mirai.root") {
        override fun safeCast(value: Any): MiraiEvent? = doSafeCast(value)
    }
}

/**
 * Mirai在simbot中进行流转的标记接口。
 *
 * @see Event
 * @see NativeMiraiEvent
 * @author ForteScarlet
 */
public interface MiraiSimbotEvent<E : NativeMiraiEvent> : MiraiEvent {

    override val id: ID

    /**
     * 原生的mirai事件对象
     */
    override val nativeEvent: E


    public companion object Key : BaseEventKey<MiraiSimbotBotEvent<*>>("mirai.event", MiraiEvent) {
        override fun safeCast(value: Any): MiraiSimbotBotEvent<*>? = doSafeCast(value)
    }

}


/**
 * 一切与 [NativeMiraiBotEvent] 相关的事件类型，也是simbot中主要使用的事件类型。
 *
 * @see NativeMiraiEvent
 */
public interface MiraiSimbotBotEvent<E : NativeMiraiBotEvent> : MiraiSimbotEvent<E> {

    /**
     * 事件中的bot对象。
     */
    override val bot: MiraiBot


    public companion object Key : BaseEventKey<MiraiSimbotBotEvent<*>>("mirai.bot_event", setOf(MiraiSimbotEvent)) {
        override fun safeCast(value: Any): MiraiSimbotBotEvent<*>? = doSafeCast(value)
    }

}

/**
 * 与mirai的 [NativeMiraiContact] 相关的事件。
 */
public interface MiraiSimbotContactMessageEvent<E : NativeMiraiMessageEvent> :
    MiraiSimbotBotEvent<E>,
    ContactMessageEvent {

    /**
     * 事件中的bot对象。
     */
    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val user: MiraiContact

    @JvmSynthetic
    override suspend fun user(): MiraiContact = user

    override val messageContent: MiraiReceivedMessageContent


    public companion object Key :
        BaseEventKey<MiraiSimbotContactMessageEvent<*>>("mirai.message_event", setOf(MiraiSimbotBotEvent)) {
        override fun safeCast(value: Any): MiraiSimbotContactMessageEvent<*>? = doSafeCast(value)
    }
}

/**
 * 在 mirai [NativeMiraiContact] 下与 [love.forte.simbot.definition.Contact] 相关的事件。
 */
public interface MiraiSimbotUserMessageEvent<E : NativeMiraiMessageEvent> :
    MiraiSimbotContactMessageEvent<E> {

    /**
     * 事件中的bot对象。
     */
    override val bot: MiraiBot

    @JvmSynthetic
    override suspend fun user(): MiraiContact

    override val messageContent: MiraiReceivedMessageContent


    public companion object Key :
        BaseEventKey<MiraiSimbotContactMessageEvent<*>>("mirai.message_event", setOf(MiraiSimbotBotEvent)) {
        override fun safeCast(value: Any): MiraiSimbotContactMessageEvent<*>? = doSafeCast(value)
    }
}

/**
 * 在 mirai [NativeMiraiContact] 下与 [love.forte.simbot.definition.Group] 相关的事件。
 */
public interface MiraiSimbotGroupMessageEvent<E : NativeMiraiMessageEvent> :
    MiraiSimbotBotEvent<E>,
    ChatroomMessageEvent {

    /**
     * 事件中的bot对象。
     */
    override val bot: MiraiBot

    @JvmSynthetic
    override suspend fun author(): MiraiMember

    @JvmSynthetic
    override suspend fun source(): MiraiGroup


    override val visibleScope: Event.VisibleScope
        get() = Event.VisibleScope.PUBLIC

    override val messageContent: MiraiReceivedMessageContent


    public companion object Key :
        BaseEventKey<MiraiSimbotContactMessageEvent<*>>("mirai.message_event", setOf(MiraiSimbotBotEvent)) {
        override fun safeCast(value: Any): MiraiSimbotContactMessageEvent<*>? = doSafeCast(value)
    }
}

