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

import kotlinx.serialization.Serializable
import love.forte.simbot.FragileSimbotApi
import love.forte.simbot.utils.toHex
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.DeviceInfoBuilder

/**
 * 简化的设备信息。针对 [DeviceInfo] 的简化映射类型。
 * 字段中绝大多数 [ByteArray] 类型会以 [String] 的形式提供。
 *
 * ### 不稳定
 *
 * [SimpleDeviceInfo] 是通过手动映射的方式构建而成，无法保证兼容性与可用性，需谨慎使用。
 *
 * @see DeviceInfo
 */
@FragileSimbotApi
@Serializable
@MiraiMappingType(DeviceInfo::class)
public data class SimpleDeviceInfo(
        public val display: String?,
        public val product: String?,
        public val device: String?,
        public val board: String?,
        public val brand: String?,
        public val model: String?,
        public val bootloader: String?,
        public val fingerprint: String?,
        public val bootId: String?,
        public val procVersion: String?,
        public val baseBand: String?,
        public val version: Version?,
        public val simInfo: String?,
        public val osType: String?,
        public val macAddress: String?,
        public val wifiBSSID: String?,
        public val wifiSSID: String?,
        public val imsiMd5: String?,
        public val imei: String?,
        public val apn: String?,
        public val androidId: String?,
) {

    /**
     * @see DeviceInfo.Version
     */
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
        sdk = sdk
)

@Suppress("DuplicatedCode")
@FragileSimbotApi
public fun SimpleDeviceInfo.toDeviceInfo(): DeviceInfo = DeviceInfoBuilder
        .fromRandom().apply {
            display?.also(::display)
            product?.also(::product)
            device?.also(::device)
            board?.also(::board)
            brand?.also(::brand)
            model?.also(::model)
            bootloader?.also(::bootloader)
            fingerprint?.also(::fingerprint)
            bootId?.also(::bootId)
            procVersion?.also(::procVersion)
            baseBand?.also(::baseBand)
            version?.also { version(it.toVersion()) }
            simInfo?.also(::simInfo)
            osType?.also(::osType)
            macAddress?.also(::macAddress)
            wifiBSSID?.also(::wifiBSSID)
            wifiSSID?.also(::wifiSSID)
            imsiMd5?.also(::imsiMd5)
            imei?.also(::imei)
            apn?.also(::apn)
            androidId?.also(::androidId)
        }
        .build()

@FragileSimbotApi
public fun DeviceInfo.Version.toSimple(): SimpleDeviceInfo.Version = SimpleDeviceInfo.Version(
        incremental = incremental.decodeToString(),
        release = release.decodeToString(),
        codename = codename.decodeToString(),
        sdk = 0
)

@Suppress("DeprecatedCallableAddReplaceWith")
@FragileSimbotApi
@Deprecated("deprecated. SimpleDeviceInfo 应当仅用于文件配置中，不需要被反向转化")
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
        imsiMd5 = imsiMd5.toHex(),
        imei = imei,
        apn = apn.decodeToString(),
        androidId = androidId.decodeToString()
)
