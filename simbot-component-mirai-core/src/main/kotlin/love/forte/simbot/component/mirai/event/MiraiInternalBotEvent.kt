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

import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.component.mirai.bot.MiraiBotManager
import love.forte.simbot.component.mirai.event.MiraiInternalBotEvent.Key
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.Event
import love.forte.simbot.event.internal.*
import love.forte.simbot.message.doSafeCast

/**
 *
 * Mirai组件下对 [InternalBotEvent] 的实现总类型的统一实现接口。
 *
 * 此事件虽然只实现了 [Event], 但实际上其事件类型都是 [InternalBotEvent] 下的相关类型，
 * 且 [Key] 中存在的继承关系也是对 [InternalBotEvent] 的实现。
 *
 * @author ForteScarlet
 */
public sealed interface MiraiInternalBotEvent : Event {

    public companion object Key : BaseEventKey<MiraiInternalBotEvent>(
        "mirai.internal.bot", InternalBotEvent
    ) {
        override fun safeCast(value: Any): MiraiInternalBotEvent? = doSafeCast(value)
    }
}


/**
 * 当一个 [MiraiBot] 经由 [MiraiBotManager.register] 而注册得到后触发的事件。
 *
 * 由于 [MiraiBotManager.register] 并非可挂起函数，因此 `register` 中对bot的注册逻辑执行完成后会立刻返回，而对于事件的推送会异步的进行。
 *
 * @see MiraiBot
 * @see MiraiBotManager
 * @see MiraiBotManager.register
 *
 */
public abstract class MiraiBotRegisteredEvent : BotRegisteredEvent(), MiraiInternalBotEvent {

    /**
     * 被注册的Bot对象。
     */
    abstract override val bot: MiraiBot


    override val key: InternalEvent.Key<out InternalEvent> get() = Key

    public companion object Key : BaseInternalKey<MiraiBotRegisteredEvent>(
        "mirai.internal.bot.registered", BotRegisteredEvent, MiraiInternalBotEvent
    ) {
        override fun safeCast(value: Any): MiraiBotRegisteredEvent? = doSafeCast(value)
    }
}


/**
 * 当一个 [MiraiBot] 执行 [MiraiBot.start] 时会触发此事件。
 *
 * [MiraiBot.start] 会在最后完成整个事件触发流程后返回，因此对此事件的处理请尽可能避免过长时间的阻塞或挂起，
 * 以及尽量避免出现嵌套执行 [start][MiraiBot.start].
 *
 *
 * @see MiraiBot
 * @see MiraiBot.start
 *
 */
public abstract class MiraiBotStartedEvent : BotStartedEvent(), MiraiInternalBotEvent {

    /**
     * 被启动的Bot对象。得到此对象的时候，[MiraiBot] 实际上的启动逻辑均已执行完毕。
     */
    abstract override val bot: MiraiBot


    override val key: InternalEvent.Key<MiraiBotStartedEvent> get() = Key

    public companion object Key : BaseInternalKey<MiraiBotStartedEvent>(
        "mirai.internal.bot.started", BotStartedEvent, MiraiInternalBotEvent
    ) {
        override fun safeCast(value: Any): MiraiBotStartedEvent? = doSafeCast(value)
    }
}