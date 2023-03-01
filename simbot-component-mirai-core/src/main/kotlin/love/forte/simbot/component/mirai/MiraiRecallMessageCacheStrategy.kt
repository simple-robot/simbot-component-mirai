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

package love.forte.simbot.component.mirai

import love.forte.simbot.component.mirai.bot.MiraiBot
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.data.MessageChain

/**
 *
 * Mirai撤回消息缓存策略。
 *
 * 主要用于 [MiraiMessageRecallEvent][love.forte.simbot.component.mirai.event.MiraiMessageRecallEvent],
 * 在事件中缓存群消息与好友消息，并在 [MiraiMessageRecallEvent][love.forte.simbot.component.mirai.event.MiraiMessageRecallEvent]
 * 中进行消息读取。
 *
 * [MiraiRecallMessageCacheStrategy] 中的缓存函数 (`onXxxMessageEvent`)
 * 都将直接操作Mirai的事件对象，这些事件会发生在simbot事件被真正触发之前。
 * 消息缓存的处理应当是迅速的，否则这会严重影响到正常事件的处理。
 *
 * ## 标准实现
 *
 * [MiraiRecallMessageCacheStrategy] 提供了一些可供选择的 [默认标准实现][StandardMiraiRecallMessageCacheStrategy]，
 * 它们可以满足大多数情况下的基本策略需求:
 * - [InvalidMiraiRecallMessageCacheStrategy]
 * - [MemoryLruMiraiRecallMessageCacheStrategy]
 *
 *
 * @author ForteScarlet
 */
public interface MiraiRecallMessageCacheStrategy {
    
    /**
     * 记录mirai的群消息事件的消息缓存。
     */
    public fun cacheGroupMessageEvent(bot: MiraiBot, event: GroupMessageEvent)
    
    /**
     * 记录mirai的好友消息事件的缓存。
     */
    public fun cacheFriendMessageEvent(bot: MiraiBot, event: FriendMessageEvent)
    
    /**
     * 获取群撤回事件所对应的mirai消息链对象。
     */
    public fun getGroupMessageCache(bot: MiraiBot, event: MessageRecallEvent.GroupRecall): MessageChain?
    
    /**
     * 获取好友撤回事件所对应的mirai消息链对象。
     */
    public fun getFriendMessageCache(bot: MiraiBot, event: MessageRecallEvent.FriendRecall): MessageChain?
    
    /**
     * 当 [MiraiBot] 被关闭或结束时。此函数会在启动时通过 [MiraiBot.invokeOnCompletion] 注册。
     */
    public fun invokeOnBotCompletion(bot: MiraiBot, cause: Throwable?)
    
}

