package love.forte.simbot.component.mirai

import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.properties.Properties
import love.forte.simbot.*
import love.forte.simbot.action.NotSupportActionException
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.LoggerAdapters.asMiraiLogger
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import kotlin.time.ExperimentalTime


/**
 *
 * Mirai组件中对 [MiraiBot] 进行管理的 [BotManager].
 *
 * @author ForteScarlet
 */
public abstract class MiraiBotManager : BotManager<MiraiBot>() {
    override val component: Component get() = ComponentMirai.component

    /**
     * manager中的 [start] 没有效果。
     */
    override suspend fun start(): Boolean = false

    override fun register(verifyInfo: BotVerifyInfo): MiraiBot {
        val serializer = MiraiViaBotFileConfiguration.serializer()

        @OptIn(ExperimentalSerializationApi::class)
        val configuration = verifyInfo.tryResolveVerifyInfo(
            ::VerifyFailureException,
            {
                CJson.decodeFromStream(serializer, it)
            },
            CYaml?.let { yaml ->
                (
                        {
                            yaml.decodeFromStream(serializer, it)
                        }
                        )
            } ?: { throw NotSupportActionException("com.charleskorn.kaml.Yaml not in classpath") },
            {
                val p = java.util.Properties().also { p -> p.load(it) }
                val stringMap = mutableMapOf<String, String>()
                for (key in p.stringPropertyNames()) {
                    stringMap[key] = p.getProperty(key)
                }
                // 鉴于 kotlinx properties 的限制，需要对 defaultIntents 属性进行特殊处理
                val defaultIntents = stringMap.remove("defaultIntents")
                if (defaultIntents != null) {
                    val defaultIntentsSplit = defaultIntents.split(Regex(" *, *"))
                    defaultIntentsSplit.forEachIndexed { index, intent ->
                        stringMap["defaultIntents.$index"] = intent
                    }
                }

                CProp.decodeFromStringMap(serializer, stringMap)
            },

            ).getOrThrow()

        if (configuration.component == null) {
            throw NoSuchComponentException("Component is not found in [${verifyInfo.infoName}]")
        }

        if (configuration.component != ComponentMirai.COMPONENT_ID.toString()) {
            throw ComponentMismatchException("[${configuration.component}] != [${ComponentMirai.COMPONENT_ID}]")
        }

        return register(configuration.code, configuration.password, configuration.miraiBotConfiguration)
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
    @Api4J // overloads for java
    public fun register(code: Long, password: String): MiraiBot = register(code, password, BotConfiguration.Default)


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
    @Api4J // overloads for java
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


}

// 只有在注册时候会使用到, 不保留为属性。

@OptIn(ExperimentalSerializationApi::class)
private val CProp
    get() = Properties(SerializersModule { })
private val CYaml get() = CYamlFunction?.invoke()
private val CYamlFunction: (() -> com.charleskorn.kaml.Yaml)? by lazy {
    try {
        return@lazy {
            com.charleskorn.kaml.Yaml(
                configuration = YamlConfiguration(
                    strictMode = false
                )
            )
        }
    } catch (e: NoClassDefFoundError) {
        LoggerFactory.getLogger(MiraiBotManager::class)
            .error("[com.charleskorn.kaml.Yaml] not in your classpath. If you want to support for yaml, add 'com.charleskorn.kaml:kaml:\$version' into your classpath.")
        return@lazy null
    }
}
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
    val password: String, // support for password md5
    val component: String? = null,

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

    // 如果是字符串，尝试解析为json，否则视为文件路径，必须保证文件扩展名为 'json'. 如果bot本身为json格式，则允许此处直接为json对象。
    // 如果是Yaml解析，此处可能会出错。
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
     *
     * properties:
     * ```properties
     * contactListCache.saveIntervalMillis = 60000
     * contactListCache.friendListCacheEnabled = true
     * contactListCache.groupMemberListCacheEnabled = true
     * ```
     *
     * yaml:
     * ```yaml
     * contactListCache:
     *    saveIntervalMillis: 60000
     *    friendListCacheEnabled: true
     *    groupMemberListCacheEnabled: true
     *
     * ```
     *
     */
    @SerialName("contactListCache")
    private val contactListCacheConfiguration: ContactListCacheConfiguration = ContactListCacheConfiguration(),


    private val loginCacheEnabled: Boolean = BotConfiguration.Default.loginCacheEnabled,

    private val convertLineSeparator: Boolean = BotConfiguration.Default.convertLineSeparator,

    ) {


    private val deviceInfo: ((Bot) -> DeviceInfo) = { simbotMiraiDeviceInfo(it.id, deviceInfoSeed) }
    private val botLoggerSupplier: ((Bot) -> MiraiLogger) = {
        val name = "love.forte.simbot.mirai.bot.${it.id}"
        LoggerFactory.getLogger(name).asMiraiLogger()
    }
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

