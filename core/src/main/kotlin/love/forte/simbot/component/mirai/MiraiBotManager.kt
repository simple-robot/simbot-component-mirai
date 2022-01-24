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

package love.forte.simbot.component.mirai

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import love.forte.simbot.*
import love.forte.simbot.component.mirai.internal.MiraiBotManagerImpl
import love.forte.simbot.event.EventProcessor
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.LoggerAdapters.asMiraiLogger
import net.mamoe.mirai.utils.MiraiLogger
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.time.ExperimentalTime


private fun hex(hex: String): ByteArray {
    val result = ByteArray(hex.length / 2)
    for (idx in result.indices) {
        val srcIdx = idx * 2
        val high = hex[srcIdx].toString().toInt(16) shl 4
        val low = hex[srcIdx + 1].toString().toInt(16)
        result[idx] = (high or low).toByte()
    }

    return result
}


/**
 *
 * Mirai组件中对 [MiraiBot] 进行管理的 [BotManager].
 *
 * @author ForteScarlet
 */
public abstract class MiraiBotManager : BotManager<MiraiBot>() {
    protected abstract val logger: Logger
    override val component: Component get() = ComponentMirai.component

    /**
     * manager中的 [start] 没有效果。
     */
    @JvmSynthetic
    override suspend fun start(): Boolean = false

    @OptIn(ExperimentalSerializationApi::class)
    override fun register(verifyInfo: BotVerifyInfo): MiraiBot {
        val serializer = MiraiViaBotFileConfiguration.serializer()

        val json = CJson
        val jsonElement = verifyInfo.inputStream().use { inp -> json.decodeFromStream(JsonElement.serializer(), inp) }
        val component = jsonElement.jsonObject["component"]?.jsonPrimitive?.content
            ?: throw NoSuchComponentException("Component is not found in [${verifyInfo.infoName}]")

        logger.debug("[{}] json element load: {}", verifyInfo.infoName, jsonElement)

        if (component != ComponentMirai.COMPONENT_ID.toString()) {
            logger.debug("[{}] mismatch: [{}] != [{}]", verifyInfo.infoName, component, ComponentMirai.COMPONENT_ID)
            throw ComponentMismatchException("[$component] != [${ComponentMirai.COMPONENT_ID}]")
        }

        val configuration = json.decodeFromJsonElement(serializer, jsonElement)

        val password = configuration.password
        if (password != null) {
            return register(configuration.code, password = password, configuration.miraiBotConfiguration)
        }
        val passwordMD5 = configuration.passwordMD5
            ?: throw VerifyFailureException("One of the [password] or [passwordMD5] must exist")

        return register(configuration.code, passwordMD5 = hex(passwordMD5), configuration.miraiBotConfiguration)
    }

    /**
     * 注册一个Bot。
     *
     * @param code 账号
     * @param password 密码
     * @param configuration mirai bot 配置
     */
    public abstract fun register(code: Long, password: String, configuration: BotConfiguration): MiraiBot

    /**
     * 注册一个Bot。
     * @param code 账号
     * @param password 密码
     */
    public fun register(code: Long, password: String): MiraiBot =
        register(code, password, MiraiViaBotFileConfiguration(code, password = password).miraiBotConfiguration)


    /**
     * 注册一个Bot。
     *
     * @param code 账号
     * @param passwordMD5 密码的MD5字节数组
     * @param configuration mirai bot 配置
     */
    public abstract fun register(code: Long, passwordMD5: ByteArray, configuration: BotConfiguration): MiraiBot

    /**
     * 注册一个Bot。
     * @param code 账号
     * @param passwordMD5 密码的MD5字节数组
     */
    public fun register(code: Long, passwordMD5: ByteArray): MiraiBot =
        register(code, passwordMD5, BotConfiguration.Default)


    /**
     * 注册一个Bot。
     *
     * @param code 账号
     * @param password 密码
     * @param configuration mirai bot 配置
     */
    public abstract fun register(
        code: Long,
        password: String,
        configuration: BotFactory.BotConfigurationLambda
    ): MiraiBot


    /**
     * 注册一个Bot。
     *
     * @param code 账号
     * @param passwordMD5 密码的MD5字节数组
     * @param configuration mirai bot 配置
     */
    public abstract fun register(
        code: Long,
        passwordMD5: ByteArray,
        configuration: BotFactory.BotConfigurationLambda
    ): MiraiBot


    public companion object {
        @JvmStatic
        public fun newInstance(eventProcessor: EventProcessor): MiraiBotManager {
            return MiraiBotManagerImpl(eventProcessor)
        }
    }

}


public fun miraiBotManager(eventProcessor: EventProcessor): MiraiBotManager =
    MiraiBotManager.newInstance(eventProcessor)


// 只有在注册时候会使用到, 不保留为属性。
private val CJson
    get() = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }


/**
 *
 * @see BotConfiguration
 */
@Serializable
internal data class MiraiViaBotFileConfiguration(
    val code: Long,
    val password: String? = null,
    val passwordMD5: String? = null,

    /** mirai配置自定义deviceInfoSeed的时候使用的随机种子。默认为1. */
    private val deviceInfoSeed: Long = 1,

    @Serializable(FileSerializer::class)
    private val workingDir: File = BotConfiguration.Default.workingDir,

    private val heartbeatPeriodMillis: Long = BotConfiguration.Default.heartbeatPeriodMillis,

    private val statHeartbeatPeriodMillis: Long = BotConfiguration.Default.statHeartbeatPeriodMillis,

    private val heartbeatTimeoutMillis: Long = BotConfiguration.Default.heartbeatTimeoutMillis,

    @Serializable(HeartbeatStrategySerializer::class)
    private val heartbeatStrategy: BotConfiguration.HeartbeatStrategy = BotConfiguration.Default.heartbeatStrategy,

    private val reconnectionRetryTimes: Int = BotConfiguration.Default.reconnectionRetryTimes,
    private val autoReconnectOnForceOffline: Boolean = BotConfiguration.Default.autoReconnectOnForceOffline,

    @Serializable(MiraiProtocolSerializer::class)
    private val protocol: BotConfiguration.MiraiProtocol = BotConfiguration.Default.protocol,

    private val highwayUploadCoroutineCount: Int = BotConfiguration.Default.highwayUploadCoroutineCount,

    // 如果是字符串，尝试解析为json
    // 否则视为文件路径。
    // 如果bot本身为json格式，则允许此处直接为json对象。
    private val deviceInfoJson: JsonElement? = null,


    private val noNetworkLog: Boolean = false,
    private val noBotLog: Boolean = false,
    private val isShowingVerboseEventLog: Boolean = BotConfiguration.Default.isShowingVerboseEventLog,

    @Serializable(FileSerializer::class)
    private val cacheDir: File = BotConfiguration.Default.cacheDir,

    /**
     *
     * json:
     * ```json
     * {
     *  "contactListCache": {
     *      "saveIntervalMillis": 60000
     *      "friendListCacheEnabled": true
     *      "groupMemberListCacheEnabled": true
     *  }
     * }
     * ```
     */
    @SerialName("contactListCache")
    private val contactListCacheConfiguration: ContactListCacheConfiguration = ContactListCacheConfiguration(),


    private val loginCacheEnabled: Boolean = BotConfiguration.Default.loginCacheEnabled,

    private val convertLineSeparator: Boolean = BotConfiguration.Default.convertLineSeparator,

    ) {


    @Transient
    private val deviceInfo: (Bot) -> DeviceInfo = d@{ bot ->
        if (deviceInfoJson == null) {
            simbotMiraiDeviceInfo(bot.id, deviceInfoSeed)
        } else {
            val json = CJson
            if (deviceInfoJson is JsonPrimitive && deviceInfoJson.isString) {
                // file path
                val filePath = deviceInfoJson.content
                when {
                    filePath.startsWith("classpath:") -> {
                        val path = filePath.substring(9)
                        json.readResourceDeviceInfo(path)
                    }
                    filePath.startsWith("file:") -> {
                        val path = filePath.substring(5)
                        json.readFileDeviceInfo(Path(path))
                    }
                    else -> {
                        // 先看看文件存不存在
                        val file = Path(filePath)
                        if (file.exists()) {
                            json.readFileDeviceInfo(file)
                        } else {
                            json.readResourceDeviceInfo(filePath)
                        }
                    }
                }

            } else if (deviceInfoJson is JsonObject) {
                json.decodeFromJsonElement(DeviceInfo.serializer(), deviceInfoJson)
            } else {
                // 'deviceInfoJson' 的类型必须是jsonObject或json
                throw SimbotIllegalArgumentException("The type of 'deviceInfoJson' must be json object or string")
            }

        }
    }

    private fun Json.readFileDeviceInfo(path: Path): DeviceInfo {
        return decodeFromString(DeviceInfo.serializer(), path.readText())
    }

    private fun Json.readResourceDeviceInfo(path: String): DeviceInfo {
        val text = javaClass.classLoader.getResourceAsStream(path)
            ?.bufferedReader()
            ?.readText()
            ?: throw NoSuchElementException("Bot device info resource: $path")
        return decodeFromString(DeviceInfo.serializer(), text)
    }


    @Transient
    private val botLoggerSupplier: ((Bot) -> MiraiLogger) = {
        val name = "love.forte.simbot.mirai.bot.${it.id}"
        LoggerFactory.getLogger(name).asMiraiLogger()
    }
    @Transient
    private val networkLoggerSupplier: ((Bot) -> MiraiLogger) = {
        val name = "love.forte.simbot.mirai.net.${it.id}"
        LoggerFactory.getLogger(name).asMiraiLogger()
    }


    val miraiBotConfiguration: BotConfiguration
        get() = BotConfiguration().also {
            it.workingDir = workingDir
            it.heartbeatPeriodMillis = heartbeatPeriodMillis
            it.statHeartbeatPeriodMillis = statHeartbeatPeriodMillis
            it.heartbeatTimeoutMillis = heartbeatTimeoutMillis
            it.heartbeatStrategy = heartbeatStrategy
            it.reconnectionRetryTimes = reconnectionRetryTimes
            it.autoReconnectOnForceOffline = autoReconnectOnForceOffline
            it.protocol = protocol
            it.highwayUploadCoroutineCount = highwayUploadCoroutineCount

            it.deviceInfo = deviceInfo

            if (noNetworkLog) it.noNetworkLog() else it.networkLoggerSupplier = networkLoggerSupplier
            if (noBotLog) it.noBotLog() else it.botLoggerSupplier = botLoggerSupplier

            it.isShowingVerboseEventLog = isShowingVerboseEventLog

            it.cacheDir = cacheDir
            it.contactListCache = contactListCacheConfiguration.contactListCache
            it.loginCacheEnabled = loginCacheEnabled
            it.convertLineSeparator = convertLineSeparator
        }


    @Serializable
    class ContactListCacheConfiguration @OptIn(ExperimentalTime::class) constructor(
        private val saveIntervalMillis: Long = BotConfiguration.Default.contactListCache.saveIntervalMillis,
        private val friendListCacheEnabled: Boolean = BotConfiguration.Default.contactListCache.friendListCacheEnabled,
        private val groupMemberListCacheEnabled: Boolean = BotConfiguration.Default.contactListCache.groupMemberListCacheEnabled,
    ) {
        val contactListCache: BotConfiguration.ContactListCache
            get() {
                return BotConfiguration.ContactListCache().also {
                    it.saveIntervalMillis = saveIntervalMillis
                    it.friendListCacheEnabled = friendListCacheEnabled
                    it.groupMemberListCacheEnabled = groupMemberListCacheEnabled
                }
            }
    }


}

internal class FileSerializer : KSerializer<File> {
    override fun deserialize(decoder: Decoder): File {
        val dir = decoder.decodeString()
        return File(dir)
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("File", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: File) {
        encoder.encodeString(value.toString())
    }
}

/**
 * 永远转为大写并允许段横杠-。
 */
internal abstract class EnumStringSerializer<E : Enum<E>>(name: String, private val valueOf: (String) -> E) :
    KSerializer<E> {
    override fun deserialize(decoder: Decoder): E {
        val name = decoder.decodeString().replace('-', '_').uppercase()
        return valueOf(name)
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(name, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: E) {
        encoder.encodeString(value.name)
    }
}


@PublishedApi
internal class HeartbeatStrategySerializer : EnumStringSerializer<BotConfiguration.HeartbeatStrategy>(
    "HeartbeatStrategy",
    BotConfiguration.HeartbeatStrategy::valueOf
)

@PublishedApi
internal class MiraiProtocolSerializer :
    EnumStringSerializer<BotConfiguration.MiraiProtocol>("MiraiProtocol", BotConfiguration.MiraiProtocol::valueOf)

