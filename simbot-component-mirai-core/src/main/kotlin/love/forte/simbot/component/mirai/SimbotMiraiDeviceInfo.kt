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
        imei = "86" + getRandomString(15, '0'..'9', r),
        apn = "wifi".toByteArray(),
        androidId = getRandomByteArray(8, r).toUHexString("").lowercase().encodeToByteArray()

    )
}


/*
 * 以下源代码修改自
 * net.mamoe.mirai.utils.SystemDeviceInfo.kt、
 * net.mamoe.mirai.utils.ExternalImage.kt、
 * net.mamoe.mirai.utils.Bytes.kt
 *
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

/**
 * 生成长度为 16, 元素为随机 `0..255` 的 [ByteArray]
 */
private fun getRandomByteArray(r: Random): ByteArray = getRandomByteArray(16, r)

private fun getRandomByteArray(length: Int, r: Random): ByteArray = ByteArray(length) { r.nextInt(0..255).toByte() }


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

private fun ByteArray.toUHexString(
    separator: String = " ",
    offset: Int = 0,
    length: Int = this.size - offset
): String {
    this.checkOffsetAndLength(offset, length)
    if (length == 0) {
        return ""
    }
    val lastIndex = offset + length
    return buildString(length * 2) {
        this@toUHexString.forEachIndexed { index, it ->
            if (index in offset until lastIndex) {
                val ret = it.toUByte().toString(16).uppercase()
                if (ret.length == 1) append('0')
                append(ret)
                if (index < lastIndex - 1) append(separator)
            }
        }
    }
}

private fun ByteArray.checkOffsetAndLength(offset: Int, length: Int) {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }
}

private fun luhn(imei: String): Int {
    var odd = false
    val zero = '0'
    val sum = imei.sumOf { char ->
        odd = !odd
        if (odd) {
            char.code - zero.code
        } else {
            val s = (char.code - zero.code) * 2
            s % 10 + s / 10
        }
    }
    return (10 - sum % 10) % 10
}
