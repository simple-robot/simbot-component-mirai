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

package love.forte.simbot.component.mirai.bot

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import love.forte.simbot.FragileSimbotApi
import love.forte.simbot.LoggerFactory
import love.forte.simbot.SimbotIllegalStateException
import love.forte.simbot.bot.BotVerifyInfo
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.internal.InternalApi
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.LoggerAdapters.asMiraiLogger
import net.mamoe.mirai.utils.MiraiLogger
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
import java.io.File


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
     * 用户密码信息配置。
     */
    val passwordInfo: PasswordInfoConfiguration? = null,
    
    /**
     * 必要属性之外的额外配置属性。
     */
    var config: Config = Config()
) {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    public val component: String = MiraiComponent.ID_VALUE
    
    // region 弃用的密码配置
    
    /**
     * 密码。与 [passwordMD5] 和 [passwordMD5Bytes] 之间只能存在一个。
     */
    @Deprecated("Use passwordInfo")
    @ScheduledForRemoval(inVersion = "3.0.0")
    var password: String? = null
    
    /**
     * 密码。与 [password] 和 [passwordMD5Bytes] 之间只能存在一个。
     */
    @Deprecated("Use passwordInfo")
    @ScheduledForRemoval(inVersion = "3.0.0")
    var passwordMD5: String? = null
    
    /**
     * 密码。与 [password] 和 [passwordMD5] 之间只能存在一个。
     */
    @Deprecated("Use passwordInfo")
    @ScheduledForRemoval(inVersion = "3.0.0")
    var passwordMD5Bytes: ByteArray? = null
    
    // endregion
    
    /**
     * [MiraiBotVerifyInfoConfiguration] 中除了必要信息以外的额外配置信息。
     */
    @OptIn(FragileSimbotApi::class)
    @Serializable
    public data class Config(
        /** mirai配置自定义deviceInfoSeed的时候使用的随机种子。默认为1. */
        var deviceInfoSeed: Long = DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED,
        
        /**
         * Mirai配置中的工作目录。
         *
         * @see BotConfiguration.workingDir
         */
        @Serializable(FileSerializer::class) var workingDir: File = BotConfiguration.Default.workingDir,
        
        /**
         * 同mirai原生配置 [BotConfiguration.heartbeatPeriodMillis]。
         */
        var heartbeatPeriodMillis: Long = BotConfiguration.Default.heartbeatPeriodMillis,
        /**
         * 同mirai原生配置 [BotConfiguration.statHeartbeatPeriodMillis]。
         */
        var statHeartbeatPeriodMillis: Long = BotConfiguration.Default.statHeartbeatPeriodMillis,
        /**
         * 同mirai原生配置 [BotConfiguration.heartbeatTimeoutMillis]。
         */
        var heartbeatTimeoutMillis: Long = BotConfiguration.Default.heartbeatTimeoutMillis,
        
        /**
         * 同mirai原生配置 [BotConfiguration.heartbeatStrategy]。
         */
        @Serializable(HeartbeatStrategySerializer::class) var heartbeatStrategy: BotConfiguration.HeartbeatStrategy = BotConfiguration.Default.heartbeatStrategy,
        
        /**
         * 同mirai原生配置 [BotConfiguration.reconnectionRetryTimes]。
         */
        var reconnectionRetryTimes: Int = BotConfiguration.Default.reconnectionRetryTimes,
        
        /**
         * 同mirai原生配置 [BotConfiguration.autoReconnectOnForceOffline]。
         */
        var autoReconnectOnForceOffline: Boolean = BotConfiguration.Default.autoReconnectOnForceOffline,
        
        /**
         * 同mirai原生配置 [BotConfiguration.protocol]。
         */
        @Serializable(MiraiProtocolSerializer::class) var protocol: BotConfiguration.MiraiProtocol = BotConfiguration.Default.protocol,
        
        /**
         * 同mirai原生配置 [BotConfiguration.highwayUploadCoroutineCount]。
         */
        var highwayUploadCoroutineCount: Int = BotConfiguration.Default.highwayUploadCoroutineCount,
        
        /**
         * 如果是字符串，尝试解析为json
         * 否则视为文件路径。
         * 如果bot本身为json格式，则允许此处直接为json对象。
         *
         * 优先使用此属性。
         */
        var deviceInfoJson: DeviceInfo? = null,
        
        /**
         * 优先使用 [deviceInfo].
         */
        var simpleDeviceInfoJson: SimpleDeviceInfo? = null,
        
        /**
         * 加载的设备信息json文件的路径。
         * 如果是 `classpath:` 开头，则会优先尝试加载resource，
         * 否则优先视为文件路径加载。
         */
        var deviceInfoFile: String? = null,
        
        /**
         * 配置设备信息。
         *
         * @see DeviceInfoConfiguration
         */
        @SerialName("deviceInfo")
        var deviceInfoConfiguration: DeviceInfoConfiguration = DeviceInfoConfiguration.Auto(),
        
        /**
         * 是否不输出网络日志。当为true时等同于使用了 [BotConfiguration.noNetworkLog]
         */
        var noNetworkLog: Boolean = false,
        /**
         * 是否不输出Bot日志。当为true时等同于使用了 [BotConfiguration.noBotLog]
         */
        var noBotLog: Boolean = false,
        /**
         * 同原生配置 [BotConfiguration.isShowingVerboseEventLog]
         */
        var isShowingVerboseEventLog: Boolean = BotConfiguration.Default.isShowingVerboseEventLog,
        
        /**
         * 同原生配置 [BotConfiguration.cacheDir]
         */
        @Serializable(FileSerializer::class) var cacheDir: File = BotConfiguration.Default.cacheDir,
        
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
        @SerialName("contactListCache") var contactListCacheConfiguration: ContactListCacheConfiguration = ContactListCacheConfiguration(),
        
        /**
         * 是否开启登录缓存。
         * @see BotConfiguration.loginCacheEnabled
         */
        var loginCacheEnabled: Boolean = BotConfiguration.Default.loginCacheEnabled,
        
        /**
         * 是否处理接受到的特殊换行符, 默认为 true
         * @see BotConfiguration.convertLineSeparator
         */
        var convertLineSeparator: Boolean = BotConfiguration.Default.convertLineSeparator,
        
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
        var recallMessageCacheStrategy: RecallMessageCacheStrategyType = RecallMessageCacheStrategyType.INVALID,

        /**
         * 如果为 `true`, 则会使用 [BotConfiguration.disableAccountSecretes] 禁用 `account.secrets` 的保存。
         *
         * @see BotConfiguration.disableAccountSecretes
         */
        var accountSecrets: Boolean = false
        
        ) {
        
        @Transient
        private val deviceInfo: (Bot) -> DeviceInfo = deviceInfoConfiguration.also {
            deviceInfoCompatibleCheck()
        }
        
        private fun deviceInfoCompatibleCheck() {
            // deviceInfoJson
            if (deviceInfoJson != null) {
                val illegalProp = "deviceInfoJson"
                val warning = SimbotIllegalStateException(
                    """
                    The configuration property [config.$illegalProp] is deprecated.
                
                    Maybe you should replace the property [config.$illegalProp]:
                    
                    ```
                    {
                       "config": {
                          "$illegalProp": deviceInfoJson
                       }
                    }
                    ```
                   
                    to [config.deviceInfo]:
                    
                    ```
                    {
                       "config": {
                           "deviceInfo": {
                              "type": "${DeviceInfoConfiguration.JsonObj.TYPE}",
                              "object": deviceInfoJson
                           }
                       }
                    }
                    ```
                    
                    See [DeviceInfoConfiguration] and [MiraiBotVerifyInfoConfiguration.Config.deviceInfo] for more information.
                    
                """.trimIndent()
                )
                log.error("Deprecated config property", warning)
            }
            
            // simpleDeviceInfoJson
            if (simpleDeviceInfoJson != null) {
                val illegalProp = "simpleDeviceInfoJson"
                val warning = SimbotIllegalStateException(
                    """
                    The configuration property [config.$illegalProp] is deprecated.
                
                    Maybe you should replace the property [config.$illegalProp]:
                    
                    ```
                    {
                       "config": {
                          "$illegalProp": simpleDeviceInfoJson
                       }
                    }
                    ```
                   
                    to [config.deviceInfo]:
                    
                    ```
                    {
                       "config": {
                           "deviceInfo": {
                              "type": "${DeviceInfoConfiguration.SimpleJsonObj.TYPE}",
                              "object": simpleDeviceInfoJson
                           }
                       }
                    }
                    ```
                    
                    See [DeviceInfoConfiguration] and [MiraiBotVerifyInfoConfiguration.Config.deviceInfo] for more information.
                    
                """.trimIndent()
                )
                log.error("Deprecated config property", warning)
            }
            
            // deviceInfoFile
            if (deviceInfoFile != null) {
                val illegalProp = "deviceInfoFile"
                val warning = SimbotIllegalStateException(
                    """
                    The configuration property [config.$illegalProp] is deprecated.
                
                    Maybe you should replace the property [config.$illegalProp]:
                    
                    ```
                    {
                       "config": {
                          "$illegalProp": "$deviceInfoFile"
                       }
                    }
                    ```
                   
                    to [config.deviceInfo]:
                    
                    ```
                    {
                       "config": {
                           "deviceInfo": {
                              "type": "${DeviceInfoConfiguration.Resource.TYPE}",
                              "paths": ["$deviceInfoFile"]
                           }
                       }
                    }
                    ```
                    
                    See [DeviceInfoConfiguration] and [MiraiBotVerifyInfoConfiguration.Config.deviceInfo] for more information.
                    
                """.trimIndent()
                )
                log.error("Deprecated config property", warning)
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
                
                if (accountSecrets) {
                    it.disableAccountSecretes()
                }
                
            }
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
            private val log = LoggerFactory.getLogger<Config>()
            
            @JvmField
            @Deprecated("Unused")
            public val DEFAULT: Config = Config()
            
            /**
             * 构建一个新的 [Config] 实例。
             *
             * ```kotlin
             * // function
             * buildConfig {
             *   // ...
             * }
             * ```
             *
             * ```java
             * Config config = Config.build(builder -> {
             *      // ...
             *  });
             * ```
             *
             */
            @JvmStatic
            @JvmName("build")
            public fun buildConfig(builder: BuilderFunction<Config>): Config {
                return Config().doBuild(builder)
            }
            
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
        
        // 想要更多实现? see: StandardMiraiRecallMessageCacheStrategy
        
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
    public data class ContactListCacheConfiguration(
        /**
         *
         * @see BotConfiguration.ContactListCache.saveIntervalMillis
         */
        var saveIntervalMillis: Long = BotConfiguration.Default.contactListCache.saveIntervalMillis,
        /**
         *
         * @see BotConfiguration.ContactListCache.friendListCacheEnabled
         */
        var friendListCacheEnabled: Boolean = BotConfiguration.Default.contactListCache.friendListCacheEnabled,
        /**
         *
         * @see BotConfiguration.ContactListCache.groupMemberListCacheEnabled
         */
        var groupMemberListCacheEnabled: Boolean = BotConfiguration.Default.contactListCache.groupMemberListCacheEnabled,
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
            @Deprecated("Unused")
            public val DEFAULT: ContactListCacheConfiguration = ContactListCacheConfiguration()
    
            /**
             * 构建一个 [ContactListCacheConfiguration] 实例。
             *
             * ```kotlin
             * buildContactListCacheConfiguration {
             *    saveIntervalMillis = 123
             *    friendListCacheEnabled = false
             *    groupMemberListCacheEnabled = false
             * }
             * ```
             *
             * ```java
             * ContactListCacheConfiguration configuration = ContactListCacheConfiguration.build(config -> {
             *    config.setSaveIntervalMillis(123);
             *    config.setFriendListCacheEnabled(false);
             *    config.setGroupMemberListCacheEnabled(false);
             * });
             * ```
             *
             */
            @JvmStatic
            @JvmName("build")
            public fun buildContactListCacheConfiguration(builder: BuilderFunction<ContactListCacheConfiguration>): ContactListCacheConfiguration {
                return ContactListCacheConfiguration().doBuild(builder)
            }
            
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


//region Serializers

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

//endregion


public fun interface BuilderFunction<T> {
    public operator fun T.invoke()
}

private fun <T> T.doBuild(builder: BuilderFunction<T>): T {
    return apply { builder.apply { invoke() } }
}