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
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.definition.GroupInfoContainer
import love.forte.simbot.definition.MemberInfoContainer
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.Event
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.Member as OriginalMiraiMember
import net.mamoe.mirai.event.events.GroupTempMessagePostSendEvent as OriginalMiraiGroupTempMessagePostSendEvent

/**
 * 群临时会话消息发送后的消息事件。此事件不会实现 [ContactMessageEvent], 取而代之的是使用 [GroupInfoContainer], [MemberInfoContainer], [MessageEvent].
 * 此消息本质上并非"群成员的临时消息"，而只是对bot的行为的后置处理。
 *
 * @author ForteScarlet
 */
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public interface MiraiGroupTempMessagePostSendEvent :
    MiraiMessagePostSendEvent<OriginalMiraiMember, OriginalMiraiGroupTempMessagePostSendEvent>,
    GroupInfoContainer, MemberInfoContainer, MessageEvent {
    
    override val bot: MiraiBot
    override val messageContent: MiraiReceivedMessageContent
    
    /**
     * 发送目标群成员所属群对象。
     */
    override suspend fun group(): MiraiGroup
    
    /**
     * 发送目标群成员对象。
     */
    override suspend fun member(): MiraiMember
    
    /**
     * 所有 `post send` 相关事件的源头均来自bot自身。
     */
    override suspend fun source(): MiraiBot = bot
    
    
    override val key: Event.Key<out MiraiGroupTempMessagePostSendEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupTempMessagePostSendEvent>(
        "mirai.group_temp_message_post_send_event", MiraiMessagePostSendEvent, MessageEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupTempMessagePostSendEvent? = doSafeCast(value)
    }
}
