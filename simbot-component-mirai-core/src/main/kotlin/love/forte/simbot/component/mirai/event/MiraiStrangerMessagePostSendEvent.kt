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
