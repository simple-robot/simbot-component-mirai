/*
 *  Copyright (c) 2022-2022 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU 通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU 通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU 通用公共许可证的复本。如果没有，请看:
 *  https://www.gnu.org/licenses
 *  https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *  https://www.gnu.org/licenses/lgpl-3.0-standalone.html
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

