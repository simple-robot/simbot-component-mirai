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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.SimbotIllegalArgumentException
import love.forte.simbot.component.mirai.internal.InternalApi
import net.mamoe.mirai.auth.BotAuthorization

/**
 * 通过 [MiraiBotVerifyInfoConfiguration.passwordInfo] 来提供多种形式的密码配置。
 *
 * 目前支持的形式有：
 * ### [明文密码][TextPasswordInfoConfiguration]:
 *
 * - [普通明文密码][PasswordInfoConfiguration.Text]
 * - [环境变量明文密码][PasswordInfoConfiguration.EnvText]
 *
 * ### [md5密码][Md5BytesPasswordInfoConfiguration]:
 *
 * - [md5字符串密码][PasswordInfoConfiguration.Md5Text]
 * - [环境变量md5字符串密码][PasswordInfoConfiguration.EnvMd5Text]
 * - [md5字节组密码][PasswordInfoConfiguration.Md5Bytes]
 *
 * ### [二维码扫描][QRCode]
 *
 * ## [BotAuthorizationConfiguration]
 *
 * 使用更符合语义的 [BotAuthorizationConfiguration] 来进行配置
 *
 * @see BotAuthorizationConfiguration
 */
@Serializable
@OptIn(InternalApi::class)
@Deprecated("use 'BotAuthorizationConfiguration' plz")
public sealed class PasswordInfoConfiguration {
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
    public data class Text(val text: String) : TextPasswordInfoConfiguration() {
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
    public data class Md5Text(val md5: String) : Md5BytesPasswordInfoConfiguration() {
        
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
    public data class Md5Bytes(val md5: ByteArray) : Md5BytesPasswordInfoConfiguration() {
        
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Md5Bytes) return false
            
            return md5.contentEquals(other.md5)
        }
        
        override fun hashCode(): Int = md5.contentHashCode()
        
        override fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): ByteArray = md5
        
        public companion object {
            public const val TYPE: String = "md5_bytes"
        }
    }
    
    
    /**
     * 内部用于标记后续的通过**环境变量**配置形式相关的 [PasswordInfoConfiguration] 实现，
     * 并为它们提供统一的行为和辅助功能。
     *
     * ## 配置属性
     *
     * [EnvPasswordInfoConfiguration] 的实现提供了两个可选属性：[env] 和 [prop]。
     * 这两个属性都是可选的，但是应当至少提供其中一个。换言之，它们不能**同时为空**。
     *
     * 在选取使用时，[prop] 的优先级高于 [env]，[EnvPasswordInfoConfiguration] 会优先尝试通过 [prop] (通过[System.getProperty])
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
    public sealed interface EnvPasswordInfoConfiguration {
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
                    prop0?.replaceCodeMark()?.let(System::getProperty)
                        ?: env0?.replaceCodeMark()?.let(System::getenv)
                
                return value ?: throw NoSuchElementException(buildString {
                    if (prop0 != null) {
                        // 键为 [a] 的prop的值
                        append("value of [prop] with key [").append(prop0.replaceCodeMark()).append("]; ")
                    }
                    if (env0 != null) {
                        append("value of [env] with key [").append(env0.replaceCodeMark()).append("]; ")
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
     * 更多信息参阅 [EnvPasswordInfoConfiguration] 接口的描述。
     *
     *
     *
     * @see EnvPasswordInfoConfiguration
     *
     */
    @Serializable
    @SerialName(EnvText.TYPE)
    public data class EnvText(
        override val prop: String? = null,
        override val env: String? = null,
    ) : TextPasswordInfoConfiguration(), EnvPasswordInfoConfiguration {
        
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
     * 更多信息参阅 [EnvPasswordInfoConfiguration] 接口的描述。
     *
     * @see EnvPasswordInfoConfiguration
     *
     */
    @Serializable
    @SerialName(EnvMd5Text.TYPE)
    public data class EnvMd5Text(
        override val prop: String? = null,
        override val env: String? = null,
    ) : Md5BytesPasswordInfoConfiguration(), EnvPasswordInfoConfiguration {
        
        override fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): ByteArray {
            return Md5Text(configuration.envValue).getPassword(configuration)
        }
        
        public companion object {
            public const val TYPE: String = "env_${Md5Text.TYPE}"
        }
    }

    /**
     * 通过二维码扫码的方式来登陆bot。
     *
     * ```json
     * "passwordInfo": {
     *    "type": "qr_code"
     * }
     * ```
     *
     * @since 3.0.0.0-M6
     *
     * @see BotAuthorization.byQRCode
     */
    @Serializable
    @SerialName(QRCode.TYPE)
    @ExperimentalSimbotApi
    public object QRCode : AuthorizationConfiguration() {
        override fun getBotAuthorization(configuration: MiraiBotVerifyInfoConfiguration): BotAuthorization {
            return BotAuthorization.byQRCode()
        }

        public const val TYPE: String = "qr_code"
    }

    
    public companion object Companion {
        /**
         * 得到一个 [Text]。
         */
        @JvmStatic
        public fun text(text: String): Text = Text(text)
        
        /**
         * 得到一个 [Md5Text]。
         */
        @JvmStatic
        public fun md5(md5: String): Md5Text = Md5Text(md5)
        
        /**
         * 得到一个 [Md5Bytes]。
         */
        @JvmStatic
        public fun md5(md5: ByteArray): Md5Bytes = Md5Bytes(md5)
        
        /**
         * [EnvPasswordInfoConfiguration] 配置信息中继器。
         *
         * ```kotlin
         * PasswordInfoConfiguration.env(prop = "...", env = "...").text()
         * PasswordInfoConfiguration.env(prop = "...", env = "...").md5()
         *
         * PasswordInfoConfiguration.env().prop("prop").env("env").text()
         * PasswordInfoConfiguration.env().env("env").md5()
         * ```
         *
         *
         * 请注意 [EnvPasswordInfoConfiguration] 的限制：[prop] 与 [env] 在最终不可**同时为null**。
         *
         */
        @JvmStatic
        @JvmOverloads
        public fun env(prop: String? = null, env: String? = null): EnvConfigurationRepeater =
            EnvConfigurationRepeater(prop, env)
    
        /**
         * 用于 [env] 使用的中继器。
         */
        @Suppress("MemberVisibilityCanBePrivate")
        public data class EnvConfigurationRepeater(var prop: String?, var env: String?) {
            /**
             * 设置 [prop] 的值，然后返回自身。
             *
             * ```kotlin
             * selector.prop("prop").env("env")
             * ```
             */
            public fun prop(value: String?): EnvConfigurationRepeater = apply { prop = value }
            
            /**
             * 设置 [env] 的值，然后返回自身。
             *
             * ```kotlin
             * selector.env("env").prop("prop")
             * ```
             */
            public fun env(value: String?): EnvConfigurationRepeater = apply { env = value }
            
            /**
             * 根据 [prop] 和 [env] 得到一个 [EnvText]。
             */
            public fun text(): EnvText = EnvText(prop, env)
            
            /**
             * 根据 [prop] 和 [env] 得到一个 [EnvMd5Text]。
             */
            public fun md5(): EnvMd5Text = EnvMd5Text(prop, env)
            
        }
        
    }
    
}

/**
 * 提供密码md5字节数组的密码配置形式。
 */
@Suppress("DEPRECATION")
public sealed class Md5BytesPasswordInfoConfiguration : PasswordInfoConfiguration() {
    @OptIn(InternalApi::class)
    public abstract fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): ByteArray
}

/**
 * 直接提供**明文**密码字符串的密码配置形式。
 */
@Suppress("DEPRECATION")
public sealed class TextPasswordInfoConfiguration : PasswordInfoConfiguration() {
    @OptIn(InternalApi::class)
    public abstract fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): String
}

/**
 * 通过非密码登陆的方式（使用 [BotAuthorization]）。
 *
 * Note: *通过 [BotAuthorization] 是mirai `v2.15.0` 后才有的能力*
 *
 * @since 3.0.0.0-M6
 */
@Suppress("DEPRECATION")
public sealed class AuthorizationConfiguration : PasswordInfoConfiguration() {
    @OptIn(InternalApi::class)
    public abstract fun getBotAuthorization(configuration: MiraiBotVerifyInfoConfiguration): BotAuthorization
}

