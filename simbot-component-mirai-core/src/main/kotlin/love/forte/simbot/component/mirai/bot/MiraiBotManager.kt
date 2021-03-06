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

package love.forte.simbot.component.mirai.bot

import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import love.forte.simbot.*
import love.forte.simbot.application.ApplicationBuilder
import love.forte.simbot.application.ApplicationConfiguration
import love.forte.simbot.application.EventProviderAutoRegistrarFactory
import love.forte.simbot.application.EventProviderFactory
import love.forte.simbot.bot.BotManager
import love.forte.simbot.bot.BotVerifyInfo
import love.forte.simbot.bot.ComponentMismatchException
import love.forte.simbot.bot.VerifyFailureException
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.internal.InternalApi
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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText


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
    
    /**
     * manager中的 [start] 没有效果。
     */
    @JvmSynthetic
    override suspend fun start(): Boolean = false
    
    @OptIn(InternalApi::class)
    override fun register(verifyInfo: BotVerifyInfo): MiraiBot {
        val serializer = MiraiBotVerifyInfoConfiguration.serializer()
        
        if (verifyInfo.componentId != this.component.id) {
            logger.debug("[{}] mismatch by mirai: [{}] != [{}]", verifyInfo.name, component, this.component.id)
            throw ComponentMismatchException("[$component] != [${this.component.id}]")
        }
        
        val configuration = verifyInfo.decode(serializer)
        
        val password = configuration.password
        if (password != null) {
            return register(
                configuration.code,
                password = password,
                configuration.simbotBotConfiguration,
            )
        }
        val passwordMD5 = configuration.passwordMD5
            ?: throw VerifyFailureException("One of the [password] or [passwordMD5] must exist")
        
        return register(
            configuration.code,
            passwordMD5 = hex(passwordMD5),
            configuration.simbotBotConfiguration,
        )
    }
    
    /**
     * 注册一个Bot。
     *
     * 此函数构建的 [MiraiBot] 中，如果配置了[MiraiBotConfiguration.initialBotConfiguration],
     * 则将会完全的直接使用 [configuration] 中的 [MiraiBotConfiguration.initialBotConfiguration],
     * 包括其中的设备信息配置、logger配置等。
     *
     * @param code 账号
     * @param password 密码
     * @param configuration simbot中的bot配置类
     */
    public abstract fun register(code: Long, password: String, configuration: MiraiBotConfiguration): MiraiBot
    
    
    /**
     * 注册一个Bot。
     *
     * 此函数构建的 [MiraiBot] 中，如果配置了[MiraiBotConfiguration.initialBotConfiguration],
     * 则将会完全的直接使用 [configuration] 配置结果中的 [MiraiBotConfiguration.initialBotConfiguration],
     * 包括其中的设备信息配置、logger配置等。
     *
     * @param code 账号
     * @param password 密码
     * @param configuration simbot中的bot配置类配置函数
     */
    public fun register(code: Long, password: String, configuration: MiraiBotConfigurationConfigurator): MiraiBot =
        register(code, password, configuration.run { MiraiBotConfiguration().also { c -> c.config() } })
    
    
    /**
     * 注册一个Bot。
     * @param code 账号
     * @param password 密码
     */
    public fun register(code: Long, password: String): MiraiBot =
        register(code, password, MiraiBotConfiguration())
    
    
    /**
     * 注册一个Bot。
     *
     * 此函数构建的 [MiraiBot] 中，如果配置了[MiraiBotConfiguration.initialBotConfiguration],
     * 则将会完全的直接使用 [configuration] 中的 [MiraiBotConfiguration.initialBotConfiguration],
     * 包括其中的设备信息配置、logger配置等。
     *
     * @param code 账号
     * @param passwordMD5 密码的MD5字节数组
     * @param configuration simbot bot 配置
     */
    public abstract fun register(code: Long, passwordMD5: ByteArray, configuration: MiraiBotConfiguration): MiraiBot
    
    
    /**
     * 注册一个Bot。
     *
     * 此函数构建的 [MiraiBot] 中，如果配置了[MiraiBotConfiguration.initialBotConfiguration],
     * 则将会完全的直接使用 [configuration] 中的 [MiraiBotConfiguration.initialBotConfiguration],
     * 包括其中的设备信息配置、logger配置等。
     *
     * @param code 账号
     * @param passwordMD5 密码的MD5字节数组
     * @param configuration simbot bot 配置函数
     */
    public fun register(code: Long, passwordMD5: ByteArray, configuration: MiraiBotConfigurationConfigurator): MiraiBot =
        register(code, passwordMD5, configuration.run { MiraiBotConfiguration().also { c -> c.config() } })
    
    /**
     * 注册一个Bot。
     *
     * @param code 账号
     * @param passwordMD5 密码的MD5字节数组
     */
    public fun register(code: Long, passwordMD5: ByteArray): MiraiBot =
        register(code, passwordMD5, MiraiBotConfiguration())
    
    
    /**
     * [MiraiBotManager] 的构造工厂。
     *
     */
    public companion object Factory : EventProviderFactory<MiraiBotManager, MiraiBotManagerConfiguration> {
        override val key: Attribute<MiraiBotManager> = attribute("SIMBOT.MIRAI")
        
        @JvmStatic
        @Suppress("DeprecatedCallableAddReplaceWith")
        @Deprecated("install mirai in simbotApplication.")
        public fun newInstance(eventProcessor: EventProcessor): MiraiBotManager {
            return MiraiBotManagerImpl(eventProcessor, MiraiComponent(), MiraiBotManagerConfigurationImpl())
        }
        
        
        override suspend fun create(
            eventProcessor: EventProcessor,
            components: List<Component>,
            applicationConfiguration: ApplicationConfiguration,
            configurator: MiraiBotManagerConfiguration.() -> Unit,
        ): MiraiBotManager {
            // configurator ignore
            // find self
            val component = components.find { it.id == MiraiComponent.ID_VALUE } as? MiraiComponent
                ?: throw NoSuchComponentException("There are no MiraiComponent(id=${MiraiComponent.ID_VALUE}) registered in the current application.")
            
            val configuration = MiraiBotManagerConfigurationImpl().also {
                val context = applicationConfiguration.coroutineContext
                val parentJob = context[Job]
                it.parentCoroutineContext = context + SupervisorJob(parentJob)
                configurator(it)
            }
            
            return MiraiBotManagerImpl(eventProcessor, component, configuration).also {
                configuration.useBotManager(it)
            }
        }
    }
    
}


public fun interface MiraiBotConfigurationConfigurator {
    public fun MiraiBotConfiguration.config()
}



/**
 * [MiraiBotManager] 的配置类。
 *
 */
@Suppress("DEPRECATION")
public interface MiraiBotManagerConfiguration {
    
    /**
     * 用于使用在 [MiraiBotManager] 中以及作为所有Bot的父类协程上下文。
     *
     * 会使用 [ApplicationConfiguration] 中的配置作为初始值。
     *
     */
    public var parentCoroutineContext: CoroutineContext
    
    /**
     * 注册一个mirai bot.
     *
     * 从此处注册bot将会早于通过 [ApplicationBuilder.bots] 中进行全局注册的bot被执行。
     *
     * @param code 账号
     * @param password 密码
     * @param configuration mirai的 bot 注册所需要的配置类。
     * @param onBot 当bot被注册后执行函数。
     */
    @Deprecated("Use ApplicationBuilder.miraiBots { ... } or BotRegistrar.mirai { ... }")
    public fun register(
        code: Long,
        password: String,
        configuration: MiraiBotConfiguration,
        onBot: suspend (bot: MiraiBot) -> Unit = {},
    )
    
    /**
     * 注册一个mirai bot.
     *
     * @param code 账号
     * @param passwordMd5 密码的md5数据
     * @param configuration mirai的 bot 注册所需要的配置类。
     * @param onBot 当bot被注册后执行函数。
     */
    @Deprecated("Use ApplicationBuilder.miraiBots { ... } or BotRegistrar.mirai { ... }")
    public fun register(
        code: Long,
        passwordMd5: ByteArray,
        configuration: MiraiBotConfiguration,
        onBot: suspend (bot: MiraiBot) -> Unit = {},
    )
    
    /**
     * 注册一个mirai bot.
     *
     * 从此处注册bot将会早于通过 [ApplicationBuilder.bots] 中进行全局注册的bot被执行。
     *
     * @param code 账号
     * @param password 密码
     * @param configuration simbot组件的 bot 注册所需要的配置类。
     * @param onBot 当bot被注册后执行函数。
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Use ApplicationBuilder.miraiBots { ... } or BotRegistrar.mirai { ... }")
    public fun register(
        code: Long,
        password: String,
        configuration: BotFactory.BotConfigurationLambda = BotFactory.BotConfigurationLambda {},
        onBot: suspend (bot: MiraiBot) -> Unit = {},
    ) {
        register(code, password, MiraiBotConfiguration().botConfiguration(configuration), onBot)
    }
    
    /**
     * 注册一个mirai bot.
     *
     * @param code 账号
     * @param passwordMd5 密码的md5数据
     * @param configuration mirai的 bot 注册所需要的配置类。
     * @param onBot 当bot被注册后执行函数。
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Use ApplicationBuilder.miraiBots { ... } or BotRegistrar.mirai { ... }")
    public fun register(
        code: Long,
        passwordMd5: ByteArray,
        configuration: BotFactory.BotConfigurationLambda = BotFactory.BotConfigurationLambda {},
        onBot: suspend (bot: MiraiBot) -> Unit = {},
    ) {
        register(code, passwordMd5, MiraiBotConfiguration().botConfiguration(configuration), onBot)
    }
}


/**
 * [MiraiBotManager] 的自动注册工厂。
 */
public class MiraiBotManagerAutoRegistrarFactory :
    EventProviderAutoRegistrarFactory<MiraiBotManager, MiraiBotManagerConfiguration> {
    override val registrar: MiraiBotManager.Factory get() = MiraiBotManager
}


/**
 * [MiraiBotManager] 的配置类。
 *
 */
private class MiraiBotManagerConfigurationImpl : MiraiBotManagerConfiguration {
    override var parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
    
    private var botManagerProcessor: (suspend (MiraiBotManager) -> Unit)? = null
    
    private fun newProcessor(p: suspend (MiraiBotManager) -> Unit) {
        botManagerProcessor.also { old ->
            botManagerProcessor = { manager ->
                old?.invoke(manager)
                p(manager)
            }
        }
    }
    
    @Suppress("OVERRIDE_DEPRECATION", "OverridingDeprecatedMember")
    override fun register(
        code: Long,
        password: String,
        configuration: MiraiBotConfiguration,
        onBot: suspend (bot: MiraiBot) -> Unit,
    ) {
        newProcessor { manager -> onBot(manager.register(code, password, configuration)) }
    }
    
    
    @Suppress("OVERRIDE_DEPRECATION", "OverridingDeprecatedMember")
    override fun register(
        code: Long,
        passwordMd5: ByteArray,
        configuration: MiraiBotConfiguration,
        onBot: suspend (bot: MiraiBot) -> Unit,
    ) {
        newProcessor { manager -> onBot(manager.register(code, passwordMd5, configuration)) }
    }
    
    suspend fun useBotManager(botManager: MiraiBotManager) {
        botManagerProcessor?.invoke(botManager)
    }
    
    
}


@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
@Deprecated("Use simbotApplication and install MiraiBotManager.")
public fun miraiBotManager(eventProcessor: EventProcessor): MiraiBotManager =
    MiraiBotManager.newInstance(eventProcessor)


/**
 *
 * 通过 [BotVerifyInfo] 进行注册的bot信息配置类。
 *
 * 对一个bot的配置，账号（[code]）与密码（[password] | [passwordMD5] | [passwordMD5Bytes]）为必须属性；
 * 其他额外配置位于 [config] 属性中。
 *
 *
 * @see BotConfiguration
 */
@Serializable
@InternalApi
public data class MiraiBotVerifyInfoConfiguration(
    /**
     * 账号。
     */
    val code: Long,
    
    /**
     * 密码。与 [passwordMD5] 和 [passwordMD5Bytes] 之间只能存在一个。
     */
    val password: String? = null,
    
    /**
     * 密码。与 [password] 和 [passwordMD5Bytes] 之间只能存在一个。
     */
    val passwordMD5: String? = null,
    
    /**
     * 密码。与 [password] 和 [passwordMD5] 之间只能存在一个。
     */
    @Suppress("ArrayInDataClass")
    val passwordMD5Bytes: ByteArray? = null,
    
    /**
     * 必要属性之外的额外配置属性。
     */
    val config: Config = Config.DEFAULT,
) {
    
    /**
     * [MiraiBotVerifyInfoConfiguration] 中除了必要信息以外的额外配置信息。
     */
    @OptIn(FragileSimbotApi::class)
    @Serializable
    public data class Config(
    
        /** mirai配置自定义deviceInfoSeed的时候使用的随机种子。默认为1. */
        val deviceInfoSeed: Long = DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED,
    
        @Serializable(FileSerializer::class)
        val workingDir: File = BotConfiguration.Default.workingDir,
    
        val heartbeatPeriodMillis: Long = BotConfiguration.Default.heartbeatPeriodMillis,
    
        val statHeartbeatPeriodMillis: Long = BotConfiguration.Default.statHeartbeatPeriodMillis,
    
        val heartbeatTimeoutMillis: Long = BotConfiguration.Default.heartbeatTimeoutMillis,
    
        @Serializable(HeartbeatStrategySerializer::class)
        val heartbeatStrategy: BotConfiguration.HeartbeatStrategy = BotConfiguration.Default.heartbeatStrategy,
    
        val reconnectionRetryTimes: Int = BotConfiguration.Default.reconnectionRetryTimes,
        val autoReconnectOnForceOffline: Boolean = BotConfiguration.Default.autoReconnectOnForceOffline,
    
        @Serializable(MiraiProtocolSerializer::class)
        val protocol: BotConfiguration.MiraiProtocol = BotConfiguration.Default.protocol,
    
        val highwayUploadCoroutineCount: Int = BotConfiguration.Default.highwayUploadCoroutineCount,
    
        /**
         * 如果是字符串，尝试解析为json
         * 否则视为文件路径。
         * 如果bot本身为json格式，则允许此处直接为json对象。
         *
         * 优先使用此属性。
         */
        val deviceInfoJson: DeviceInfo? = null,
    
        /**
         * 优先使用 [deviceInfo].
         */
        val simpleDeviceInfoJson: SimpleDeviceInfo? = null,
    
        /**
         * 加载的设备信息json文件的路径。
         * 如果是 `classpath:` 开头，则会优先尝试加载resource，
         * 否则优先视为文件路径加载。
         */
        val deviceInfoFile: String? = null,
    
        val noNetworkLog: Boolean = false,
        val noBotLog: Boolean = false,
        val isShowingVerboseEventLog: Boolean = BotConfiguration.Default.isShowingVerboseEventLog,
    
        @Serializable(FileSerializer::class)
        val cacheDir: File = BotConfiguration.Default.cacheDir,
    
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
        val contactListCacheConfiguration: ContactListCacheConfiguration = ContactListCacheConfiguration(),
    
        /**
         * 是否开启登录缓存。
         * @see BotConfiguration.loginCacheEnabled
         */
        val loginCacheEnabled: Boolean = BotConfiguration.Default.loginCacheEnabled,
    
        /**
         * 是否处理接受到的特殊换行符, 默认为 true
         * @see BotConfiguration.convertLineSeparator
         */
        val convertLineSeparator: Boolean = BotConfiguration.Default.convertLineSeparator,
        
        ///////////// simbot config
        /**
         * 消息撤回缓存策略。默认为 [RecallMessageCacheStrategyType.INVALID]。
         *
         * ```json
         * {
         *   "recallMessageCacheStrategy": "INVALID"
         * }
         * ```
         *
         */
        val recallMessageCacheStrategy: RecallMessageCacheStrategyType = RecallMessageCacheStrategyType.INVALID,
    
        ) {
        
        
        @OptIn(FragileSimbotApi::class)
        @Transient
        private val deviceInfo: (Bot) -> DeviceInfo = d@{ bot ->
            // temp json
            val json = Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
            deviceInfoJson
                ?: simpleDeviceInfoJson?.toDeviceInfo()
                ?: if (deviceInfoFile?.isNotBlank() == true) {
                    simbotMiraiDeviceInfo(bot.id, deviceInfoSeed)
                } else {
                    if (deviceInfoFile == null) {
                        return@d simbotMiraiDeviceInfo(bot.id, deviceInfoSeed)
                    }
                    
                    
                    when {
                        deviceInfoFile.startsWith("classpath:") -> {
                            val path = deviceInfoFile.substring(9)
                            json.readResourceDeviceInfo(path)
                        }
                        deviceInfoFile.startsWith("file:") -> {
                            val path = deviceInfoFile.substring(5)
                            json.readFileDeviceInfo(Path(path))
                        }
                        else -> {
                            // 先看看文件存不存在
                            val file = Path(deviceInfoFile)
                            if (file.exists()) {
                                json.readFileDeviceInfo(file)
                            } else {
                                json.readResourceDeviceInfo(deviceInfoFile)
                            }
                        }
                    }
                }
        }
        
        /**
         * 将当前配置的信息转化为 [MiraiBotConfiguration][BotConfiguration] 实例。
         */
        public fun miraiBotConfiguration(initial: BotConfiguration): BotConfiguration {
            return initial.also {
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
        
        
        public companion object {
            
            @JvmField
            public val DEFAULT: Config = Config()
        }
    }
    
    /**
     * 使用的消息撤回缓存策略类型。
     *
     * @see StandardMiraiRecallMessageCacheStrategy
     */
    @Serializable(RecallMessageCacheStrategyTypeSerializer::class)
    public enum class RecallMessageCacheStrategyType(public val strategy: () -> MiraiRecallMessageCacheStrategy) {
        /**
         * 使用 [InvalidMiraiRecallMessageCacheStrategy].
         *
         */
        INVALID({ InvalidMiraiRecallMessageCacheStrategy }),
        
        /**
         * 使用 [MemoryLruMiraiRecallMessageCacheStrategy]
         */
        MEMORY_LRU({ MemoryLruMiraiRecallMessageCacheStrategy() }),
        
        // 想要更多实现? see StandardMiraiRecallMessageCacheStrategy
        
    }
    
    
    /**
     * 通过 [config] 构建一个 [MiraiBotConfiguration][BotConfiguration].
     */
    public fun miraiBotConfiguration(initial: BotConfiguration): BotConfiguration =
        config.miraiBotConfiguration(initial)
    
    
    /**
     * mirai的联系人列表缓存配置的对应配置类。
     *
     * @see BotConfiguration.ContactListCache
     *
     */
    @Serializable
    public data class ContactListCacheConfiguration constructor(
        /**
         *
         * @see BotConfiguration.ContactListCache.saveIntervalMillis
         */
        val saveIntervalMillis: Long = BotConfiguration.Default.contactListCache.saveIntervalMillis,
        /**
         *
         * @see BotConfiguration.ContactListCache.friendListCacheEnabled
         */
        val friendListCacheEnabled: Boolean = BotConfiguration.Default.contactListCache.friendListCacheEnabled,
        /**
         *
         * @see BotConfiguration.ContactListCache.groupMemberListCacheEnabled
         */
        val groupMemberListCacheEnabled: Boolean = BotConfiguration.Default.contactListCache.groupMemberListCacheEnabled,
    ) {
        
        /**
         * 得到对应的 [MiraiBotConfiguration.ContactListCache][BotConfiguration.ContactListCache] 实例。
         */
        @Transient
        public val contactListCache: BotConfiguration.ContactListCache =
            BotConfiguration.ContactListCache().also {
                it.saveIntervalMillis = saveIntervalMillis
                it.friendListCacheEnabled = friendListCacheEnabled
                it.groupMemberListCacheEnabled = groupMemberListCacheEnabled
            }
        
        public companion object {
            @JvmField
            public val DEFAULT: ContactListCacheConfiguration = ContactListCacheConfiguration()
        }
    }
    
    
    public val simbotBotConfiguration: MiraiBotConfiguration
        get() {
            
            return MiraiBotConfiguration(
                config.recallMessageCacheStrategy.strategy(),
            ).apply {
                botConfiguration {
                    miraiBotConfiguration(this)
                }
            }
        }
    
    
}

/**
 * 文件路径序列化器。
 */
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
 * 枚举名称序列化器。永远转为大写并允许段横杠-。
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

@OptIn(InternalApi::class)
internal class RecallMessageCacheStrategyTypeSerializer :
    EnumStringSerializer<MiraiBotVerifyInfoConfiguration.RecallMessageCacheStrategyType>(
        "RecallMessageCacheStrategyType",
        MiraiBotVerifyInfoConfiguration.RecallMessageCacheStrategyType::valueOf
    )