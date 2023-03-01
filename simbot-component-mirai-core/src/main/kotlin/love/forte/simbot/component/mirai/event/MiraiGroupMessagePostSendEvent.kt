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

import love.forte.simbot.component.mirai.JSTP
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.definition.GroupInfoContainer
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.Event
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent as OriginalMiraiGroupMessagePostSendEvent

/**
 * 群消息发送后的消息事件。此事件不会实现 [GroupMessageEvent], 取而代之的是使用 [GroupInfoContainer], [MessageEvent].
 * 此消息本质上并非"群中得到的消息"，而只是对bot的行为的后置处理。
 *
 * @author ForteScarlet
 */
public interface MiraiGroupMessagePostSendEvent :
    MiraiMessagePostSendEvent<OriginalMiraiGroup, OriginalMiraiGroupMessagePostSendEvent>,
    GroupInfoContainer, MessageEvent {
    
    override val bot: MiraiBot
    override val messageContent: MiraiReceivedMessageContent
    
    
    /**
     * 发送目标群对象。
     */
    @JSTP
    override suspend fun group(): MiraiGroup
    
    
    /**
     * 所有 `post send` 相关事件的源头均来自于[bot]自身。
     */
    @JSTP
    override suspend fun source(): MiraiBot = bot
    
    
    override val key: Event.Key<out MiraiGroupMessagePostSendEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupMessagePostSendEvent>(
        "mirai.group_message_post_send_event", MiraiMessagePostSendEvent, MessageEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupMessagePostSendEvent? = doSafeCast(value)
    }
}
