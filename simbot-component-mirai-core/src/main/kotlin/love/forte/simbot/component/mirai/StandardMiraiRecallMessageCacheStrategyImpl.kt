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
 * 由组件所提供的 [MiraiRecallMessageCacheStrategy] 标准实现类型。
 *
 * ## 更多标准策略?
 * [StandardMiraiRecallMessageCacheStrategy] 的实现应当拥有明确且泛用的适用场景、不应需要额外依赖、不会过于复杂且庞大。
 *
 * 如果您有符合上述条件的策略实现需求，可以通过 [Pull requests](https://github.com/simple-robot/simbot-component-mirai/pulls)
 * 贡献您的方案。
 *
 * @see InvalidMiraiRecallMessageCacheStrategy
 * @see MemoryLruMiraiRecallMessageCacheStrategy
 */
public interface StandardMiraiRecallMessageCacheStrategy : MiraiRecallMessageCacheStrategy


/**
 * [MiraiRecallMessageCacheStrategy] 的最简实现，无效的缓存策略，即**不进行缓存**。
 *
 * [InvalidMiraiRecallMessageCacheStrategy] 是处理最快的缓存策略，
 * 但使用后无法再从撤回事件中得到 [撤回的消息内容][love.forte.simbot.component.mirai.event.MiraiMessageRecallEvent.messages]。
 *
 * [InvalidMiraiRecallMessageCacheStrategy] 将是 [MiraiBotConfiguration] 的默认策略。
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
 */
public class MemoryLruMiraiRecallMessageCacheStrategy(
    private val groupMaxSize: Int = DEFAULT_GROUP_MAX_SIZE,
    private val friendMaxSize: Int = DEFAULT_FRIEND_MAX_SIZE,
) : StandardMiraiRecallMessageCacheStrategy {
    private val caches = ConcurrentHashMap<Long, BotCacheSegment>()
    
    private fun MiraiBot.getCacheSegment(): BotCacheSegment {
        return caches.computeIfAbsent(id.value) { BotCacheSegment(ConcurrentHashMap(), ConcurrentHashMap()) }
    }
    
    private fun MiraiBot.getGroupCacheSegment(groupId: Long): CacheSegment {
        return getCacheSegment().groupCache.computeIfAbsent(groupId) {
            CacheSegment(ReentrantReadWriteLock(), SimpleLruMap(mapInitSize(groupMaxSize), groupMaxSize))
        }
    }
    
    private fun MiraiBot.getFriendCacheSegment(friendId: Long): CacheSegment {
        return getCacheSegment().friendCache.computeIfAbsent(friendId) {
            CacheSegment(ReentrantReadWriteLock(), SimpleLruMap(mapInitSize(friendMaxSize), friendMaxSize))
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
        caches.clear()
    }
    
    private data class BotCacheSegment(
        val groupCache: ConcurrentHashMap<Long, CacheSegment>,
        val friendCache: ConcurrentHashMap<Long, CacheSegment>,
    )
    
    
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
    
    
    private class SimpleLruMap<K, V>(initialCapacity: Int, private val maxSize: Int) :
        LinkedHashMap<K, V>(initialCapacity, 0.75F, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size >= maxSize
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
    
    private fun mapInitSize(maxSize: Int): Int {
        return ((maxSize / 0.75F) + 1).toInt()
    }
    
    public companion object {
        private const val BASE_DEFAULT_GROUP_MAX_SIZE: Int = 1024
        private const val BASE_DEFAULT_FRIEND_MAX_SIZE: Int = 128
        
        public const val DEFAULT_GROUP_MAX_SIZE: Int = (BASE_DEFAULT_GROUP_MAX_SIZE * 0.75F).toInt() - 1
        public const val DEFAULT_FRIEND_MAX_SIZE: Int = (BASE_DEFAULT_FRIEND_MAX_SIZE * 0.75F).toInt() - 1
    }
}






