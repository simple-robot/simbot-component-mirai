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

import love.forte.simbot.action.ReplySupport
import love.forte.simbot.action.SendSupport
import love.forte.simbot.component.mirai.JST
import love.forte.simbot.component.mirai.JSTP
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.Event
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.Member as OriginalMiraiMember
import net.mamoe.mirai.event.events.GroupTempMessageEvent as OriginalMiraiGroupTempMessageEvent


/**
 * @see OriginalMiraiGroupTempMessageEvent
 */
public typealias OriginalMiraiGroupTempMessageEvent = OriginalMiraiGroupTempMessageEvent


/**
 * mirai群临时会话事件。
 *
 * @author ForteScarlet
 */
public interface MiraiMemberMessageEvent
    : MiraiSimbotContactMessageEvent<OriginalMiraiGroupTempMessageEvent>,
    ContactMessageEvent, ReplySupport, SendSupport {
    
    override val bot: MiraiBot
    
    /**
     * 发送消息的群成员。
     */
    @JSTP
    override suspend fun user(): MiraiMember
    
    
    /**
     * 发送消息的群成员。同 [user]。
     */
    @JSTP
    override suspend fun source(): MiraiMember = user()
    
    
    // region reply api
    
    /**
     * 回复此群成员的消息。效果等同于 [send].
     */
    @JST
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    
    /**
     * 回复此群成员的消息。效果等同于 [send].
     */
    @JST
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    
    /**
     * 回复此群成员的消息。效果等同于 [send].
     */
    @JST
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    // endregion
    
    // region send api
    /**
     * 向此群成员发送消息。效果等同于 [reply].
     */
    @JST
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    
    /**
     * 向此群成员发送消息。效果等同于 [reply].
     */
    @JST
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    
    
    /**
     * 向此群成员发送消息。效果等同于 [reply].
     */
    @JST
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    // endregion
    
    override val key: Event.Key<MiraiMemberMessageEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiMemberMessageEvent>(
        "mirai.group_temp_message",
        MiraiSimbotContactMessageEvent, ContactMessageEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberMessageEvent? = doSafeCast(value)
    }
    
}
