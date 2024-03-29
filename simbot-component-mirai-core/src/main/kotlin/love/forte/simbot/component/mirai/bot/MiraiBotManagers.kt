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

import love.forte.simbot.FragileSimbotApi
import love.forte.simbot.application.*
import love.forte.simbot.bot.OriginBotManager
import love.forte.simbot.component.mirai.MiraiComponent


/**
 *
 * 从当前 [ApplicationBuilder] 中，通过 [ApplicationBuilder.bots] 寻找并使用注册的 [MiraiBotManager].
 *
 * ```kotlin
 * simpleApplication {
 *     miraiBots { // it: simboot.BotRegistrar, this: MiraiBotManager
 *         val bot: MiraiBot = register(114514L, "password")
 *         bot.start()
 *     }
 * }
 * ```
 *
 * 如果当前环境中不存在 [MiraiBotManager], 则会抛出 [NoSuchElementException] 。
 * 异常不会立即抛出的，而是在此配置被使用的时候才会抛出。
 *
 * @throws NoSuchElementException 当目标 [ApplicationBuilder] 中不存在 [MiraiBotManager] 时。
 *
 */
public inline fun ApplicationBuilder<*>.miraiBots(
    crossinline block: suspend MiraiBotManager.(BotRegistrar) -> Unit,
) {
    bots {
        val miraiBotManager = providers.firstNotNullOfOrNull {
            it as? MiraiBotManager
        } ?: throw NoSuchElementException("No event provider of type [MiraiBotManager] in providers: $providers")

        miraiBotManager.block(this)
    }
}

/**
 *
 * 从当前 [ApplicationBuilder] 中，通过 [ApplicationBuilder.bots] 寻找并使用注册的 [MiraiBotManager].
 *
 * ```kotlin
 * simpleApplication {
 *     miraiBotsIfSupport { // it: simboot.BotRegistrar, this: MiraiBotManager
 *         val bot: MiraiBot = register(114514L, "password")
 *         bot.start()
 *     }
 * }
 * ```
 *
 * 如果当前环境中不存在 [MiraiBotManager], 则 [block] 不会被执行。
 *
 */
public inline fun ApplicationBuilder<*>.miraiBotsIfSupport(
    crossinline block: suspend MiraiBotManager.(BotRegistrar) -> Unit,
) {
    bots {
        val miraiBotManager = providers.firstNotNullOfOrNull {
            it as? MiraiBotManager
        } ?: return@bots

        miraiBotManager.block(this)
    }
}


/**
 * 在 [ApplicationBuilder.bots] 作用域中寻找并使用 [MiraiBotManager].
 *
 * ```kotlin
 * simpleApplication {
 *     bots {// this: BotRegistrar
 *         mirai { // this: MiraiBotManager
 *             val bot: MiraiBot = register(114514L, "password")
 *             bot.start()
 *         }
 *     }
 * }
 * ```
 *
 * 如果当前环境中不存在 [MiraiBotManager], 则会抛出 [NoSuchElementException] 。
 * 异常不会立即抛出的，而是在此配置被使用的时候才会抛出。
 *
 * @throws NoSuchElementException 当目标 [BotRegistrar] 中不存在 [MiraiBotManager] 时。
 *
 */
public inline fun BotRegistrar.mirai(block: MiraiBotManager.() -> Unit) {
    val miraiBotManager = providers.firstNotNullOfOrNull {
        it as? MiraiBotManager
    } ?: throw NoSuchElementException("No event provider of type [MiraiBotManager] in providers: $providers")
    miraiBotManager.block()
}

/**
 * 在 [ApplicationBuilder.bots] 作用域中寻找并使用 [MiraiBotManager].
 *
 * ```kotlin
 * simpleApplication {
 *     bots {// this: BotRegistrar
 *         miraiIfSupport { // this: MiraiBotManager
 *             val bot: MiraiBot = register(114514L, "password")
 *             bot.start()
 *         }
 *     }
 * }
 * ```
 *
 * 如果当前 [BotRegistrar] 中不存在 [MiraiBotManager], 则 [block] 不会被执行。
 *
 */
public inline fun BotRegistrar.miraiIfSupport(block: MiraiBotManager.() -> Unit) {
    val miraiBotManager = providers.firstNotNullOfOrNull {
        it as? MiraiBotManager
    } ?: throw NoSuchElementException("No event provider of type [MiraiBotManager] in providers: $providers")
    miraiBotManager.block()
}


// region manager获取扩展

/**
 * 通过 [OriginBotManager] 获取所有的 [MiraiBotManager]。
 *
 * @see OriginBotManager
 */
@FragileSimbotApi
@Suppress("NOTHING_TO_INLINE")
public inline fun miraiBotManagers(): List<MiraiBotManager> = OriginBotManager.filterIsInstance<MiraiBotManager>()

/**
 * 从 [OriginBotManager] 获取第一个 [MiraiComponent]。
 *
 * @throws [NoSuchElementException] 如果找不到的话
 */
@FragileSimbotApi
@Suppress("NOTHING_TO_INLINE")
public inline fun firstMiraiBotManager(): MiraiBotManager =
    OriginBotManager.first { it is MiraiBotManager } as MiraiBotManager

/**
 * 从 [OriginBotManager] 获取第一个 [MiraiComponent]。
 *
 * 如果找不到则返回null。
 */
@FragileSimbotApi
@Suppress("NOTHING_TO_INLINE")
public inline fun firstMiraiBotManagerOrNull(): MiraiBotManager? =
    OriginBotManager.firstOrNull { it is MiraiBotManager } as MiraiBotManager?


/**
 * 获取其中为 [MiraiBotManager] 的管理器。
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Iterable<EventProvider>.filterIsMiraiBotManagers(): List<MiraiBotManager> =
    filterIsInstance<MiraiBotManager>()


/**
 * 过滤获取其中为 [MiraiBotManager] 的管理器。
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Sequence<EventProvider>.filterIsMiraiBotManagers(): Sequence<MiraiBotManager> =
    filterIsInstance<MiraiBotManager>()

/**
 * 获取其中第一个为 [MiraiBotManager] 的管理器。
 * 如果找不到则抛出 [NoSuchElementException]。
 *
 * @throws NoSuchElementException 如果找不到
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Iterable<EventProvider>.firstMiraiBotManager(): MiraiBotManager =
    first { it is MiraiBotManager } as MiraiBotManager

/**
 * 获取其中第一个为 [MiraiBotManager] 的管理器。
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Iterable<EventProvider>.firstMiraiBotManagerOrNull(): MiraiBotManager? =
    firstOrNull { it is MiraiBotManager } as MiraiBotManager?


/**
 * 过滤获取其中第一个为 [MiraiBotManager] 的管理器。
 * 如果找不到则抛出 [NoSuchElementException]。
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Sequence<EventProvider>.firstMiraiBotManager(): MiraiBotManager =
    first { it is MiraiBotManager } as MiraiBotManager


/**
 * 过滤获取其中第一个为 [MiraiBotManager] 的管理器。
 * 如果找不到则得到null。
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Sequence<EventProvider>.firstMiraiBotManagerOrNull(): MiraiBotManager? =
    firstOrNull { it is MiraiBotManager } as MiraiBotManager?


/**
 * 得到 [OriginBotManager] 中的所有 mirai 组件。
 *
 * @see OriginBotManager
 */
@FragileSimbotApi
public inline val OriginBotManager.miraiBotManagers: List<MiraiBotManager> get() = filterIsMiraiBotManagers()


/**
 * 从 [Application.botManagers] 中寻找所有的 [MiraiBotManager].
 *
 */
public inline val Application.miraiBotManagers: List<MiraiBotManager> get() = botManagers.filterIsMiraiBotManagers()

/**
 * 从 [Application.botManagers] 中寻找第一个的 [MiraiBotManager].
 *
 * @throws NoSuchElementException 未寻得任何实例
 */
public inline val Application.firstMiraiBotManager: MiraiBotManager get() = botManagers.firstMiraiBotManager()

/**
 * 从 [Application.botManagers] 中寻找第一个的 [MiraiBotManager].
 *
 */
public inline val Application.firstMiraiBotManagerOrNull: MiraiBotManager? get() = botManagers.firstMiraiBotManagerOrNull()


/**
 * 从 [BotManagers] 中寻找所有的 [MiraiBotManager].
 *
 */
public inline val BotManagers.miraiBotManagers: List<MiraiBotManager> get() = filterIsMiraiBotManagers()

// endregion
