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

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.json.Json
import love.forte.simbot.FragileSimbotApi
import love.forte.simbot.component.mirai.MiraiViaBotFileConfiguration
import love.forte.simbot.component.mirai.SimpleDeviceInfo
import love.forte.simbot.component.mirai.toSimple
import net.mamoe.mirai.utils.DeviceInfo
import org.junit.jupiter.api.Test

/*
 *  Copyright (c) 2022 ForteScarlet <ForteScarlet@163.com>
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

class DeviceInfoTest {

    @OptIn(FragileSimbotApi::class)
    @Test
    fun jsonTest() {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        }

        val info = DeviceInfo.random()
        val str = json.encodeToString(SimpleDeviceInfo.serializer(), info.toSimple())

        println(str)
    }

    /*
    {
        "display": "MIRAI.886953.001",
        "product": "mirai",
        "device": "mirai",
        "board": "mirai",
        "brand": "mamoe",
        "model": "mirai",
        "bootloader": "unknown",
        "fingerprint": "mamoe/mirai/mirai:10/MIRAI.200122.001/0889935:user/release-keys",
        "bootId": "56C65B96-BC1F-7D07-F268-A1A8C48DFE6B",
        "procVersion": "Linux version 3.0.31-v08m3IJZ (android-build@xxx.xxx.xxx.xxx.com)",
        "baseBand": "",
        "version": {
            "sdk": 0
        },
        "simInfo": "T-Mobile",
        "osType": "android",
        "macAddress": "02:00:00:00:00:00",
        "wifiBSSID": "02:00:00:00:00:00",
        "wifiSSID": "<unknown ssid>",
        "imsiMd5": "\u0019m|5���Wl�5�$\u0016k�",
        "imei": "006905565323596",
        "apn": "wifi"
    }

     */


    @OptIn(FragileSimbotApi::class)
    @Test
    fun yamlTest() {
        val conf = MiraiViaBotFileConfiguration(
            code = 123,
            password = "123222xxx",
            simpleDeviceInfoJson = DeviceInfo.random().toSimple()
        )

        val yaml = Yaml(
            configuration = YamlConfiguration(
                strictMode = false
            )
        )

        val str = yaml.encodeToString(MiraiViaBotFileConfiguration.serializer(), conf)
        println(str)

    }

}