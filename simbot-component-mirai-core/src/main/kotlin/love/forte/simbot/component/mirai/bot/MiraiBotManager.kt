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
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import love.forte.simbot.*
import love.forte.simbot.application.ApplicationBuilder
import love.forte.simbot.application.ApplicationConfiguration
import love.forte.simbot.application.EventProviderAutoRegistrarFactory
import love.forte.simbot.application.EventProviderFactory
import love.forte.simbot.bot.BotManager
import love.forte.simbot.bot.BotVerifyInfo
import love.forte.simbot.bot.ComponentMismatchException
import love.forte.simbot.component.mirai.MiraiBotConfiguration
import love.forte.simbot.component.mirai.MiraiComponent
import love.forte.simbot.component.mirai.internal.InternalApi
import love.forte.simbot.component.mirai.internal.MiraiBotManagerImpl
import love.forte.simbot.event.EventProcessor
import net.mamoe.mirai.BotFactory
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


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
        
        compatibleCheck(configuration)
        
        when (val passwordInfo = configuration.passwordInfo) {
            is TextPasswordInfo -> {
                return register(
                    code = configuration.code,
                    password = passwordInfo.getPassword(configuration),
                    configuration = configuration.simbotBotConfiguration,
                )
            }
            
            is Md5BytesPasswordInfo -> {
                return register(
                    code = configuration.code,
                    passwordMD5 = passwordInfo.getPassword(configuration),
                    configuration = configuration.simbotBotConfiguration,
                )
            }
        }
    }
    
    @Suppress("DEPRECATION")
    @OptIn(InternalApi::class)
    private fun compatibleCheck(configuration: MiraiBotVerifyInfoConfiguration) {
        val password = configuration.password
        if (password != null) {
            val showPwd =
                if (logger.isDebugEnabled) password else "${password.firstOrNull() ?: "?"}*****${password.lastOrNull() ?: "?"}"
            throw SimbotIllegalStateException(
                """
                The configuration property [password] is deprecated. Maybe you should replace the property [password]
                
                ```
                {
                  "code": ${configuration.code},
                  "password": "$showPwd"
                }
                ```
                
                to [passwordInfo]:
                
                ```
                {
                  "code": ${configuration.code},
                  "passwordInfo": {
                     "type": "${PasswordInfo.Text.TYPE}",
                     "text": "$showPwd"
                  }
                }
                ```
                
                See [PasswordInfo] and [MiraiBotVerifyInfoConfiguration.passwordInfo] for more information.
            """.trimIndent()
            )
        }
        
        
        val passwordMD5 = configuration.passwordMD5
        if (passwordMD5 != null) {
            val showPwd =
                if (logger.isDebugEnabled) passwordMD5 else "${passwordMD5.firstOrNull() ?: "?"}*****${passwordMD5.lastOrNull() ?: "?"}"
            
            throw SimbotIllegalStateException(
                """
                The configuration property [passwordMD5] is deprecated. Maybe you should replace the property [passwordMD5]
                
                ```
                {
                  "code": ${configuration.code},
                  "passwordMD5": "$showPwd"
                }
                ```
                
                to [passwordInfo]:
                
                ```
                {
                  "code": ${configuration.code},
                  "passwordInfo": {
                     "type": "${PasswordInfo.Md5Text.TYPE}",
                     "md5": "$showPwd"
                  }
                }
                ```
                
                See [PasswordInfo] and [MiraiBotVerifyInfoConfiguration.passwordInfo] for more information.
            """.trimIndent()
            )
            
        }
        
        
        val passwordMD5Bytes = configuration.passwordMD5Bytes
        if (passwordMD5Bytes != null) {
            val showPwd =
                if (logger.isDebugEnabled) passwordMD5Bytes.joinToString(
                    ", ",
                    "[",
                    "]"
                ) else "[**, **, **]"
            
            throw SimbotIllegalStateException(
                """
                The configuration property [passwordMD5Bytes] is deprecated. Maybe you should replace the property [passwordMD5Bytes]
                
                ```
                {
                  "code": ${configuration.code},
                  "passwordMD5Bytes": $showPwd
                }
                ```
                
                to [passwordInfo]:
                
                ```
                {
                  "code": ${configuration.code},
                  "passwordInfo": {
                     "type": "${PasswordInfo.Md5Bytes.TYPE}",
                     "md5": $showPwd
                  }
                }
                ```
                
                See [PasswordInfo] and [MiraiBotVerifyInfoConfiguration.passwordInfo] for more information.
            """.trimIndent()
            )
        }
        
        
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
    public fun register(
        code: Long,
        passwordMD5: ByteArray,
        configuration: MiraiBotConfigurationConfigurator,
    ): MiraiBot =
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

