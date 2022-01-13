package love.forte.simbot.component.mirai.event

import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.*
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.ChatroomMessageEvent
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.Event
import love.forte.simbot.message.doSafeCast

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
 * @see net.mamoe.mirai.event.events.MessageEvent
 */
public typealias NativeMiraiMessageEvent = net.mamoe.mirai.event.events.MessageEvent


/**
 * Mirai的原生事件类型。
 *
 * @see net.mamoe.mirai.event.events.UserMessageEvent
 */
public typealias NativeMiraiUserMessageEvent = net.mamoe.mirai.event.events.UserMessageEvent


/**
 * [MiraiEvent] 是所有在simbot中进行实现的事件类型的顶层类型。
 *
 * [MiraiEvent] 是 `sealed` 的，其只有两个实现：[MiraiSimbotEvent] 和 [UnsupportedMiraiEvent].
 *
 * [MiraiSimbotEvent] 是其他所有以提供针对性实现的mirai事件的父类型，而 [UnsupportedMiraiEvent] 是其他可能出现的一切未提供支持的事件的统一类型。
 *
 * @see MiraiSimbotEvent
 * @see UnsupportedMiraiEvent
 */
public sealed interface MiraiEvent : Event {

    override val bot: MiraiBot

    /**
     * mirai事件的元数据。可以通过 [Metadata.nativeEvent] 得到此事件的原始事件对象。
     */
    override val metadata: Metadata

    /**
     * Mirai事件在simbot中的 metadata对象。
     */
    public interface Metadata : Event.Metadata {
        /**
         * Mirai事件中并不一定存在id，当原始事件中没有ID的情况下，
         * 将会使用事件对象的hash作为ID值。
         */
        override val id: ID

        /**
         * 得到此事件中的原生 mirai 事件对象。
         */
        public val nativeEvent: NativeMiraiEvent
    }

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

    /**
     * 得到此事件的 metadata，其中可以通过 [MiraiSimbotEvent.Metadata.nativeEvent] 得到原生的mirai事件对象。
     */
    override val metadata: Metadata<E>

    public companion object Key : BaseEventKey<MiraiSimbotBotEvent<*>>("mirai.event", MiraiEvent) {
        override fun safeCast(value: Any): MiraiSimbotBotEvent<*>? = doSafeCast(value)
    }


    /**
     * Mirai事件在simbot中的 metadata对象。
     */
    public interface Metadata<E : NativeMiraiEvent> : MiraiEvent.Metadata {
        /**
         * Mirai事件中并不一定存在id，当原始事件中没有ID的情况下，
         * 将会使用事件对象的hash作为ID值。
         */
        override val id: ID

        /**
         * 得到此事件中的原生 mirai 事件对象。
         */
        override val nativeEvent: E
    }
}

public inline val <E : NativeMiraiEvent> MiraiSimbotEvent<E>.nativeEvent: E get() = metadata.nativeEvent


public abstract class BaseMiraiSimbotEventMetadata<E : NativeMiraiEvent>(
    final override val nativeEvent: E,
    override val id: ID = nativeEvent.hashCode().ID
) : MiraiSimbotEvent.Metadata<E> {
    public val isIntercepted: Boolean get() = nativeEvent.isIntercepted
}

/**
 * 得到一个根据 [MiraiSimbotEvent.Metadata] 实现的最基础的meta实例。
 */
public fun <E : NativeMiraiEvent> E.toSimpleMetadata(id: ID? = null): MiraiSimbotEvent.Metadata<E> =
    SimpleMiraiSimbotEventMetadata(this, id ?: this.hashCode().ID)

/**
 * 基础的 [BaseMiraiSimbotEventMetadata] 实现。
 */
private class SimpleMiraiSimbotEventMetadata<E : NativeMiraiEvent>(
    nativeEvent: E,
    id: ID
) : BaseMiraiSimbotEventMetadata<E>(nativeEvent, id)


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
public interface MiraiSimbotChatroomMessageEvent<E : NativeMiraiMessageEvent> :
    MiraiSimbotBotEvent<E>,
    ChatroomMessageEvent {

    /**
     * 事件中的bot对象。
     */
    override val bot: MiraiBot

    override suspend fun author(): MiraiMember
    override suspend fun source(): MiraiGroup


    override val visibleScope: Event.VisibleScope
        get() = Event.VisibleScope.PUBLIC

    override val messageContent: MiraiReceivedMessageContent


    public companion object Key :
        BaseEventKey<MiraiSimbotContactMessageEvent<*>>("mirai.message_event", setOf(MiraiSimbotBotEvent)) {
        override fun safeCast(value: Any): MiraiSimbotContactMessageEvent<*>? = doSafeCast(value)
    }
}

