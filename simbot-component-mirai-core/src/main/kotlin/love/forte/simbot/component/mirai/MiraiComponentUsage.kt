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

import love.forte.simbot.ability.CompletionPerceivable
import love.forte.simbot.application.Application
import love.forte.simbot.application.ApplicationBuilder
import love.forte.simbot.application.ApplicationBuilderDsl
import love.forte.simbot.component.mirai.bot.MiraiBotManager
import love.forte.simbot.component.mirai.bot.MiraiBotManagerConfiguration


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
@ApplicationBuilderDsl
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


