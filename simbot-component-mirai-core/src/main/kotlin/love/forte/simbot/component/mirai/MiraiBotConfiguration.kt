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

import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration

/**
 * mirai组件的bot配置类。
 *
 * 与 [net.mamoe.mirai.utils.BotConfiguration] 不同，
 * 此配置类是由simbot所需的。
 *
 * @see createMiraiBotConfiguration
 *
 * @author ForteScarlet
 */
public class MiraiBotConfiguration(
    /**
     * 消息撤回事件的消息缓存策略。
     * 默认使用 [InvalidMiraiRecallMessageCacheStrategy]。
     */
    public var recallCacheStrategy: MiraiRecallMessageCacheStrategy = InvalidMiraiRecallMessageCacheStrategy,
) {
    
    /**
     * mirai的原生配置类的配置函数。
     */
    private var botConfigurationLambda: BotFactory.BotConfigurationLambda = BotFactory.BotConfigurationLambda {}
    
    /**
     * 初始化的配置类。
     *
     * 默认情况下, 当没有覆盖初始配置类的时候，simbot会提供一个提前准备了simbot基础信息的[BotConfiguration]。
     * 但是如果通过 [initialBotConfiguration] 函数覆盖了初始配置类，则simbot将会直接完全按照初始配置类使用，
     * 不再提供拥有simbot特殊配置的配置类。
     *
     * 如果你想基于特殊配置类之上进行配置，请使用 [botConfiguration] 函数进行配置，而不是覆盖初始配置类。
     */
    public var initialBotConfiguration: BotConfiguration? = null
    
    /**
     * 追加对配置类的额外配置。
     *
     * 与 [initialBotConfiguration] 不同，[botConfiguration] 添加的只是 **额外** 配置，
     * 不会影响 [initialBotConfiguration] 属性的特性。
     *
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun botConfiguration(function: BotFactory.BotConfigurationLambda): MiraiBotConfiguration = apply {
        val old = botConfigurationLambda
        botConfigurationLambda = BotFactory.BotConfigurationLambda {
            old.apply { invoke() }
            function.apply { invoke() }
        }
    }
    
    /**
     * 覆盖初始配置类。
     *
     * 默认情况下, 当没有覆盖初始配置类的时候，simbot会提供一个提前准备了simbot基础信息的[BotConfiguration]。
     * 但是如果通过 [initialBotConfiguration] 函数覆盖了初始配置类，则simbot将会直接完全按照初始配置类使用，
     * 不再提供拥有simbot特殊配置的配置类。
     *
     * 如果你想基于特殊配置类之上进行配置，请使用 [botConfiguration] 函数进行配置，而不是覆盖初始配置类。
     *
     */
    public fun initialBotConfiguration(configuration: BotConfiguration): MiraiBotConfiguration = apply {
        initialBotConfiguration = configuration
    }
    
    /**
     * 根据配置构建 [BotConfiguration] 实例。
     */
    internal fun createBotConfiguration(initialBotConfigurationResolver: (BotConfiguration?) -> BotConfiguration): BotConfiguration {
        val initialBotConfiguration = initialBotConfigurationResolver(initialBotConfiguration)
        return initialBotConfiguration.apply { botConfigurationLambda.apply { invoke() } }
    }
}


/**
 * 构建一个 [MiraiBotConfiguration] 并进行配置。
 */
public inline fun createMiraiBotConfiguration(
    initial: MiraiBotConfiguration = MiraiBotConfiguration(),
    block: MiraiBotConfiguration.() -> Unit,
): MiraiBotConfiguration {
    return initial.also(block)
}
