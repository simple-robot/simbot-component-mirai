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

package love.forte.simbot.component.mirai.bot

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import love.forte.simbot.FragileSimbotApi
import love.forte.simbot.SimbotIllegalArgumentException
import love.forte.simbot.SimbotIllegalStateException
import love.forte.simbot.bot.BotVerifyInfo
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.internal.InternalApi
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.logger.logger
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
 * 对一个bot的配置, 账号（[code]）与密码（[passwordInfo]）为必须属性,
 * 其他额外配置位于 [config] 属性中。
 *
 * ### 仅序列化用
 * [MiraiBotVerifyInfoConfiguration] 是用于对应配置文件进行序列化/反序列化的，因此其仅考虑面向配置文件兼容/二进制兼容，
 * 而不保证源码兼容，需要尽可能避免直接在代码中构建此类。
 *
 * @see BotConfiguration
 */
@Serializable
@InternalApi
@OptIn(FragileSimbotApi::class)
public data class MiraiBotVerifyInfoConfiguration(
    /**
     * 账号。
     */
    val code: Long,

    /**
     * 用户密码信息配置。
     */
    @Suppress("DEPRECATION")
    @Deprecated("use 'authorization'")
    val passwordInfo: PasswordInfoConfiguration? = null,


    val authorization: BotAuthorizationConfiguration? = null,

    /**
     * 必要属性之外的额外配置属性。
     */
    var config: Config = Config(),
) {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    public val component: String = MiraiComponent.ID_VALUE

    // region 弃用的密码配置

    /**
     * @suppress see [passwordInfo]
     */
    @Deprecated("Use passwordInfo", level = DeprecationLevel.ERROR)
    @ScheduledForRemoval(inVersion = "3.0.0.0")
    var password: String? = null

    /**
     * @suppress see [passwordInfo]
     */
    @Deprecated("Use passwordInfo", level = DeprecationLevel.ERROR)
    @ScheduledForRemoval(inVersion = "3.0.0.0")
    var passwordMD5: String? = null

    /**
     * @suppress see [passwordInfo]
     */
    @Deprecated("Use passwordInfo", level = DeprecationLevel.ERROR)
    @ScheduledForRemoval(inVersion = "3.0.0.0")
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
        @Serializable(FileSerializer::class)
        @Transient
        var workingDir: File = BotConfiguration.Default.workingDir,

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
        @Deprecated("Use deviceInfoConfiguration", level = DeprecationLevel.ERROR)
        @ScheduledForRemoval(inVersion = "3.0.0.0")
        var deviceInfoJson: DeviceInfo? = null,

        /**
         * 优先使用 [deviceInfo].
         */
        @Deprecated("Use deviceInfoConfiguration", level = DeprecationLevel.ERROR)
        @ScheduledForRemoval(inVersion = "3.0.0.0")
        var simpleDeviceInfoJson: SimpleDeviceInfo? = null,

        /**
         * 加载的设备信息json文件的路径。
         * 如果是 `classpath:` 开头，则会优先尝试加载resource，
         * 否则优先视为文件路径加载。
         */
        @Deprecated("Use deviceInfoConfiguration", level = DeprecationLevel.ERROR)
        @ScheduledForRemoval(inVersion = "3.0.0.0")
        var deviceInfoFile: String? = null,

        /**
         * 配置设备信息。
         *
         * @see DeviceInfoConfiguration
         */
        @SerialName("deviceInfo")
        var deviceInfoConfiguration: DeviceInfoConfiguration? = DeviceInfoConfiguration.Auto(),

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
         * 类似原生配置 [BotConfiguration.cacheDir], 但是此处默认值为null。
         *
         * 当值为null时，[cacheDir] 会使用 `BotConfiguration.Default.cacheDir.resolve(botCode)` 路径作为缓存目录。
         * 需要注意的是，这与 BotConfiguration [默认配置][BotConfiguration.Default] 中的行为不同。
         * 由于 [mirai#2475](https://github.com/mamoe/mirai/issues/2475) 的原因，当需要启用 `account.secrets` 的保存时（即 [disableAccountSecretes] == false 时）
         * 需要保证各bot缓存目录的独立。
         *
         */
        @Serializable(FileSerializer::class) var cacheDir: File? = null,

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
         * Deprecated: 使用 [recallMessageCacheStrategyConfig] 来得到对未来更好的扩展性和更丰富的可配置性。
         *
         * @see recallMessageCacheStrategyConfig
         * @suppress Use [recallMessageCacheStrategyConfig] plz.
         */
        @Deprecated("Use 'recallMessageCacheStrategyConfig'", level = DeprecationLevel.ERROR)
        @ScheduledForRemoval(inVersion = "3.0.0.0")
        var recallMessageCacheStrategy: RecallMessageCacheStrategyType? = null,

        /**
         * 消息撤回缓存策略，默认使用 [不缓存][RecallMessageCacheStrategyConfiguration.Invalid] 策略。
         *
         * ```json
         * {
         *   "recallMessageCacheStrategyConfig": {
         *      "type": "invalid"
         *   }
         * }
         * ```
         *
         * @see RecallMessageCacheStrategyConfiguration
         */
        var recallMessageCacheStrategyConfig: RecallMessageCacheStrategyConfiguration = RecallMessageCacheStrategyConfiguration.invalid(),

        /**
         * 如果为 `true`, 则会使用 [BotConfiguration.disableAccountSecretes] 禁用 `account.secrets` 的保存。
         *
         * > Deprecated: 由于属性名与实际行为存在歧义，因此使用新的 [disableAccountSecretes] 来代替此属性。
         */
        @Deprecated("Use disableAccountSecretes")
        var accountSecrets: Boolean? = null,


        /**
         * 是否禁止保存 `account.secrets`.
         * 如果为 `true`, 则会使用 [BotConfiguration.disableAccountSecretes] 禁用 `account.secrets` 的保存。
         *
         * @see BotConfiguration.disableAccountSecretes
         */
        var disableAccountSecretes: Boolean = false,

        ) {


        /**
         * deviceInfo 构造器
         */
        @Transient
        private val deviceInfo: ((Bot) -> DeviceInfo)? = deviceInfoConfiguration.also {
            deviceInfoCompatibleCheck()
        }

        @Suppress("DEPRECATION_ERROR")
        private fun deviceInfoCompatibleCheck() {
            // deviceInfoJson
            if (deviceInfoJson != null) {
                val illegalProp = "deviceInfoJson"
                val error = SimbotIllegalStateException(
                    """
                    The configuration property [config.$illegalProp] is deprecated and will be removed.
                
                    Maybe you should replace the property [config.$illegalProp]:
                    
                    ```
                    {
                       "config": {
                          "$illegalProp": { ... },
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

                throw DeprecatedConfigurationPropertyException("config.$illegalProp", error)
            }

            // simpleDeviceInfoJson
            if (simpleDeviceInfoJson != null) {
                val illegalProp = "simpleDeviceInfoJson"
                val error = SimbotIllegalStateException(
                    """
                    The configuration property [config.$illegalProp] is deprecated and will be removed.
                
                    Maybe you should replace the property [config.$illegalProp]:
                    
                    ```
                    {
                       "config": {
                          "$illegalProp": { ... }
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

                throw DeprecatedConfigurationPropertyException("config.$illegalProp", error)
            }

            // deviceInfoFile
            if (deviceInfoFile != null) {
                val illegalProp = "deviceInfoFile"
                val error = SimbotIllegalStateException(
                    """
                    The configuration property [config.$illegalProp] is deprecated and will be removed.
                
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
                throw DeprecatedConfigurationPropertyException("config.$illegalProp", error)
            }
        }


        /**
         * 将当前配置的信息转化为 [MiraiBotConfiguration][BotConfiguration] 实例。
         */
        internal fun miraiBotConfiguration(self: MiraiBotVerifyInfoConfiguration, initial: BotConfiguration): BotConfiguration {
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

                val cacheDir0 = cacheDir ?: BotConfiguration.Default.cacheDir.resolve(self.code.toString())

                it.cacheDir = cacheDir0
                it.contactListCache = contactListCacheConfiguration.contactListCache
                it.loginCacheEnabled = loginCacheEnabled
                it.convertLineSeparator = convertLineSeparator

                @Suppress("DEPRECATION")
                if (accountSecrets != null) {
                    // 过时兼容
                    logger.warn("The property `config.accountSecrets` is deprecated, use `config.disableAccountSecretes` instead")
                    it.disableAccountSecretes()
                }

                if (disableAccountSecretes) {
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
            private val logger = LoggerFactory.logger<Config>()

            @JvmField
            @Deprecated("Unused", level = DeprecationLevel.ERROR)
            @ScheduledForRemoval(inVersion = "3.0.0.0")
            public val DEFAULT: Config = Config()

            /**
             * 构建一个新的 [Config] 实例。
             *
             * ```kotlin
             * // Kotlin
             * buildConfig {
             *   // ...
             * }
             * ```
             *
             * ```java
             * // Java
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
    private fun miraiBotConfiguration(initial: BotConfiguration): BotConfiguration =
        config.miraiBotConfiguration(this, initial)


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
            @Deprecated("Unused", level = DeprecationLevel.ERROR)
            @ScheduledForRemoval(inVersion = "3.0.0.0")
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
                recallMessageCacheStrategy
            ).apply {
                botConfiguration {
                    miraiBotConfiguration(this)
                }
            }
        }


    @Suppress("DEPRECATION_ERROR")
    private val recallMessageCacheStrategy: MiraiRecallMessageCacheStrategy
        get() {
            val deprecatedConfig = config.recallMessageCacheStrategy
            if (deprecatedConfig != null) {
                val toTypeName: String = when (deprecatedConfig) {
                    RecallMessageCacheStrategyType.INVALID -> {
                        RecallMessageCacheStrategyConfiguration.Invalid.TYPE
                    }

                    RecallMessageCacheStrategyType.MEMORY_LRU -> {
                        RecallMessageCacheStrategyConfiguration.MemoryLru.TYPE
                    }
                }
                val err = SimbotIllegalArgumentException(
                    """
                    The configuration property [recallMessageCacheStrategy] is deprecated and will be removed.
                    
                    Maybe you should replace the property [recallMessageCacheStrategy]:
                
                    ```
                    {
                      "code": $code,
                      "passwordInfo": { ... },
                      "config": {
                          "recallMessageCacheStrategy": "${deprecatedConfig.name}"
                      }
                    }
                    ```
                    
                    to [recallMessageCacheStrategyConfig]:
                    
                    ```
                    {
                      "code": $code,
                      "passwordInfo": { ... },
                      "config": {
                          "recallMessageCacheStrategyConfig": {
                              "type": "$toTypeName"
                          }
                      }
                    }
                    ```
                
                    See [RecallMessageCacheStrategyConfiguration] and [MiraiBotVerifyInfoConfiguration.Config.recallMessageCacheStrategyConfig] for more information.
                    
                """.trimIndent()
                )
                throw DeprecatedConfigurationPropertyException("config.recallMessageCacheStrategy", err)
            }

            return config.recallMessageCacheStrategyConfig.recallMessageCacheStrategy(this)
        }

}


// region Serializers

/**
 * 文件路径序列化器。
 */
internal object FileSerializer : KSerializer<File> {
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

// endregion


public fun interface BuilderFunction<T> {
    public operator fun T.invoke()
}

private fun <T> T.doBuild(builder: BuilderFunction<T>): T {
    return apply { builder.apply { invoke() } }
}


internal class DeprecatedConfigurationPropertyException(message: String, cause: Throwable? = null) :
    SimbotIllegalStateException(message, cause)
