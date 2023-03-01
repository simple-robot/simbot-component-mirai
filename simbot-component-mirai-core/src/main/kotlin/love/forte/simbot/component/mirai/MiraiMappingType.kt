/*
 *  Copyright (c) 2023-2023 ForteScarlet.
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
