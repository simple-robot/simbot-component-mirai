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
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.Event
import love.forte.simbot.event.FriendEvent
import love.forte.simbot.event.GroupEvent
import love.forte.simbot.message.Messages
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.event.events.MessageRecallEvent

/**
 * 消息撤回事件。
 *
 * @see MessageRecallEvent
 */
public sealed class MiraiMessageRecallEvent<E : MessageRecallEvent> : MiraiSimbotEvent<E> {
    /**
     * 尝试得到被撤回的消息内容。
     *
     * 当存在下列情况的时候，被撤回的消息可能为 `null`:
     * - 被撤回的消息是在bot离线期间发送的（缓存消息只有在bot启动时才生效）
     * - 消息撤回事件无法在缓存中查询到对应的消息
     * - 其他未知的异常
     */
    public abstract val messages: Messages?
    
    public companion object Key : BaseEventKey<MiraiMessageRecallEvent<*>>(
        "mirai.message_recall", MiraiSimbotEvent
    ) {
        override fun safeCast(value: Any): MiraiMessageRecallEvent<*>? = doSafeCast(value)
    }
}

/**
 * Mirai的好友消息撤回事件。
 */
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public abstract class MiraiFriendMessageRecallEvent : MiraiMessageRecallEvent<MessageRecallEvent.FriendRecall>(),
    FriendEvent {
    
    /**
     * 事件涉及的好友。
     */
    abstract override suspend fun friend(): MiraiFriend
    
    /**
     * 事件涉及的好友。同 [friend]。
     */
    override suspend fun user(): MiraiFriend = friend()
    
    override val key: Event.Key<out MiraiFriendMessageRecallEvent>
        get() = Key
    
    public companion object Key : BaseEventKey<MiraiFriendMessageRecallEvent>(
        "mirai.friend_message_recall", MiraiMessageRecallEvent, FriendEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendMessageRecallEvent? = doSafeCast(value)
    }
}

/**
 * Mirai的群消息撤回事件。
 */
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public abstract class MiraiGroupMessageRecallEvent : MiraiMessageRecallEvent<MessageRecallEvent.GroupRecall>(),
    GroupEvent {
    
    /**
     * 消息发送者。
     */
    public abstract val author: MiraiMember
    
    /**
     * 消息撤回者。如果为null则为 bot 撤回的。
     */
    public abstract val operator: MiraiMember?
    
    /**
     * 事件涉及的群。
     */
    abstract override suspend fun group(): MiraiGroup
    
    /**
     * 事件涉及的群。同 [group]。
     */
    override suspend fun organization(): MiraiGroup = group()
    
    override val key: Event.Key<out MiraiGroupMessageRecallEvent>
        get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupMessageRecallEvent>(
        "mirai.group_message_recall", MiraiMessageRecallEvent, GroupEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupMessageRecallEvent? = doSafeCast(value)
    }
}

