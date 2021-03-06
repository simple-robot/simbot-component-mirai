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
 */

package love.forte.simbot.component.mirai

import kotlinx.serialization.Serializable
import love.forte.simbot.FragileSimbotApi
import net.mamoe.mirai.utils.DeviceInfo


@FragileSimbotApi
@Serializable
public data class SimpleDeviceInfo(
    public val display: String,
    public val product: String,
    public val device: String,
    public val board: String,
    public val brand: String,
    public val model: String,
    public val bootloader: String,
    public val fingerprint: String,
    public val bootId: String,
    public val procVersion: String,
    public val baseBand: String,
    public val version: Version,
    public val simInfo: String,
    public val osType: String,
    public val macAddress: String,
    public val wifiBSSID: String,
    public val wifiSSID: String,
    public val imsiMd5: String,
    public val imei: String,
    public val apn: String,
) {
    @Serializable
    @FragileSimbotApi
    public data class Version(
        public val incremental: String = "5891938",
        public val release: String = "10",
        public val codename: String = "REL",
        public val sdk: Int = 29,
    )
}

@FragileSimbotApi
public fun SimpleDeviceInfo.Version.toVersion(): DeviceInfo.Version = DeviceInfo.Version(
    incremental = incremental.toByteArray(),
    release = release.toByteArray(),
    codename = codename.toByteArray(),
    sdk = 0
)

@FragileSimbotApi
public fun SimpleDeviceInfo.toDeviceInfo(): DeviceInfo = DeviceInfo(
    display = display.toByteArray(),
    product = product.toByteArray(),
    device = device.toByteArray(),
    board = board.toByteArray(),
    brand = brand.toByteArray(),
    model = model.toByteArray(),
    bootloader = bootloader.toByteArray(),
    fingerprint = fingerprint.toByteArray(),
    bootId = bootId.toByteArray(),
    procVersion = procVersion.toByteArray(),
    baseBand = baseBand.toByteArray(),
    version = version.toVersion(),
    simInfo = simInfo.toByteArray(),
    osType = osType.toByteArray(),
    macAddress = macAddress.toByteArray(),
    wifiBSSID = wifiBSSID.toByteArray(),
    wifiSSID = wifiSSID.toByteArray(),
    imsiMd5 = imsiMd5.toByteArray(),
    imei = imei,
    apn = apn.toByteArray()
)


@FragileSimbotApi
public fun DeviceInfo.Version.toSimple(): SimpleDeviceInfo.Version = SimpleDeviceInfo.Version(
    incremental = incremental.decodeToString(),
    release = release.decodeToString(),
    codename = codename.decodeToString(),
    sdk = 0
)

@FragileSimbotApi
public fun DeviceInfo.toSimple(): SimpleDeviceInfo = SimpleDeviceInfo(
    display = display.decodeToString(),
    product = product.decodeToString(),
    device = device.decodeToString(),
    board = board.decodeToString(),
    brand = brand.decodeToString(),
    model = model.decodeToString(),
    bootloader = bootloader.decodeToString(),
    fingerprint = fingerprint.decodeToString(),
    bootId = bootId.decodeToString(),
    procVersion = procVersion.decodeToString(),
    baseBand = baseBand.decodeToString(),
    version = version.toSimple(),
    simInfo = simInfo.decodeToString(),
    osType = osType.decodeToString(),
    macAddress = macAddress.decodeToString(),
    wifiBSSID = wifiBSSID.decodeToString(),
    wifiSSID = wifiSSID.decodeToString(),
    imsiMd5 = imsiMd5.decodeToString(),
    imei = imei,
    apn = apn.decodeToString()
)