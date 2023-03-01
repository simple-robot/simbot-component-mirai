/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU Affero通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
 *
 *
 */

package love.forte.simbot.component.mirai

import love.forte.simbot.utils.md5
import net.mamoe.mirai.utils.DeviceInfo
import kotlin.random.Random
import kotlin.random.nextInt


internal const val DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED: Long = 1

/**
 * 构建一个在mirai组件中的设备信息实例。
 */
public fun simbotMiraiDeviceInfo(c: Long, s: Long = DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED): DeviceInfo {
    val r = Random(c * s)
    return DeviceInfo(
        display = "MIRAI-SIMBOT.200122.001".toByteArray(),
        product = "mirai-simbot".toByteArray(),
        device = "mirai-simbot".toByteArray(),
        board = "mirai-simbot".toByteArray(),
        brand = "forte".toByteArray(),
        model = "mirai-simbot".toByteArray(),
        bootloader = "unknown".toByteArray(),
        // mamoe/mirai/mirai:10/MIRAI.200122.001/
        fingerprint = "mamoe/mirai/mirai:10/MIRAI.200122.001/${
            getRandomString(
                7,
                '0'..'9',
                r
            )
        }:user/release-keys".toByteArray(),
        bootId = md5 { digest(getRandomByteArray(r)) },
        procVersion = "Linux version 3.0.31-${getRandomString(r)} (android-build@xxx.xxx.xxx.xxx.com)".toByteArray(),
        baseBand = byteArrayOf(),
        version = DeviceInfo.Version(),
        simInfo = "T-Mobile".toByteArray(),
        osType = "android".toByteArray(),
        macAddress = "02:00:00:00:00:00".toByteArray(),
        wifiBSSID = "02:00:00:00:00:00".toByteArray(),
        wifiSSID = "<unknown ssid>".toByteArray(),
        imsiMd5 = md5 { digest(getRandomByteArray(r)) },
        imei = getRandomString(15, '0'..'9', r),
        apn = "wifi".toByteArray()
    
    )
}


/*
 * 以下源代码修改自
 * net.mamoe.mirai.utils.SystemDeviceInfo.kt、
 * net.mamoe.mirai.utils.ExternalImage.kt
 *
 * 原源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

/**
 * 生成长度为 16, 元素为随机 `0..255` 的 [ByteArray]
 */
private fun getRandomByteArray(r: Random): ByteArray = ByteArray(16) { r.nextInt(0..255).toByte() }

private val defaultRanges: Array<CharRange> = arrayOf('a'..'z', 'A'..'Z', '0'..'9')

/**
 * 根据所给 [charRange] 随机生成长度为 [length] 的 [String].
 */
private fun getRandomString(length: Int, charRange: CharRange, r: Random): String =
    String(CharArray(length) { charRange.random(r) })

/**
 * 根据 [defaultRanges] 随机生成长度为 8 的 [String].
 */
private fun getRandomString(r: Random): String =
    String(CharArray(8) { defaultRanges[r.nextInt(0..defaultRanges.lastIndex)].random(r) })

private operator fun ByteArray.get(rangeStart: Int, rangeEnd: Int): String = buildString {
    for (it in rangeStart..rangeEnd) {
        append(this@get[it].fixToString())
    }
}

private fun Byte.fixToString(): String {
    return when (val b = this.toInt() and 0xff) {
        in 0..15 -> "0${this.toString(16).uppercase()}"
        else -> b.toString(16).uppercase()
    }
}

