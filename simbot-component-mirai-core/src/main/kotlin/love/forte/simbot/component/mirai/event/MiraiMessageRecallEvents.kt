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
 *
 */

package love.forte.simbot.component.mirai.event

import love.forte.simbot.Api4J
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.definition.Friend
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
public abstract class MiraiFriendMessageRecallEvent : MiraiMessageRecallEvent<MessageRecallEvent.FriendRecall>(),
    FriendEvent {
    
    @OptIn(Api4J::class)
    abstract override val friend: MiraiFriend
    
    override suspend fun friend(): MiraiFriend = friend
    
    @OptIn(Api4J::class)
    override val user: Friend
        get() = friend
    
    override suspend fun user(): Friend = friend
    
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
    
    @OptIn(Api4J::class)
    abstract override val group: MiraiGroup
    
    @OptIn(Api4J::class)
    override val organization: MiraiGroup get() = group
    
    override suspend fun group(): MiraiGroup = group
    
    override suspend fun organization(): MiraiGroup = group
    
    override val key: Event.Key<out MiraiGroupMessageRecallEvent>
        get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupMessageRecallEvent>(
        "mirai.group_message_recall", MiraiMessageRecallEvent, GroupEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupMessageRecallEvent? = doSafeCast(value)
    }
}

