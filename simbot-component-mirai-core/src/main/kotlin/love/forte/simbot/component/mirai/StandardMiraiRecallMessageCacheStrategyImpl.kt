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

import love.forte.simbot.component.mirai.MemoryLruMiraiRecallMessageCacheStrategy.Companion.DEFAULT_FRIEND_MAX_SIZE
import love.forte.simbot.component.mirai.MemoryLruMiraiRecallMessageCacheStrategy.Companion.DEFAULT_GROUP_MAX_SIZE
import love.forte.simbot.component.mirai.bot.MiraiBot
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.data.MessageChain
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * 标准撤回消息缓存策略，
 * 由组件所提供的 [MiraiRecallMessageCacheStrategy] 标准实现类型。
 *
 * ## 更多标准策略?
 * [StandardMiraiRecallMessageCacheStrategy] 的实现应当拥有明确且泛用的适用场景、不应需要额外依赖、不会过于复杂且庞大。
 *
 * 如果您有符合上述条件的策略实现需求，可以通过 [Pull Requests](https://github.com/simple-robot/simbot-component-mirai/pulls)
 * 贡献您的方案。
 *
 * @see InvalidMiraiRecallMessageCacheStrategy
 * @see MemoryLruMiraiRecallMessageCacheStrategy
 */
public interface StandardMiraiRecallMessageCacheStrategy : MiraiRecallMessageCacheStrategy


/**
 * [MiraiRecallMessageCacheStrategy] 的最简实现，无效的缓存策略，即**不进行缓存**。
 *
 * [InvalidMiraiRecallMessageCacheStrategy] 理所当然的是处理最快的缓存策略，
 * 但使用后无法再从撤回事件中得到 [撤回的消息内容][love.forte.simbot.component.mirai.event.MiraiMessageRecallEvent.messages]。
 *
 * [InvalidMiraiRecallMessageCacheStrategy] 是 [MiraiBotConfiguration.recallCacheStrategy] 的默认策略。
 *
 */
public object InvalidMiraiRecallMessageCacheStrategy : StandardMiraiRecallMessageCacheStrategy {
    override fun cacheGroupMessageEvent(bot: MiraiBot, event: GroupMessageEvent) {
        // do nothing
    }
    
    override fun cacheFriendMessageEvent(bot: MiraiBot, event: FriendMessageEvent) {
        // do nothing
    }
    
    override fun getGroupMessageCache(bot: MiraiBot, event: MessageRecallEvent.GroupRecall): MessageChain? {
        return null
    }
    
    override fun getFriendMessageCache(bot: MiraiBot, event: MessageRecallEvent.FriendRecall): MessageChain? {
        return null
    }
    
    override fun invokeOnBotCompletion(bot: MiraiBot, cause: Throwable?) {
        // nothing.
    }
}


/**
 * 基于内存的 `LRU` 缓存策略实现。
 *
 * [MemoryLruMiraiRecallMessageCacheStrategy] 会在内存中内建缓存用的LRU Map来对消息进行缓存，
 * 因此 [MemoryLruMiraiRecallMessageCacheStrategy] 会需要占用更多的内存来进行消息缓存。
 *
 * 缓存会区分bot和群id/好友id。因此不同bot下不同的群/好友之间的缓存数量上限是分开计算的。
 * 默认情况下，单个群/好友的消息缓存上限分别为 [DEFAULT_GROUP_MAX_SIZE] 和 [DEFAULT_FRIEND_MAX_SIZE]。
 *
 *
 */
public class MemoryLruMiraiRecallMessageCacheStrategy(
    /** 缓存消息的最大上限，会根据 [loadFactor] 计算为最终的初始化容量 */
    groupMaxSize: Int = DEFAULT_GROUP_MAX_SIZE,
    /** 缓存好友消息的最大上限，会根据 [loadFactor] 计算为最终的初始化容量 */
    friendMaxSize: Int = DEFAULT_FRIEND_MAX_SIZE,
    /** 内部哈希表所使用的负载因子 */
    private val loadFactor: Float = DEFAULT_LOAD_FACTOR,
) : StandardMiraiRecallMessageCacheStrategy {
    private val caches = ConcurrentHashMap<Long, BotCacheSegment>()
    private val groupInitSize = mapInitSize(groupMaxSize, loadFactor)
    private val friendInitSize = mapInitSize(friendMaxSize, loadFactor)
    
    private fun createSimpleLruMap(maxSize: Int): SimpleLruMap<String, MessageChain> {
        return SimpleLruMap(maxSize, maxSize, loadFactor)
    }
    
    private fun MiraiBot.getCacheSegment(): BotCacheSegment {
        return caches.computeIfAbsent(id.value) { BotCacheSegment(ConcurrentHashMap(), ConcurrentHashMap()) }
    }
    
    private fun MiraiBot.getGroupCacheSegment(groupId: Long): CacheSegment {
        return getCacheSegment().groupCache.computeIfAbsent(groupId) {
            CacheSegment(ReentrantReadWriteLock(), createSimpleLruMap(groupInitSize))
        }
    }
    
    private fun MiraiBot.getFriendCacheSegment(friendId: Long): CacheSegment {
        return getCacheSegment().friendCache.computeIfAbsent(friendId) {
            CacheSegment(ReentrantReadWriteLock(), createSimpleLruMap(friendInitSize))
        }
    }
    
    
    override fun cacheGroupMessageEvent(bot: MiraiBot, event: GroupMessageEvent) {
        val messageChain = event.message
        val cacheId = event.cacheId
        bot.getGroupCacheSegment(event.group.id).write {
            it[cacheId] = messageChain
        }
    }
    
    override fun cacheFriendMessageEvent(bot: MiraiBot, event: FriendMessageEvent) {
        val messageChain = event.message
        val cacheId = event.cacheId
        bot.getFriendCacheSegment(event.friend.id).write {
            it[cacheId] = messageChain
        }
        
    }
    
    override fun getGroupMessageCache(bot: MiraiBot, event: MessageRecallEvent.GroupRecall): MessageChain? {
        val segment = bot.getGroupCacheSegment(event.group.id)
        val cacheId = event.cacheId
        return segment.read { it[cacheId] }
    }
    
    override fun getFriendMessageCache(bot: MiraiBot, event: MessageRecallEvent.FriendRecall): MessageChain? {
        val segment = bot.getFriendCacheSegment(event.authorId)
        val cacheId = event.cacheId
        return segment.read { it[cacheId] }
    }
    
    override fun invokeOnBotCompletion(bot: MiraiBot, cause: Throwable?) {
        caches.remove(bot.id.value)?.clear()
    }
    
    private data class BotCacheSegment(
        val groupCache: ConcurrentHashMap<Long, CacheSegment>,
        val friendCache: ConcurrentHashMap<Long, CacheSegment>,
    ) {
        fun clear() {
            groupCache.clear()
            friendCache.clear()
        }
    }
    
    
    private data class CacheSegment(
        private val lock: ReentrantReadWriteLock,
        private val cache: SimpleLruMap<String, MessageChain>,
    ) {
        inline fun <T> read(block: (SimpleLruMap<String, MessageChain>) -> T): T {
            return lock.read {
                block(cache)
            }
        }
        
        inline fun <T> write(block: (SimpleLruMap<String, MessageChain>) -> T): T {
            return lock.write {
                block(cache)
            }
        }
    }
    
    
    private class SimpleLruMap<K, V>(
        initialCapacity: Int,
        private val maxSize: Int,
        loadFactor: Float = DEFAULT_LOAD_FACTOR,
    ) :
        LinkedHashMap<K, V>(initialCapacity, loadFactor, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size >= maxSize
        }
        
        companion object {
        }
    }
    
    private val MessageRecallEvent.FriendRecall.cacheId: String
        get() {
            // md5..?
            return buildString(64) {
                messageIds.joinTo(this, separator = ".", postfix = ",")
                messageInternalIds.joinTo(this, separator = ".", postfix = ",")
                append(',')
                append(messageTime)
            }
        }
    private val MessageRecallEvent.GroupRecall.cacheId: String
        get() {
            return buildString(64) {
                messageIds.joinTo(this, separator = ".", postfix = ",")
                messageInternalIds.joinTo(this, separator = ".", postfix = ",")
                append(',')
                append(messageTime)
            }
        }
    
    private val FriendMessageEvent.cacheId: String
        get() {
            return buildString(64) {
                source.ids.joinTo(this, separator = ".", postfix = ",")
                source.internalIds.joinTo(this, separator = ".", postfix = ",")
                append(',')
                append(source.time)
            }
        }
    private val GroupMessageEvent.cacheId: String
        get() {
            return buildString(64) {
                source.ids.joinTo(this, separator = ".", postfix = ",")
                source.internalIds.joinTo(this, separator = ".", postfix = ",")
                append(',')
                append(source.time)
            }
        }
    
    private fun mapInitSize(maxSize: Int, loadFactor: Float): Int {
        val value = (maxSize / loadFactor).toInt()
        return if (isTableSize(value)) value else value + 1
    }
    
    private fun isTableSize(cap: Int): Boolean {
        var n = cap - 1
        n = n or (n ushr 1)
        n = n or (n ushr 2)
        n = n or (n ushr 4)
        n = n or (n ushr 8)
        n = n or (n ushr 16)
        return (n + 1) == cap
    }
    
    public companion object {
        public const val DEFAULT_GROUP_MAX_SIZE: Int = 1536
        public const val DEFAULT_FRIEND_MAX_SIZE: Int = 96
        public const val DEFAULT_LOAD_FACTOR: Float = 0.75F
    }
}



