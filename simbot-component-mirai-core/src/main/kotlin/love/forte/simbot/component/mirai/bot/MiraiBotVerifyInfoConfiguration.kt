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
import love.forte.simbot.FragileSimbotApi
import love.forte.simbot.LoggerFactory
import love.forte.simbot.SimbotIllegalArgumentException
import love.forte.simbot.bot.BotVerifyInfo
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.bot.PasswordInfo.EnvPasswordInfo.Companion.CODE_MARK
import love.forte.simbot.component.mirai.internal.InternalApi
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.LoggerAdapters.asMiraiLogger
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText


/**
 * 直接提供**明文**密码字符串的密码配置形式。
 */
public sealed class TextPasswordInfo : PasswordInfo() {
    @OptIn(InternalApi::class)
    public abstract fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): String
}


/**
 * 提供密码md5字节数组的密码配置形式。
 */
public sealed class Md5BytesPasswordInfo : PasswordInfo() {
    @OptIn(InternalApi::class)
    public abstract fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): ByteArray
}


/**
 * 通过 [MiraiBotVerifyInfoConfiguration.passwordInfo] 来提供多种形式的密码配置。
 *
 * 目前支持的形式有：
 * ### [明文密码][TextPasswordInfo]:
 *
 * - [普通明文密码][PasswordInfo.Text]
 * - [环境变量明文密码][PasswordInfo.EnvText]
 *
 * ### [md5密码][Md5BytesPasswordInfo]:
 *
 * - [md5字符串密码][PasswordInfo.Md5Text]
 * - [环境变量md5字符串密码][PasswordInfo.EnvMd5Text]
 * - [md5字节组密码][PasswordInfo.Md5Bytes]
 *
 */
@Serializable
@OptIn(InternalApi::class)
public sealed class PasswordInfo {
    /**
     * 明文密码类型。
     *
     * ```json
     * {
     *    "type": "text",
     *    "text": "PASSWORD TEXT"
     * }
     * ```
     *
     */
    @Serializable
    @SerialName(Text.TYPE)
    public data class Text(val text: String) : TextPasswordInfo() {
        override fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): String {
            return text
        }
        
        public companion object {
            public const val TYPE: String = "text"
        }
    }
    
    
    /**
     * MD5密码（字符串）类型。
     * ```json
     * {
     *    "type": "md5_text",
     *    "md5": "e807f1fcf82d112f2bb018ca6738a19f"
     * }
     * ```
     */
    @Serializable
    @SerialName(Md5Text.TYPE)
    public data class Md5Text(val md5: String) : Md5BytesPasswordInfo() {
        @Transient
        private val bytes = hex(md5)
        
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
        
        override fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): ByteArray {
            return bytes
        }
        
        public companion object {
            public const val TYPE: String = "md5_text"
        }
    }
    
    
    /**
     * MD5密码（字节数据）类型。
     * ```json
     * {
     *    "type": "md5_bytes",
     *    "md5": [-24, 7, -15, -4, -15, 45, 18, 47, -101, -80, 24, -54, 102, 56, -95, -97]
     * }
     * ```
     */
    @Serializable
    @SerialName(Md5Bytes.TYPE)
    public data class Md5Bytes(val md5: ByteArray) : Md5BytesPasswordInfo() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Md5Bytes) return false
            
            return md5.contentEquals(other.md5)
        }
        
        override fun hashCode(): Int {
            return md5.contentHashCode()
        }
        
        override fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): ByteArray {
            return md5
        }
        
        public companion object {
            public const val TYPE: String = "md5_bytes"
        }
    }
    
    
    /**
     * 内部用于标记后续的通过**环境变量**配置形式相关的 [PasswordInfo] 实现，
     * 并为它们提供统一的行为和辅助功能。
     *
     * ## 配置属性
     *
     * [EnvPasswordInfo] 的实现提供了两个可选属性：[env] 和 [prop]。
     * 这两个属性都是可选的，但是应当至少提供其中一个。换言之，它们不能**同时为空**。
     *
     * 在选取使用时，[prop] 的优先级高于 [env]，[EnvPasswordInfo] 会优先尝试通过 [prop] (通过[System.getProperty])
     * 获取对应的信息，而后再尝试通过 [env] (通过[System.getenv]) 获取信息。
     *
     * 如果最终无法得到任何信息，则会抛出异常。
     *
     * ## 占位符替换
     *
     * 默认情况下，配置属性 [env] 和 [prop] 中的值，会将其中的简单占位符 [`$CODE$`][CODE_MARK]
     * 替换为当前配置账号的 code，即 [MiraiBotVerifyInfoConfiguration.code]。
     *
     * 例如如下配置：
     *
     * ```json
     * {
     *   "code": 123456,
     *   "passwordInfo": {
     *      "type": "env_xxxx",
     *      "prop": "simbot.mirai.$CODE$.password",
     *      "env": "simbot.mirai.$CODE$.password"
     *   }
     * }
     * ```
     * 其中的 [prop] 将会最终被替换为 `"simbot.mirai.123456.password`。
     *
     *
     */
    @InternalApi
    public sealed interface EnvPasswordInfo {
        /**
         * 虚拟机选项的键名，即需要通过 [System.getProperty] 读取的属性键。
         *
         */
        public val prop: String?
        
        /**
         * 环境变量属性的键名，即需要通过 [System.getenv] 读取的属性键。
         *
         */
        public val env: String?
        
        /**
         * 尝试通过 [prop] 和 [env] 获取指定环境变量下的信息。
         *
         * @throws IllegalStateException 当 [prop] 和 [env] 同时为空时
         * @throws NoSuchElementException 当无法从 [prop] 或 [env] 中取得任何有效值时
         */
        @InternalApi
        public val MiraiBotVerifyInfoConfiguration.envValue: String
            get() {
                fun String.replaceCodeMark(): String = replace(CODE_MARK, code.toString())
                
                val prop0 = prop
                val env0 = env
                if (prop0 == null && env0 == null) {
                    throw SimbotIllegalArgumentException("[passwordInfo.prop] and [passwordInfo.env] cannot be null at the same time")
                }
                
                val value =
                    prop0?.let(System::getProperty)?.replaceCodeMark() ?: env0?.let(System::getenv)?.replaceCodeMark()
                
                return value ?: throw NoSuchElementException(buildString {
                    if (prop0 != null) {
                        // 键为 [a] 的prop的值
                        append("Value of prop with key [").append(prop0).append("]; ")
                    }
                    if (env0 != null) {
                        append("Value of env with key [").append(env0).append("]; ")
                    }
                })
            }
        
        
        public companion object {
            public const val CODE_MARK: String = "\$CODE$"
        }
    }
    
    
    /**
     * 通过环境变量来加载密码信息配置。加载的是明文密码配置。
     * 密码值的要求与 [Text] 一致。
     *
     * ```json
     * "passwordInfo": {
     *    "type": "env_text",
     *    "prop": "...",
     *    "env": "..."
     * }
     * ```
     *
     * 更多信息参阅 [EnvPasswordInfo] 接口的描述。
     *
     *
     *
     * @see EnvPasswordInfo
     *
     */
    @Serializable
    @SerialName(EnvText.TYPE)
    public data class EnvText(
        override val prop: String? = null,
        override val env: String? = null,
    ) : TextPasswordInfo(), EnvPasswordInfo {
        
        override fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): String = configuration.envValue
        
        public companion object {
            public const val TYPE: String = "env_${Text.TYPE}"
        }
    }
    
    
    /**
     * 通过环境变量来加载密码信息配置。加载的是Md5密码（字符串形式）配置。
     * 密码值的要求与 [Md5Text] 一致。
     *
     * ```json
     * "passwordInfo": {
     *    "type": "env_md5_text",
     *    "prop": "...",
     *    "env": "..."
     * }
     * ```
     *
     * 更多信息参阅 [EnvPasswordInfo] 接口的描述。
     *
     * @see EnvPasswordInfo
     *
     */
    @Serializable
    @SerialName(EnvMd5Text.TYPE)
    public data class EnvMd5Text(
        override val prop: String? = null,
        override val env: String? = null,
    ) : Md5BytesPasswordInfo(), EnvPasswordInfo {
        
        override fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): ByteArray {
            return Md5Text(configuration.envValue).getPassword(configuration)
        }
        
        public companion object {
            public const val TYPE: String = "env_${Md5Text.TYPE}"
        }
    }
}


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
    @Deprecated("Use passwordInfo") val password: String? = null,
    
    /**
     * 密码。与 [password] 和 [passwordMD5Bytes] 之间只能存在一个。
     */
    @Deprecated("Use passwordInfo") val passwordMD5: String? = null,
    
    /**
     * 密码。与 [password] 和 [passwordMD5] 之间只能存在一个。
     */
    @Suppress("ArrayInDataClass") @Deprecated("Use passwordInfo") val passwordMD5Bytes: ByteArray? = null,
    
    /**
     * 用户密码信息配置。
     */
    val passwordInfo: PasswordInfo,
    
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
        
        @Serializable(FileSerializer::class) val workingDir: File = BotConfiguration.Default.workingDir,
        
        val heartbeatPeriodMillis: Long = BotConfiguration.Default.heartbeatPeriodMillis,
        
        val statHeartbeatPeriodMillis: Long = BotConfiguration.Default.statHeartbeatPeriodMillis,
        
        val heartbeatTimeoutMillis: Long = BotConfiguration.Default.heartbeatTimeoutMillis,
        
        @Serializable(HeartbeatStrategySerializer::class) val heartbeatStrategy: BotConfiguration.HeartbeatStrategy = BotConfiguration.Default.heartbeatStrategy,
        
        val reconnectionRetryTimes: Int = BotConfiguration.Default.reconnectionRetryTimes,
        val autoReconnectOnForceOffline: Boolean = BotConfiguration.Default.autoReconnectOnForceOffline,
        
        @Serializable(MiraiProtocolSerializer::class) val protocol: BotConfiguration.MiraiProtocol = BotConfiguration.Default.protocol,
        
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
        
        @Serializable(FileSerializer::class) val cacheDir: File = BotConfiguration.Default.cacheDir,
        
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
        @SerialName("contactListCache") val contactListCacheConfiguration: ContactListCacheConfiguration = ContactListCacheConfiguration(),
        
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
            deviceInfoJson ?: simpleDeviceInfoJson?.toDeviceInfo() ?: if (deviceInfoFile?.isNotBlank() == true) {
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
            val text = javaClass.classLoader.getResourceAsStream(path)?.bufferedReader()?.readText()
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
        public val contactListCache: BotConfiguration.ContactListCache = BotConfiguration.ContactListCache().also {
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


@PublishedApi
internal class HeartbeatStrategySerializer : EnumStringSerializer<BotConfiguration.HeartbeatStrategy>(
    "HeartbeatStrategy", BotConfiguration.HeartbeatStrategy::valueOf
)

@PublishedApi
internal class MiraiProtocolSerializer :
    EnumStringSerializer<BotConfiguration.MiraiProtocol>("MiraiProtocol", BotConfiguration.MiraiProtocol::valueOf)


@OptIn(InternalApi::class)
internal class RecallMessageCacheStrategyTypeSerializer :
    EnumStringSerializer<MiraiBotVerifyInfoConfiguration.RecallMessageCacheStrategyType>(
        "RecallMessageCacheStrategyType", MiraiBotVerifyInfoConfiguration.RecallMessageCacheStrategyType::valueOf
    )