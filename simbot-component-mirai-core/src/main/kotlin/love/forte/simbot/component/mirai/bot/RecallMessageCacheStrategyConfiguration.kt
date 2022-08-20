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

package love.forte.simbot.component.mirai.bot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.component.mirai.InvalidMiraiRecallMessageCacheStrategy
import love.forte.simbot.component.mirai.MemoryLruMiraiRecallMessageCacheStrategy
import love.forte.simbot.component.mirai.MiraiRecallMessageCacheStrategy
import love.forte.simbot.component.mirai.internal.InternalApi

/**
 * 应用于 [MiraiBotVerifyInfoConfiguration.Config.recallMessageCacheStrategyConfig] 属性的撤回消息缓存策略配置。
 */
@Serializable
@OptIn(InternalApi::class)
public sealed class RecallMessageCacheStrategyConfiguration {
    /**
     * 提供 [MiraiBotVerifyInfoConfiguration] 信息，计算并得到一个缓存策略 [MiraiRecallMessageCacheStrategy]。
     */
    public abstract fun recallMessageCacheStrategy(config: MiraiBotVerifyInfoConfiguration): MiraiRecallMessageCacheStrategy
    
    /**
     * 代表为使用 [InvalidMiraiRecallMessageCacheStrategy] ，
     * 即使用本质上**不缓存**的缓存策略。
     *
     * ```json
     * {
     *    "type": "invalid"
     * }
     * ```
     */
    @Serializable
    @SerialName(Invalid.TYPE)
    public object Invalid : RecallMessageCacheStrategyConfiguration() {
        public const val TYPE: String = "invalid"
        
        override fun recallMessageCacheStrategy(config: MiraiBotVerifyInfoConfiguration): MiraiRecallMessageCacheStrategy =
            InvalidMiraiRecallMessageCacheStrategy
    }
    
    /**
     * 代表为使用 [MemoryLruMiraiRecallMessageCacheStrategy] ，
     * 即使用基于内存的LRU缓存策略。
     *
     * ```json
     * {
     *    "type": "memory_lru",
     *    "groupMaxSize": 1536,
     *    "friendMaxSize": 96
     * }
     * ```
     */
    @Serializable
    @SerialName(MemoryLru.TYPE)
    public data class MemoryLru(
        public val groupMaxSize: Int = MemoryLruMiraiRecallMessageCacheStrategy.DEFAULT_GROUP_MAX_SIZE,
        public val friendMaxSize: Int = MemoryLruMiraiRecallMessageCacheStrategy.DEFAULT_FRIEND_MAX_SIZE,
        public val loadFactor: Float = MemoryLruMiraiRecallMessageCacheStrategy.DEFAULT_LOAD_FACTOR
    ) : RecallMessageCacheStrategyConfiguration() {
        
        override fun recallMessageCacheStrategy(config: MiraiBotVerifyInfoConfiguration): MiraiRecallMessageCacheStrategy {
            return MemoryLruMiraiRecallMessageCacheStrategy(groupMaxSize, friendMaxSize)
        }
        
        public companion object {
            public const val TYPE: String = "memory_lru"
        }
    }
    
}
