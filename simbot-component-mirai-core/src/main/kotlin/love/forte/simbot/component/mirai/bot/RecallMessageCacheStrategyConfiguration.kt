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

package love.forte.simbot.component.mirai.bot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.component.mirai.CustomPropertiesMiraiRecallMessageCacheStrategy
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
    /*
        希望提供更多配置类型？协助我们 --> https://github.com/simple-robot/simbot-component-mirai/pulls
     */
    
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
     *
     * @property groupMaxSize 缓存消息的最大上限, 会根据 [loadFactor] 计算为最终的初始化容量
     * @property friendMaxSize 缓存好友消息的最大上限, 会根据 [loadFactor] 计算为最终的初始化容量
     * @property loadFactor 内部哈希表所使用的负载因子
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
    
    /**
     * 代表为提供一个 [CustomPropertiesMiraiRecallMessageCacheStrategy] 的具体实现类型 [className] 来进行构建。
     *
     * 提供的 [className] 类型必须至少存在一个无参公开构造用来进行实例化。
     *
     * ```json
     * {
     *   "type": "custom_properties",
     *   "className": "com.example.FooCustomPropertiesMiraiRecallMessageCacheStrategyImpl",
     *   "properties": {
     *      "foo": "foo",
     *      "tar": "bar"
     *   }
     * }
     * ```
     *
     */
    @Serializable
    @SerialName(CustomProperties.TYPE)
    public data class CustomProperties(
        public val className: String,
        public val properties: Map<String, String> = emptyMap()
    ) : RecallMessageCacheStrategyConfiguration() {
        override fun recallMessageCacheStrategy(config: MiraiBotVerifyInfoConfiguration): MiraiRecallMessageCacheStrategy {
            val loader = javaClass.classLoader ?: Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()
            val clazz = try {
                loader.loadClass(className)
            } catch (cnf: ClassNotFoundException) {
                throw IllegalArgumentException("Class [$className] not found")
            }
            
            if (!CustomPropertiesMiraiRecallMessageCacheStrategy::class.java.isAssignableFrom(clazz)) {
                throw IllegalArgumentException("The class [$className] does not extend from [CustomPropertiesMiraiRecallMessageCacheStrategy]")
            }
            
            val constructor = try {
                clazz.getConstructor()
            } catch (nsm: NoSuchMethodException) {
                throw IllegalStateException("A no-argument public constructor must exist for class [$className]")
            }
            
            return (constructor.newInstance() as CustomPropertiesMiraiRecallMessageCacheStrategy).also {
                it.properties = properties
            }
        }
        
        public companion object {
            public const val TYPE: String = "custom_properties"
        }
    }
    
    
    
    public companion object {
    
        /**
         * 得到 [Invalid]。
         *
         */
        @JvmStatic
        public fun invalid(): RecallMessageCacheStrategyConfiguration = Invalid
    
    
        /**
         * 得到 [MemoryLru]。
         *
         * @param groupMaxSize 缓存消息的最大上限, 会根据 [loadFactor] 计算为最终的初始化容量
         * @param friendMaxSize 缓存好友消息的最大上限, 会根据 [loadFactor] 计算为最终的初始化容量
         * @param loadFactor 内部哈希表所使用的负载因子
         *
         */
        @JvmStatic
        @JvmOverloads
        public fun memoryLru(
            groupMaxSize: Int = MemoryLruMiraiRecallMessageCacheStrategy.DEFAULT_GROUP_MAX_SIZE,
            friendMaxSize: Int = MemoryLruMiraiRecallMessageCacheStrategy.DEFAULT_FRIEND_MAX_SIZE,
            loadFactor: Float = MemoryLruMiraiRecallMessageCacheStrategy.DEFAULT_LOAD_FACTOR,
        ): RecallMessageCacheStrategyConfiguration = MemoryLru(groupMaxSize, friendMaxSize, loadFactor)
    
    
        /**
         * 得到 [CustomProperties]。
         */
        @JvmStatic
        @JvmOverloads
        public fun customProperties(
            className: String,
            properties: Map<String, String> = emptyMap()
        ): RecallMessageCacheStrategyConfiguration = CustomProperties(className, properties)
    
    }
}
