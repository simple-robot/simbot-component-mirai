/*
 *  Copyright (c) 2023-2023 ForteScarlet <ForteScarlet@163.com>
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

package love.forte.simbot.component.mirai

import kotlin.reflect.KClass

/**
 * 标记一个 mirai映射类型。
 *
 * 被标记的类型代表其为针对 [target] 类型的一种映射，或者说二次封装。
 *
 * [MiraiMappingType] 一般标记于那些主要职责即为映射的类型上，而一些实现simbot API的功能性类型（例如 `MiraiBot`）
 * 则不会标记。
 *
 * @property target 映射目标
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class MiraiMappingType(val target: KClass<*>)
