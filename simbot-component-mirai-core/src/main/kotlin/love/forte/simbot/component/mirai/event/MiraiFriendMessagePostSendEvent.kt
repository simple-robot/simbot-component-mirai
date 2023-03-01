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
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.JSTP
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.definition.FriendInfoContainer
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.Event
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.RemoteMessageContainer
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend
import net.mamoe.mirai.event.events.FriendMessagePostSendEvent as OriginalMiraiFriendMessagePostSendEvent

/**
 * 好友消息发送后的消息事件。此事件不会实现 [FriendMessageEvent], 取而代之的是使用 [FriendInfoContainer], [MessageEvent].
 * 因此此消息本质上并非"好友发来的消息"，而只是对bot的行为的后置处理。
 *
 * @author ForteScarlet
 */
public interface MiraiFriendMessagePostSendEvent :
    MiraiMessagePostSendEvent<OriginalMiraiFriend, OriginalMiraiFriendMessagePostSendEvent>,
    FriendInfoContainer, MessageEvent, RemoteMessageContainer {

    override val bot: MiraiBot
    override val id: ID
    override val timestamp: Timestamp
    override val messageContent: MiraiReceivedMessageContent
    override val originalEvent: OriginalMiraiFriendMessagePostSendEvent
    
    /**
     * 发送目标好友对象。
     */
    @JSTP
    override suspend fun friend(): MiraiFriend

    /**
     * 所有 `post send` 相关事件的源头均来自bot自身。
     */
    @JSTP
    override suspend fun source(): MiraiBot = bot



    override val key: Event.Key<out MiraiFriendMessagePostSendEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiFriendMessagePostSendEvent>(
        "mirai.friend_message_post_send_event", MiraiMessagePostSendEvent, MessageEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendMessagePostSendEvent? = doSafeCast(value)
    }
}
