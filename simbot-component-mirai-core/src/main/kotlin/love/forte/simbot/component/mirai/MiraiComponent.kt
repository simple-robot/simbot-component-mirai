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

@file:JvmName("MiraiComponents")

package love.forte.simbot.component.mirai

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import love.forte.simbot.*
import love.forte.simbot.ability.CompletionPerceivable
import love.forte.simbot.application.Application
import love.forte.simbot.application.ApplicationBuilder
import love.forte.simbot.application.ApplicationBuilderDsl
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.message.Message
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.utils.MiraiExperimentalApi


/**
 * simbot对应的mirai组件。
 */
public class MiraiComponent : Component {
    /**
     * 代表组件的唯一标识。
     */
    override val id: ID get() = ComponentID
    
    /**
     * 得到 [MiraiComponent] 所使用的消息序列化信息。
     */
    override val componentSerializersModule: SerializersModule
        get() = messageSerializersModule
    
    public companion object Factory : ComponentFactory<MiraiComponent, MiraiComponentConfiguration> {
        /**
         * [MiraiComponent] 的组件标识ID。
         */
        @Suppress("MemberVisibilityCanBePrivate")
        public const val ID: String = "simbot.mirai"
        
        @JvmField
        public val ComponentID: ID = ID.ID
        
        override val key: Attribute<MiraiComponent> = attribute(ID)
        
        override fun create(configurator: MiraiComponentConfiguration.() -> Unit): MiraiComponent {
            // nothing now
            // val config = MiraiComponentConfiguration().also(block)
            
            return MiraiComponent()
        }
        
        /**
         * 得到 [MiraiComponent] 所使用的消息序列化信息。
         */
        @JvmStatic
        public val messageSerializersModule: SerializersModule =
            MessageSerializers.serializersModule + SerializersModule {
                polymorphic(Message.Element::class) {
                    subclass(SimbotOriginalMiraiMessage.serializer())
                    
                    ////
                    
                    polymorphic(MiraiImage::class) {
                        subclass(MiraiImageImpl.serializer())
                    }
                    subclass(MiraiImageImpl.serializer())
                    
                    ////
                    
                    polymorphic(MiraiAudio::class) {
                        subclass(MiraiAudioImpl.serializer())
                    }
                    subclass(MiraiAudioImpl.serializer())
                    
                    ////
                    @OptIn(MiraiExperimentalApi::class) subclass(MiraiShare.serializer())
                    subclass(MiraiQuoteReply.serializer())
                    subclass(MiraiMusicShare.serializer())
                    subclass(MiraiNudge.serializer())
                    subclass(MiraiReceivedNudge.serializer())
                }
            }
    }
}


/**
 * [MiraiComponent] 注册的时候所使用的配置类。
 */
public open class MiraiComponentConfiguration


/**
 * 支持进行自动加载的组件配置工厂。
 */
public class MiraiComponentAutoRegistrarFactory :
    ComponentAutoRegistrarFactory<MiraiComponent, MiraiComponentConfiguration> {
    override val registrar: MiraiComponent.Factory
        get() = MiraiComponent
}


// region 组件、provider的安装

/**
 * 安装使用 [MiraiBotManager].
 *
 * e.g.:
 * ```kotlin
 * simbotApplication(Foo) {
 *
 *  useMiraiBotManager()
 *  // 或
 *  useMiraiBotManager {
 *      // config...
 *  }
 *
 * }
 * ```
 *
 * 相当于:
 * ```kotlin
 * simbotApplication(Foo) {
 *  install(MiraiBotManager) { ... }
 *  // ...
 * }
 * ```
 */
@JvmOverloads
@ApplicationBuilderDsl
public fun <A : Application> ApplicationBuilder<A>.useMiraiBotManager(configurator: MiraiBotManagerConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit = {}) {
    install(MiraiBotManager, configurator)
}


/**
 * 安装使用 [MiraiComponent].
 *
 * e.g.:
 * ```kotlin
 * simbotApplication(Foo) {
 *
 *  useMiraiComponent()
 *  // 或
 *  useMiraiComponent {
 *      // config...
 *  }
 *
 * }
 * ```
 *
 * 相当于:
 * ```kotlin
 * simbotApplication(Foo) {
 *  install(MiraiComponent) { ... }
 *  // ...
 * }
 * ```
 */
@JvmOverloads
@ApplicationBuilderDsl
public fun <A : Application> ApplicationBuilder<A>.useMiraiComponent(configurator: MiraiComponentConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit = {}) {
    install(MiraiComponent, configurator)
}

/**
 * 为 [MiraiComponentUsageBuilder] 提供DSL染色。
 */
@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
public annotation class MiraiComponentBuilderDsl

/**
 * 用于在 [useMirai] 函数中配置构建 [MiraiComponent] 和 [MiraiBotManager] 的构建器。
 *
 * @see useMirai
 */
public interface MiraiComponentUsageBuilder<A : Application> {
    
    /**
     * 配置 [MiraiComponent].
     * ```kotlin
     * useMirai {
     *   component {
     *       // config MiraiComponent
     *   }
     * }
     * ```
     */
    @MiraiComponentBuilderDsl
    public fun component(configurator: MiraiComponentConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit)
    
    /**
     * 配置 [MiraiBotManager].
     *
     * ```kotlin
     * useMirai {
     *   botManager {
     *      // config MiraiBotManager
     *   }
     * }
     *
     * ```
     */
    @MiraiComponentBuilderDsl
    public fun botManager(configurator: MiraiBotManagerConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit)
    
}


/**
 * 同时安装使用 [MiraiComponent] 和 [MiraiBotManager].
 *
 * e.g.
 * ```kotlin
 * simbotApplication(Foo) {
 *   useMirai {
 *      component {
 *          // config MiraiComponent...
 *      }
 *
 *      botManager {
 *          // config MiraiBotManager...
 *      }
 *   }
 *   // ...
 * }
 * ```
 *
 *
 * ```kotlin
 * simbotApplication(Foo) {
 *   useMirai()
 * }
 * ```
 *
 *
 * @see useMiraiComponent
 * @see useMiraiBotManager
 *
 */
public fun <A : Application> ApplicationBuilder<A>.useMirai(builder: MiraiComponentUsageBuilder<A>.() -> Unit = {}) {
    MiraiComponentUsageBuilderImpl<A>().also(builder).build(this)
}


private class MiraiComponentUsageBuilderImpl<A : Application> : MiraiComponentUsageBuilder<A> {
    private val componentConfigs =
        mutableListOf<MiraiComponentConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit>()
    private val botManagerConfigs =
        mutableListOf<MiraiBotManagerConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit>()
    
    override fun component(configurator: MiraiComponentConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit) {
        componentConfigs.add(configurator)
    }
    
    override fun botManager(configurator: MiraiBotManagerConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit) {
        botManagerConfigs.add(configurator)
    }
    
    fun build(builder: ApplicationBuilder<A>) {
        builder.install(MiraiComponent) {
            componentConfigs.forEach { config ->
                config(it)
            }
        }
        builder.install(MiraiBotManager) {
            botManagerConfigs.forEach { config ->
                config(it)
            }
        }
    }
}


// endregion


// region manager获取扩展
/**
 * 通过 [OriginBotManager] 获取所有的 [MiraiBotManager]。
 *
 * @see OriginBotManager
 */
@FragileSimbotApi
@Suppress("NOTHING_TO_INLINE")
public inline fun miraiComponents(): List<MiraiBotManager> = OriginBotManager.filterIsInstance<MiraiBotManager>()


/**
 * 获取其中为 [MiraiBotManager] 的管理器。
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Iterable<BotManager<*>>.filterIsMiraiBotManagers(): List<MiraiBotManager> =
    filterIsInstance<MiraiBotManager>()


/**
 * 过滤获取其中为 [MiraiBotManager] 的管理器。
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Sequence<BotManager<*>>.filterIsMiraiBotManagers(): Sequence<MiraiBotManager> =
    filterIsInstance<MiraiBotManager>()


/**
 * 得到 [OriginBotManager] 中的所有 mirai 组件。
 *
 * @see OriginBotManager
 */
@FragileSimbotApi
public inline val OriginBotManager.miraiBotManagers: List<MiraiBotManager> get() = filterIsMiraiBotManagers()
// endregion
