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

import love.forte.simbot.ability.CompletionPerceivable
import love.forte.simbot.application.*
import love.forte.simbot.component.mirai.bot.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


// region 组件、provider的安装

/**
 * 安装使用 [MiraiBotManager].
 *
 *  @suppress 使用 [useMiraiBotManager]
 * @see useMiraiBotManager
 */
@ApplicationBuilderDsl
@Deprecated("use useMiraiBotManager", ReplaceWith("useMiraiBotManager(configurator)"))
public fun <A : Application> ApplicationBuilder<A>.miraiBots(configurator: MiraiBotManagerConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit = {}) {
    useMiraiBotManager(configurator)
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
@JvmName("useMiraiComponent")
@JvmOverloads
public inline fun <A : Application> ApplicationBuilder<A>.useMiraiComponent(crossinline configurator: MiraiComponentConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit = {}) {
    install(MiraiComponent) {
        configurator(it)
    }
}

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
@JvmName("useMiraiBotManager")
@JvmOverloads
public inline fun <A : Application> ApplicationBuilder<A>.useMiraiBotManager(crossinline configurator: MiraiBotManagerConfiguration.(perceivable: CompletionPerceivable<A>) -> Unit = {}) {
    install(MiraiBotManager) {
        configurator(it)
    }
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
 * @see miraiBots
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

//region Application/BotManagers 用例
/**
 * 通过 [Application] 获取**第一个**存在的 [MiraiBotManager] 并使用它。
 *
 * ```kotlin
 * val application = createSimpleApplication {}
 * application.miraiBots { // this: MiraiBotManager
 *    // ...
 * }
 * ```
 *
 * @param failOnMiss 如果没有找到任何 [MiraiBotManager], 是否抛出 [NoSuchElementException] 异常。
 * @see BotManagers.miraiBots
 */
public inline fun Application.miraiBots(failOnMiss: Boolean, block: MiraiBotManager.() -> Unit) {
    botManagers.miraiBots(failOnMiss, block)
}

/**
 * 通过 [Application] 获取**第一个**存在的 [MiraiBotManager] 并使用它。
 *
 * ```kotlin
 * val application = createSimpleApplication {}
 * application.miraiBots { // this: MiraiBotManager
 *    // ...
 * }
 * ```
 *
 * @param block 执行函数，始终被执行，或者函数抛出异常
 *
 * @throws NoSuchElementException 如果没有找到任何 [MiraiBotManager]
 * @see BotManagers.miraiBots
 */
@OptIn(ExperimentalContracts::class)
public inline fun Application.miraiBots(block: MiraiBotManager.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    botManagers.miraiBots(true, block)
}

/**
 * 通过 [BotManagers] 获取**第一个**存在的 [MiraiBotManager] 并使用它。
 *
 * ```kotlin
 * val application = createSimpleApplication {}
 * application.botManagers.miraiBots { // this: MiraiBotManager
 *    // ...
 * }
 * ```
 *
 * @param block 执行函数，始终被执行，或者函数抛出异常
 * @throws NoSuchElementException 如果没有找到任何 [MiraiBotManager]
 */
@OptIn(ExperimentalContracts::class)
public inline fun BotManagers.miraiBots(block: MiraiBotManager.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    miraiBots(true, block)
}

/**
 * 通过 [BotManagers] 获取**第一个**存在的 [MiraiBotManager] 并使用它。
 *
 * ```kotlin
 * val application = createSimpleApplication {}
 * application.botManagers.miraiBots { // this: MiraiBotManager
 *    // ...
 * }
 * ```
 *
 * @param failOnMiss 如果没有找到任何 [MiraiBotManager], 是否抛出 [NoSuchElementException] 异常。
 */
public inline fun BotManagers.miraiBots(failOnMiss: Boolean, block: MiraiBotManager.() -> Unit) {
    val manager = (this as Iterable<EventProvider>).firstMiraiBotManagerOrNull()
        ?: if (failOnMiss) throw NoSuchElementException("MiraiBotManager") else return

    manager.block()
}
//endregion



