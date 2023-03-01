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

import love.forte.simbot.SimbotIllegalStateException
import love.forte.simbot.component.mirai.JST
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.definition.FriendInfoContainer
import love.forte.simbot.event.BaseEvent
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.toPlainText
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
@BaseEvent
public interface MiraiMessagePostSendEvent<C : net.mamoe.mirai.contact.Contact, E : OriginalMiraiMessagePostSendEvent<C>> :
    MiraiSimbotBotEvent<E>, MessageEvent {
    override val bot: MiraiBot
    
    
    /**
     * 尝试引用回复发送的消息。
     *
     * 如果发送失败（[originalEvent] 中的 [receipt][MessageReceipt] 为null）则会抛出 [SimbotIllegalStateException]
     *
     * @throws SimbotIllegalStateException 无法引用回复时
     */
    @JST
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<C> {
        val quote = originalEvent.receipt?.quote()
            ?: throw SimbotIllegalStateException("Cannot reply this event: $this: the originalEvent.receipt is null")
        val target = originalEvent.target
        val result = target.sendMessage(quote + message.toOriginalMiraiMessage(target))
        @Suppress("UNCHECKED_CAST")
        return SimbotMiraiMessageReceiptImpl(result as MessageReceipt<C>)
    }
    
    /**
     * 尝试引用回复发送的消息。
     *
     * 如果发送失败（[originalEvent] 中的 [receipt][MessageReceipt] 为null）则会抛出 [SimbotIllegalStateException]
     *
     * @throws SimbotIllegalStateException 无法引用回复时
     */
    @JST
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<C> {
        val quote = originalEvent.receipt?.quote()
            ?: throw SimbotIllegalStateException("Cannot reply this event: $this: the originalEvent.receipt is null")
        val target = originalEvent.target
        val result = target.sendMessage(quote + text.toPlainText())
        @Suppress("UNCHECKED_CAST")
        return SimbotMiraiMessageReceiptImpl(result as MessageReceipt<C>)
    }
    
    /**
     * 尝试引用回复发送的消息。
     *
     * 如果发送失败（[originalEvent] 中的 [receipt][MessageReceipt] 为null）则会抛出 [SimbotIllegalStateException]
     *
     * @throws SimbotIllegalStateException 无法引用回复时
     */
    @JST
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<C> {
        return reply(message.messages)
    }
    
    public companion object Key : BaseEventKey<MiraiMessagePostSendEvent<*, *>>(
        "mirai.message_post_send", MiraiSimbotBotEvent
    ) {
        override fun safeCast(value: Any): MiraiMessagePostSendEvent<*, *>? = doSafeCast(value)
    }
}




