import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import love.forte.simbot.component.mirai.MiraiViaBotFileConfiguration
import love.forte.simbot.component.mirai.simbotMiraiDeviceInfo

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val json = Json {
        // isLenient = true
        // ignoreUnknownKeys = true
        explicitNulls = true
        //prettyPrint = true
        encodeDefaults = true
    }

    val deviceInfo = simbotMiraiDeviceInfo(123, 1)
    val info = json.encodeToJsonElement(deviceInfo)
    val config = MiraiViaBotFileConfiguration(
        code = 123,
        password = "密码",
        deviceInfoJson = info,
    )

    val jsonstr = json.encodeToString(config)

    println(jsonstr)

}