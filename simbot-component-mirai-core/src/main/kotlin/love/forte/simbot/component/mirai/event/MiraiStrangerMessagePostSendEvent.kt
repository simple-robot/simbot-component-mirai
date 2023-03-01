/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU Affero通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
 *
 *
 */

package love.forte.simbot.component.mirai.event

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiStranger
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.definition.UserInfoContainer
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.Event
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.RemoteMessageContainer
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger
import net.mamoe.mirai.event.events.StrangerMessagePostSendEvent as OriginalMiraiStrangerMessagePostSendEvent

/**
 * 陌生人消息发送后的消息事件。此事件不会实现 [ContactMessageEvent], 取而代之的是使用 [UserInfoContainer] (Stranger) , [MessageEvent].
 * 因此此消息本质上并非"陌生人发来的消息"，而只是对bot的行为的后置处理。
 *
 * @author ForteScarlet
 */
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public interface MiraiStrangerMessagePostSendEvent :
    MiraiMessagePostSendEvent<OriginalMiraiStranger, OriginalMiraiStrangerMessagePostSendEvent>,
    UserInfoContainer, MessageEvent, RemoteMessageContainer {
    
    override val bot: MiraiBot
    override val id: ID
    override val timestamp: Timestamp
    override val messageContent: MiraiReceivedMessageContent
    override val originalEvent: OriginalMiraiStrangerMessagePostSendEvent
    
    /**
     * 发送目标陌生人对象。
     */
    override suspend fun user(): MiraiStranger
    
    /**
     * 所有 `post send` 相关事件的源头均来自bot自身。
     */
    override suspend fun source(): MiraiBot = bot
    
    
    override val key: Event.Key<out MiraiStrangerMessagePostSendEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiStrangerMessagePostSendEvent>(
        "mirai.stranger_message_post_send_event", MiraiMessagePostSendEvent, MessageEvent
    ) {
        override fun safeCast(value: Any): MiraiStrangerMessagePostSendEvent? = doSafeCast(value)
    }
}
