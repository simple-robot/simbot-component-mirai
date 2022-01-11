package love.forte.simbot.component.mirai.event

import love.forte.simbot.ID
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.event.BaseEventKey
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
 * Mirai在simbot中进行流转的标记接口。
 *
 * @see Event
 * @see NativeMiraiEvent
 * @author ForteScarlet
 */
public interface MiraiSimbotEvent<E : NativeMiraiEvent> : Event {

    /**
     * 得到此事件的 metadata，其中可以通过 [MiraiSimbotEvent.Metadata.nativeEvent] 得到原生的mirai事件对象。
     */
    override val metadata: Metadata<E>

    public companion object Key : BaseEventKey<MiraiSimbotBotEvent<*>>("mirai.event") {
        override fun safeCast(value: Any): MiraiSimbotBotEvent<*>? = doSafeCast(value)
    }


    /**
     * Mirai事件在simbot中的 metadata对象。
     */
    public interface Metadata<E : NativeMiraiEvent> : Event.Metadata {
        /**
         * Mirai事件中并不一定存在id，当原始事件中没有ID的情况下，
         * 将会使用事件对象的hash作为ID值。
         */
        override val id: ID

        /**
         * 得到此事件中的原生 mirai 事件对象。
         */
        public val nativeEvent: E
    }
}


public abstract class BaseMiraiSimbotEventMetadata<E : NativeMiraiEvent>(
    final override val nativeEvent: E
) : MiraiSimbotEvent.Metadata<E> {
    override val id: ID = nativeEvent.hashCode().ID
    public val isIntercepted: Boolean get() = nativeEvent.isIntercepted
}

/**
 * TODO meta.
 */
public fun <E : NativeMiraiEvent> E.toMetadata(): MiraiSimbotEvent.Metadata<E> = SimpleMiraiSimbotEventMetadata(this)

/**
 * 基础的 [BaseMiraiSimbotEventMetadata] 实现。
 */
private class SimpleMiraiSimbotEventMetadata<E : NativeMiraiEvent>(
    nativeEvent: E
) : BaseMiraiSimbotEventMetadata<E>(nativeEvent)


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


