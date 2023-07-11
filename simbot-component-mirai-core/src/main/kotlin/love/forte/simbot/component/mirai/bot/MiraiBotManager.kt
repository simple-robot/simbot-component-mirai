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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import love.forte.simbot.*
import love.forte.simbot.application.ApplicationConfiguration
import love.forte.simbot.application.EventProviderAutoRegistrarFactory
import love.forte.simbot.application.EventProviderFactory
import love.forte.simbot.bot.BotAlreadyRegisteredException
import love.forte.simbot.bot.BotManager
import love.forte.simbot.bot.BotVerifyInfo
import love.forte.simbot.bot.ComponentMismatchException
import love.forte.simbot.component.mirai.MiraiBotConfiguration
import love.forte.simbot.component.mirai.MiraiComponent
import love.forte.simbot.component.mirai.internal.InternalApi
import love.forte.simbot.component.mirai.internal.MiraiBotManagerImpl
import love.forte.simbot.event.EventProcessor
import net.mamoe.mirai.Bot
import net.mamoe.mirai.auth.BotAuthorization
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

        @Suppress("DEPRECATION")
        val passwordInfo = configuration.passwordInfo

        val authorization = configuration.authorization

        if (passwordInfo != null) {
            logger.warn(
                """
                The `passwordInfo' configuration property is deprecated, you may want to replace the `passwordInfo' configuration property with `authorization`.
                ```json
                {
                  "code": {},
                  "authorization": { ... } <---- replace 'passwordInfo' with 'authorization'
                  ...
                }
                ```
            """.trimIndent(), configuration.code
            )
            when (passwordInfo) {
                is TextPasswordInfoConfiguration -> {
                    return register(
                        code = configuration.code,
                        password = passwordInfo.getPassword(configuration),
                        configuration = configuration.simbotBotConfiguration,
                    )
                }

                is Md5BytesPasswordInfoConfiguration -> {
                    return register(
                        code = configuration.code,
                        password = passwordInfo.getPassword(configuration),
                        configuration = configuration.simbotBotConfiguration,
                    )
                }

                is AuthorizationConfiguration -> {
                    return register(
                        code = configuration.code,
                        authorization = passwordInfo.getBotAuthorization(configuration),
                        configuration = configuration.simbotBotConfiguration
                    )
                }
            }
        }


        return register(
            code = configuration.code,
            authorization = authorization?.getBotAuthorization(configuration)
                ?: throw IllegalArgumentException("The required attribute 'authorization' is not configured."),
            configuration = configuration.simbotBotConfiguration
        )
    }


    /**
     * 注册一个Bot。
     *
     * 此函数构建的 [MiraiBot] 中，如果配置了[MiraiBotConfiguration.initialBotConfiguration],
     * 则将会完全的直接使用 [configuration] 中的 [MiraiBotConfiguration.initialBotConfiguration],
     * 包括其中的设备信息配置、logger配置等。
     *
     * @throws BotAlreadyRegisteredException 如果bot已经在当前manager中存在
     *
     * @param code 账号
     * @param password 密码
     * @param configuration simbot中的bot配置类
     */
    public fun register(code: Long, password: String, configuration: MiraiBotConfiguration): MiraiBot =
        register(code, BotAuthorization.byPassword(password), configuration)


    /**
     * 注册一个Bot。
     *
     * 此函数构建的 [MiraiBot] 中，如果配置了[MiraiBotConfiguration.initialBotConfiguration],
     * 则将会完全的直接使用 [configuration] 配置结果中的 [MiraiBotConfiguration.initialBotConfiguration],
     * 包括其中的设备信息配置、logger配置等。
     *
     * @throws BotAlreadyRegisteredException 如果bot已经在当前manager中存在
     *
     * @param code 账号
     * @param password 密码
     * @param configuration simbot中的bot配置类配置函数
     */
    public fun register(code: Long, password: String, configuration: MiraiBotConfigurationConfigurator): MiraiBot =
        register(code, password, configuration.run { MiraiBotConfiguration().also { c -> c.config() } })


    /**
     * 注册一个Bot。
     *
     * @throws BotAlreadyRegisteredException 如果bot已经在当前manager中存在
     *
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
     * @throws BotAlreadyRegisteredException 如果bot已经在当前manager中存在
     *
     * @param code 账号
     * @param password 密码的MD5字节数组
     * @param configuration simbot bot 配置
     */
    public fun register(code: Long, password: ByteArray, configuration: MiraiBotConfiguration): MiraiBot =
        register(code, BotAuthorization.byPassword(password), configuration)


    /**
     * 注册一个Bot。
     *
     * 此函数构建的 [MiraiBot] 中，如果配置了[MiraiBotConfiguration.initialBotConfiguration],
     * 则将会完全的直接使用 [configuration] 中的 [MiraiBotConfiguration.initialBotConfiguration],
     * 包括其中的设备信息配置、logger配置等。
     *
     * @since 3.0.0.0-M6
     *
     * @param code bot的账号
     * @param authorization bot登陆用的鉴权方式
     * @param configuration simbot-mirai 组件的 bot 配置
     *
     * @throws BotAlreadyRegisteredException 如果bot已经在当前manager中存在
     * @throws Exception 其他可能在mirai注册过程中产生的异常
     *
     */
    public abstract fun register(
        code: Long,
        authorization: BotAuthorization,
        configuration: MiraiBotConfiguration
    ): MiraiBot

    /**
     * 注册一个Bot。
     *
     * 此函数构建的 [MiraiBot] 中，如果配置了[MiraiBotConfiguration.initialBotConfiguration],
     * 则将会完全的直接使用 [configuration] 中的 [MiraiBotConfiguration.initialBotConfiguration],
     * 包括其中的设备信息配置、logger配置等。
     *
     * @since 3.0.0.0-M7
     *
     * @param code bot的账号
     * @param authorization bot登陆用的鉴权方式
     * @param configuration simbot-mirai 组件的 bot 配置
     *
     * @throws BotAlreadyRegisteredException 如果bot已经在当前manager中存在
     * @throws Exception 其他可能在mirai注册过程中产生的异常
     *
     */
    public fun register(
        code: Long,
        authorization: BotAuthorization,
        configuration: MiraiBotConfigurationConfigurator
    ): MiraiBot =
        register(code, authorization, configuration.run { MiraiBotConfiguration().also { c -> c.config() } })


    /**
     * 注册一个Bot。
     *
     * 此函数构建的 [MiraiBot] 中，如果配置了[MiraiBotConfiguration.initialBotConfiguration],
     * 则将会完全的直接使用 [configuration] 中的 [MiraiBotConfiguration.initialBotConfiguration],
     * 包括其中的设备信息配置、logger配置等。
     *
     * @throws BotAlreadyRegisteredException 如果bot已经在当前manager中存在
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
     * @throws BotAlreadyRegisteredException 如果bot已经在当前manager中存在
     *
     * @param code 账号
     * @param passwordMD5 密码的MD5字节数组
     */
    public fun register(code: Long, passwordMD5: ByteArray): MiraiBot =
        register(code, passwordMD5, MiraiBotConfiguration())


    /**
     * 注册一个bot。
     *
     * 直接通过一个 [mirai bot][Bot] 注册，并包装为 [MiraiBot]。
     * 使用的 [bot] 不会进行任何操作而直接通过 [MiraiBot] 进行**包装**。
     *
     * 额外提供一个 [MiraiBotConfiguration][configuration],
     * 但是不会使用其中的 [MiraiBotConfiguration.initialBotConfiguration] 来对 [bot]
     * 进行任何操作，而只使用其他组件可能会用到的属性，例如 [MiraiBotConfiguration.recallCacheStrategy]。
     *
     * **实验性：此API尚处于试验阶段，可能会随时变更/删除。**
     *
     * @param bot 被包装的mirai原始bot类型。
     * @param configuration 用于提供组件所需信息的配置类，默认为 `null`。为 `null` 时会构建一个属性均为默认值的实例。
     *
     * @throws BotAlreadyRegisteredException 如果bot已经在当前manager中存在
     *
     * @since 3.0.0.0-RC.2
     *
     */
    @ExperimentalSimbotApi
    public abstract fun register(bot: Bot, configuration: MiraiBotConfiguration? = null): MiraiBot


    /**
     * [MiraiBotManager] 的构造工厂。
     *
     */
    public companion object Factory : EventProviderFactory<MiraiBotManager, MiraiBotManagerConfiguration> {
        override val key: Attribute<MiraiBotManager> = attribute("SIMBOT.MIRAI")

        override suspend fun create(
            eventProcessor: EventProcessor,
            components: List<Component>,
            applicationConfiguration: ApplicationConfiguration,
            configurator: MiraiBotManagerConfiguration.() -> Unit,
        ): MiraiBotManager {
            // configurator ignore
            // find self
            val component = components.find { it is MiraiComponent } as? MiraiComponent
                ?: throw NoSuchComponentException("There are no MiraiComponent(id=${MiraiComponent.ID_VALUE}) registered in the current application.")

            val configuration = MiraiBotManagerConfigurationImpl().also {
                it.parentCoroutineContext = applicationConfiguration.coroutineContext
                configurator(it)
            }

            return MiraiBotManagerImpl(eventProcessor, component, configuration)
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
@Suppress("DEPRECATION_ERROR")
public interface MiraiBotManagerConfiguration {

    /**
     * 用于使用在 [MiraiBotManager] 中以及作为所有Bot的父类协程上下文。
     *
     * 会使用 [ApplicationConfiguration] 中的配置作为初始值。
     *
     */
    public var parentCoroutineContext: CoroutineContext
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

