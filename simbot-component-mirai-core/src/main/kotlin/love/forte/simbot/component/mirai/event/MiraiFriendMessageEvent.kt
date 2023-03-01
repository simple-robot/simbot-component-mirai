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
import love.forte.simbot.component.mirai.*
import love.forte.simbot.event.*
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger
import net.mamoe.mirai.event.events.FriendMessageEvent as OriginalMiraiFriendMessageEvent
import net.mamoe.mirai.event.events.StrangerMessageEvent as OriginalMiraiStrangerMessageEvent


/**
 * 好友消息事件。
 *
 * Mirai [OriginalMiraiFriendMessageEvent] 事件对应的 [FriendMessageEvent] 事件类型。
 *
 * 戳一戳消息事件为独立的事件类型，参考 [MiraiFriendNudgeEvent].
 *
 * @see OriginalMiraiFriendMessageEvent
 * @author ForteScarlet
 */
public interface MiraiFriendMessageEvent :
    MiraiSimbotContactMessageEvent<OriginalMiraiFriendMessageEvent>,
    MiraiFriendEvent<OriginalMiraiFriendMessageEvent>,
    FriendMessageEvent, ReplySupport, SendSupport {
    override val key: Event.Key<MiraiFriendMessageEvent> get() = Key
    
    /**
     * 涉及到的好友，同 [friend]。
     */
    @JSTP
    override suspend fun user(): MiraiFriend = friend()
    
    /**
     * 涉及到的好友，同 [friend]。
     */
    @JSTP
    override suspend fun source(): MiraiFriend = friend()
    
    // region send api
    @JST
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @JST
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @JST
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    // endregion
    
    
    // region reply api
    @JST
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    
    @JST
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    
    @JST
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    // endregion
    
    public companion object Key :
        BaseEventKey<MiraiFriendMessageEvent>(
            "mirai.friend_message",
            MiraiSimbotContactMessageEvent, MiraiFriendEvent, FriendMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiFriendMessageEvent? = doSafeCast(value)
    }
}


/**
 * Mirai陌生人消息事件。
 */
public interface MiraiStrangerMessageEvent :
    MiraiSimbotContactMessageEvent<OriginalMiraiStrangerMessageEvent>,
    ContactMessageEvent, ReplySupport, SendSupport {
    override val key: Event.Key<MiraiStrangerMessageEvent> get() = Key
    
    @JSTP
    override suspend fun user(): MiraiStranger
    
    
    @JSTP
    override suspend fun source(): MiraiStranger = user()
    
    
    // region reply api
    @JST
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    @JST
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    @JST
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    // endregion
    
    
    // region send api
    @JST
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    @JST
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    @JST
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    // endregion
    
    
    public companion object Key :
        BaseEventKey<MiraiStrangerMessageEvent>(
            "mirai.stranger_message",
            MiraiSimbotContactMessageEvent, ContactMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiStrangerMessageEvent? = doSafeCast(value)
    }
}
