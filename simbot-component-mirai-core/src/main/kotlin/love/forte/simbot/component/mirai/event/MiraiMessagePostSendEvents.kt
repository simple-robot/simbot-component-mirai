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
 */

package love.forte.simbot.component.mirai.event

import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.definition.FriendInfoContainer
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.Event
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.event.events.MessagePostSendEvent as OriginalMiraiMessagePostSendEvent


/**
 * simbot下针对于mirai的 [OriginalMiraiMessagePostSendEvent] 相关事件的接口定义。
 *
 * 这些事件类似于消息事件 (例如 [MiraiSimbotContactMessageEvent])。
 *
 * 但是这些相关消息不会实现对应的消息事件类型，
 * 例如 [MiraiFriendMessagePostSendEvent] 不会实现 [FriendMessageEvent],
 * 而是会分别实现 [MessageEvent], [FriendInfoContainer], 这使得它们有别于普通的消息事件。
 *
 *
 * @see MiraiFriendMessagePostSendEvent
 * @see MiraiStrangerMessagePostSendEvent
 * @see MiraiGroupTempMessagePostSendEvent
 * @see MiraiGroupMessagePostSendEvent
 *
 */
public interface MiraiMessagePostSendEvent<C : net.mamoe.mirai.contact.Contact, E : OriginalMiraiMessagePostSendEvent<C>> :
    MiraiSimbotBotEvent<E> {
    override val bot: MiraiBot

    /**
     * 这类事件理论上属于系统内事件，只能由bot自身可见。
     */
    override val visibleScope: Event.VisibleScope
        get() = Event.VisibleScope.PRIVATE

    public companion object Key : BaseEventKey<MiraiMessagePostSendEvent<*, *>>(
        "mirai.message_post_send", MiraiSimbotBotEvent
    ) {
        override fun safeCast(value: Any): MiraiMessagePostSendEvent<*, *>? = doSafeCast(value)
    }
}




