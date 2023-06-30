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

import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.DeviceInfoBuilder
import kotlin.random.Random


internal const val DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED: Long = 1

/**
 * 构建一个在mirai组件中的设备信息实例。
 */
public fun simbotMiraiDeviceInfo(c: Long, s: Long = DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED): DeviceInfo {
    val r = Random(c * s)
    return DeviceInfoBuilder.fromRandom(r)
            .display("SIMBOT-MIRAI.199983.001")
            .product("simbot")
            .device("simbot")
            .board("simbot")
            .brand("forte")
            .model("mirai")
            .bootloader("unknown")
            .build()
}
