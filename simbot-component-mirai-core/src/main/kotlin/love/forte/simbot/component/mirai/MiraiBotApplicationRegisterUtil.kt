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

import love.forte.simbot.application.ApplicationBuilder
import love.forte.simbot.application.BotRegistrar


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