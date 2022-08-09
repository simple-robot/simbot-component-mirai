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
import love.forte.simbot.bot.BotVerifyInfo
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.bot.MiraiBotVerifyInfoConfiguration.PasswordInfo.Text
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
    @Deprecated("Use passwordInfo")
    val password: String? = null,
    
    /**
     * 密码。与 [password] 和 [passwordMD5Bytes] 之间只能存在一个。
     */
    @Deprecated("Use passwordInfo")
    val passwordMD5: String? = null,
    
    /**
     * 密码。与 [password] 和 [passwordMD5] 之间只能存在一个。
     */
    @Suppress("ArrayInDataClass")
    @Deprecated("Use passwordInfo")
    val passwordMD5Bytes: ByteArray? = null,
    
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
     * 通过 [MiraiBotVerifyInfoConfiguration.passwordInfo] 来提供多种形式的密码配置。
     *
     * 目前支持的形式有：
     * - [明文密码][Text]
     *
     */
    @Serializable
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
        @SerialName("text")
        public data class Text(val text: String) : PasswordInfo()
        
        
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
        @SerialName("md5_text")
        public data class Md5Text(val md5: String) : PasswordInfo()
        
        
        /**
         * MD5密码（字节数据）类型。
         * ```json
         * {
         *    "type": "md5_bytes",
         *    "md5": [-24, 7, -15, -4, -15, 45, 19, 47, -101, -80, 24, -54, 103, 56, -95, -97]
         * }
         * ```
         */
        @Serializable
        @SerialName("md5_bytes")
        public data class Md5Bytes(val md5: ByteArray) : PasswordInfo() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Md5Bytes) return false
                
                return md5.contentEquals(other.md5)
            }
            
            override fun hashCode(): Int {
                return md5.contentHashCode()
            }
        }
        
    }
    
    
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