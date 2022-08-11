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
import love.forte.simbot.bot.BotVerifyInfo
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration.Companion.CODE_MARK
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration.Resource.Companion.CLASSPATH_PREFIX
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration.Resource.Companion.LOCAL_FILE_PREFIX
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration.Resource.Companion.MULTI_CLASSPATH_PREFIX
import love.forte.simbot.component.mirai.bot.PasswordInfoConfiguration.EnvPasswordInfo.Companion.CODE_MARK
import love.forte.simbot.component.mirai.internal.InternalApi
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.LoggerAdapters.asMiraiLogger
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.io.InputStream
import java.nio.file.StandardOpenOption
import kotlin.io.path.*
import kotlin.random.Random


// region PasswordInfo Configuration

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
 */
@Serializable
@OptIn(InternalApi::class)
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
    ) : TextPasswordInfoConfiguration(), EnvPasswordInfo {
        
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
    ) : Md5BytesPasswordInfoConfiguration(), EnvPasswordInfo {
        
        override fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): ByteArray {
            return Md5Text(configuration.envValue).getPassword(configuration)
        }
        
        public companion object {
            public const val TYPE: String = "env_${Md5Text.TYPE}"
        }
    }
}


/**
 * 直接提供**明文**密码字符串的密码配置形式。
 */
public sealed class TextPasswordInfoConfiguration : PasswordInfoConfiguration() {
    @OptIn(InternalApi::class)
    public abstract fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): String
}


/**
 * 提供密码md5字节数组的密码配置形式。
 */
public sealed class Md5BytesPasswordInfoConfiguration : PasswordInfoConfiguration() {
    @OptIn(InternalApi::class)
    public abstract fun getPassword(configuration: MiraiBotVerifyInfoConfiguration): ByteArray
}
// endregion


// region DeviceInfo Configuration

/**
 * 通过 [MiraiBotVerifyInfoConfiguration.Config.deviceInfoConfiguration] 来提供多种形式的设备信息配置。
 */
@Serializable
public sealed class DeviceInfoConfiguration : (Bot) -> DeviceInfo {
    protected fun String.replaceCodeMark(code: String): String = replace(CODE_MARK, code)
    
    /**
     * 使用simbot通过的基础设备模板进行一定范围内的伪随机。
     *
     * ```json
     * {
     *   "deviceInfo": {
     *      "type": "simbot_random",
     *      "seed": 114
     *   }
     * }
     * ```
     *
     * @see simbotMiraiDeviceInfo
     */
    @Serializable
    @SerialName(SimbotRandom.TYPE)
    public data class SimbotRandom(public val seed: Long = DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED) :
        DeviceInfoConfiguration() {
        override fun invoke(bot: Bot): DeviceInfo = simbotMiraiDeviceInfo(bot.id, seed)
        
        public companion object {
            public const val TYPE: String = "simbot_random"
            
            @JvmField
            public val DEFAULT: SimbotRandom = SimbotRandom()
        }
    }
    
    
    /**
     * 直接使用mirai原本的随机方案进行随机。
     *
     * ```json
     * {
     *   "deviceInfo": {
     *      "type": "random",
     *      "seed": 514
     *   }
     * }
     * ```
     *
     * 其中，当 [seed] 不为 null 时，将会通过 [seed] 构建一个具体的 [Random] 使用，
     * 反之如果 [seed] 为 null，则直接使用 [Random.Default]。
     *
     * 默认情况下 [seed] 为null。
     *
     * @see DeviceInfo.random
     */
    @Serializable
    @SerialName(OriginalRandom.TYPE)
    public data class OriginalRandom(public val seed: Long? = null) : DeviceInfoConfiguration() {
        @Transient
        private val random = seed?.let(::Random) ?: Random
        override fun invoke(bot: Bot): DeviceInfo = DeviceInfo.random(random)
        
        public companion object {
            public const val TYPE: String = "random"
        }
    }
    
    
    /**
     * 通过本地文件或资源文件所得到的结果。
     * ```json
     * {
     *   "deviceInfo": {
     *      "type": "resource",
     *      "paths": ["foo/bar/device.json"]
     *   }
     * }
     * ```
     *
     * ## 占位符替换
     * [paths] 属性支持占位符替换。参考 [CODE_MARK]，例如：
     * ```json
     * {
     *   "deviceInfo": {
     *      "type": "resource",
     *      "paths": ["foo/bar/device-$CODE$.json"]
     *   }
     * }
     * ```
     * 当目标bot的id为 `123` 时, `paths` 最终会变为 `["foo/bar/device-123.json"]`
     *
     * 其他细节参见 [paths] 属性说明。
     *
     */
    @Serializable
    @SerialName(Resource.TYPE)
    public data class Resource(
        /**
         * 资源文件的路径，应当至少存在一个元素。
         *
         * ## 占位符替换
         * [paths] 属性元素支持占位符替换。参考 [CODE_MARK]。
         *
         * ## 前缀解析
         * [paths] 作为路径参数，支持部分前缀的解析：
         * - [LOCAL_FILE_PREFIX]
         * - [CLASSPATH_PREFIX]
         * - [MULTI_CLASSPATH_PREFIX]
         *
         * 当 [paths] 中的元素不存在前缀时，默认情况下会先尝试将其视作本地文件，而后再尝试作为资源文件（非深层）。
         *
         * [paths] 中元素应当为具体的文件，不可以指定目录。
         *
         */
        public val paths: List<String>,
    ) : DeviceInfoConfiguration() {
        
        init {
            Simbot.require(paths.isNotEmpty()) { "[deviceInfo.path] must not be empty." }
        }
        
        override fun invoke(bot: Bot): DeviceInfo {
            val json = Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
            val resolvedPath = paths.map { p -> p.replaceCodeMark(bot.id.toString()) }
            for (p in resolvedPath) {
                readStream(p)?.use { inp ->
                    return json.decodeFromString(DeviceInfo.serializer(), inp.bufferedReader().readText())
                }
            }
            
            throw NoSuchElementException("valid deviceInfo resource path in $resolvedPath")
        }
        
        private fun readStream(path: String): InputStream? {
            return when {
                path.startsWith(LOCAL_FILE_PREFIX) -> {
                    readLocalFile(path.substringAfter(LOCAL_FILE_PREFIX))
                }
                
                path.startsWith(CLASSPATH_PREFIX) -> {
                    readResource(path.substringAfter(CLASSPATH_PREFIX))
                }
                
                path.startsWith(MULTI_CLASSPATH_PREFIX) -> {
                    readResourceMulti(path.substringAfter(MULTI_CLASSPATH_PREFIX))
                }
                
                else -> {
                    readLocalFile(path) ?: readResource(path)
                }
            }
        }
        
        private fun readLocalFile(filePath: String): InputStream? {
            val path = Path(filePath)
            if (!path.exists()) return null
            
            return path.inputStream(StandardOpenOption.READ).buffered()
        }
        
        private fun readResource(resourcePath: String): InputStream? {
            return currentLoader.getResourceAsStream(resourcePath)?.buffered()
        }
        
        private fun readResourceMulti(resourcePath: String): InputStream? {
            val resources = currentLoader.getResources(resourcePath)
            
            if (!resources.hasMoreElements()) return null
            return resources.nextElement().openStream().buffered()
        }
        
        private val currentLoader: ClassLoader
            get() = javaClass.classLoader ?: Thread.currentThread().contextClassLoader
            ?: ClassLoader.getSystemClassLoader()
        
        
        public companion object {
            public const val TYPE: String = "resource"
            
            /**
             * 仅查找本地文件
             */
            public const val LOCAL_FILE_PREFIX: String = "file:"
            
            /**
             * 仅查找资源目录
             */
            public const val CLASSPATH_PREFIX: String = "classpath:"
            
            /**
             * 仅查找多层级的资源目录
             */
            public const val MULTI_CLASSPATH_PREFIX: String = "classpath*:"
        }
    }
    
    
    // deviceInfo json object
    
    /**
     * 直接使用 [DeviceInfo] 的序列化json对象最为目标值。
     *
     * ```json
     * {
     *   "deviceInfo": {
     *      "type": "object",
     *      "object": {
     *         "....": []
     *      }
     *   }
     * }
     * ```
     * 对于 `object` 参数的json结构请自行参考 [DeviceInfo] 或相关json文件内容，此处不做示例。
     *
     */
    @Serializable
    @SerialName(JsonObj.TYPE)
    public data class JsonObj(@SerialName("object") public val info: DeviceInfo) : DeviceInfoConfiguration() {
        
        override fun invoke(bot: Bot): DeviceInfo = info
        
        public companion object {
            public const val TYPE: String = "object"
        }
    }
    
    /**
     * 直接使用 [SimpleDeviceInfo] 的序列化json对象最为目标值。
     *
     * ```json
     * {
     *   "deviceInfo": {
     *      "type": "simple_object",
     *      "object": {
     *         "....": ""
     *      }
     *   }
     * }
     * ```
     * 对于 `object` 参数的json结构请自行参考 [SimpleDeviceInfo] 或相关json文件内容，此处不做示例。
     *
     * ## FragileSimbotApi
     * 注意！[SimpleDeviceInfo] 被标记为 [FragileSimbotApi]，因为它不能保证对 [DeviceInfo] 的100%兼容。
     * 因此请不要过于依赖它。
     *
     * @see SimpleDeviceInfo
     */
    @OptIn(FragileSimbotApi::class)
    @Serializable
    @SerialName(SimpleJsonObj.TYPE)
    public class SimpleJsonObj(@SerialName("object") public val simpleInfo: SimpleDeviceInfo) :
        DeviceInfoConfiguration() {
        
        @Transient
        private val info = simpleInfo.toDeviceInfo()
        
        override fun invoke(bot: Bot): DeviceInfo = info
        
        public companion object {
            public const val TYPE: String = "simple_object"
        }
    }
    
    
    /**
     * 自动寻找或配置deviceInfo。
     *
     * [Auto] 首先会像 [Resource] 一样，去寻找几个指定的资源文件：
     * - `$BASE_DIR$device-$CODE$.json`
     * - `$BASE_DIR$device.json`
     *
     * 其中，`$BASE_DIR$` 为配置属性 [baseDir]，默认为空，即根目录。
     * 最终得到的路径会先尝试从本地文件中寻找，而后会尝试在资源目录中寻找。
     *
     * 寻找顺序为：
     * 1. 本地文件: `$BASE_DIR$device-$CODE$.json`
     * 2. 资源文件: `$BASE_DIR$device-$CODE$.json`
     * 3. 本地文件: `$BASE_DIR$device.json`
     * 4. 资源文件: `$BASE_DIR$device.json`
     *
     * 如下示例中，
     *
     * ```json
     * {
     *   "deviceInfo": {
     *      "type": "auto",
     *      "baseDir": "devices"
     *   }
     * }
     * ```
     *
     * 假设当前bot.id为 123456，则最终寻找的目标路径为：
     *
     * - `devices/device-123456.json`
     * - `devices/device.json`
     *
     *
     * [baseDir] 允许使用 `$CODE$` 占位符进行替换，例如：
     *
     * ```json
     * {
     *   "deviceInfo": {
     *      "type": "auto",
     *      "baseDir": "devices-$CODE$"
     *   }
     * }
     * ```
     *
     * 假设当前bot.id为 `123456`, 则上述配置中的的 `baseDir` 最终会被替换为 `devices-123456`，
     * 并最终会去寻找如下目标：
     * - `devices-123456/device-123456.json`
     * - `devices-123456/device.json`
     *
     * 如果无法在上述内容中找到存在的资源，则 [Auto] 会采用与 [SimbotRandom] 一致的行为。
     *
     */
    @Serializable
    @SerialName(Auto.TYPE)
    public data class Auto(public val baseDir: String = "") : DeviceInfoConfiguration() {
        
        override fun invoke(bot: Bot): DeviceInfo {
            val code = bot.id.toString()
            val json = Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
            val formattedBaseDir = baseDir.replaceCodeMark(code)
            val classLoader = currentClassLoader
            val resolvedPaths = TARGETS.map { Path(formattedBaseDir, it.replaceCodeMark(code)) }
            for (path in resolvedPaths) {
                logger.debug("Find device info [{}] from local file", path)
                // local file
                if (path.exists()) {
                    if (!path.isRegularFile()) {
                        logger.debug("Path [{}] is exists, but is not a regular file.", path)
                    } else {
                        // read, and without try
                        return json.decodeFromString(DeviceInfo.serializer(), path.readText())
                    }
                } else {
                    logger.debug("Path [{}] does not exist", path)
                }
                
                // resource
                val resourcePath = path.toString()
                logger.debug("Find device info [{}] from resource", resourcePath)
                classLoader.getResourceAsStream(resourcePath)?.bufferedReader()?.use { reader ->
                    return json.decodeFromString(DeviceInfo.serializer(), path.readText())
                } ?: apply {
                    logger.debug("Resource [{}] does not exist", resourcePath)
                }
            }
            
            logger.debug("No device info file is found in target paths: {}. The device info will be generated using SimbotRandom.DEFAULT.", resolvedPaths)
            
            return SimbotRandom.DEFAULT(bot)
            
        }
        
        private inline val currentClassLoader: ClassLoader
            get() = javaClass.classLoader
                ?: Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()
        
        public companion object {
            private val logger = LoggerFactory.getLogger<Auto>()
            private val TARGETS: Array<String> = arrayOf(
                "device-$CODE_MARK.json",
                "device.json"
            )
            public const val TYPE: String = "auto"
        }
    }
    
    
    public companion object {
        /**
         * 用于代表当前需要进行配置的bot的账号占位符。
         * 当可以进行占位符替换时，会使用 [Bot.id] 替换字符串中的占位符。
         */
        public const val CODE_MARK: String = "\$CODE$"
    }
}
// endregion


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
    val passwordInfo: PasswordInfoConfiguration? = null,
    
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
    
        /**
         * Mirai配置中的工作目录。
         *
         * @see BotConfiguration.workingDir
         */
        @Serializable(FileSerializer::class) val workingDir: File = BotConfiguration.Default.workingDir,
    
        /**
         * 同mirai原生配置 [BotConfiguration.heartbeatPeriodMillis]。
         */
        val heartbeatPeriodMillis: Long = BotConfiguration.Default.heartbeatPeriodMillis,
        /**
         * 同mirai原生配置 [BotConfiguration.statHeartbeatPeriodMillis]。
         */
        val statHeartbeatPeriodMillis: Long = BotConfiguration.Default.statHeartbeatPeriodMillis,
        /**
         * 同mirai原生配置 [BotConfiguration.heartbeatTimeoutMillis]。
         */
        val heartbeatTimeoutMillis: Long = BotConfiguration.Default.heartbeatTimeoutMillis,
    
        /**
         * 同mirai原生配置 [BotConfiguration.heartbeatStrategy]。
         */
        @Serializable(HeartbeatStrategySerializer::class) val heartbeatStrategy: BotConfiguration.HeartbeatStrategy = BotConfiguration.Default.heartbeatStrategy,
    
        /**
         * 同mirai原生配置 [BotConfiguration.reconnectionRetryTimes]。
         */
        val reconnectionRetryTimes: Int = BotConfiguration.Default.reconnectionRetryTimes,
    
        /**
         * 同mirai原生配置 [BotConfiguration.autoReconnectOnForceOffline]。
         */
        val autoReconnectOnForceOffline: Boolean = BotConfiguration.Default.autoReconnectOnForceOffline,
    
        /**
         * 同mirai原生配置 [BotConfiguration.protocol]。
         */
        @Serializable(MiraiProtocolSerializer::class) val protocol: BotConfiguration.MiraiProtocol = BotConfiguration.Default.protocol,
    
        /**
         * 同mirai原生配置 [BotConfiguration.highwayUploadCoroutineCount]。
         */
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
    
        /**
         * 配置设备信息。
         *
         * @see DeviceInfoConfiguration
         */
        @SerialName("deviceInfo")
        val deviceInfoConfiguration: DeviceInfoConfiguration = DeviceInfoConfiguration.Auto(),
    
        /**
         * 是否不输出网络日志。当为true时等同于使用了 [BotConfiguration.noNetworkLog]
         */
        val noNetworkLog: Boolean = false,
        /**
         * 是否不输出Bot日志。当为true时等同于使用了 [BotConfiguration.noBotLog]
         */
        val noBotLog: Boolean = false,
        /**
         * 同原生配置 [BotConfiguration.isShowingVerboseEventLog]
         */
        val isShowingVerboseEventLog: Boolean = BotConfiguration.Default.isShowingVerboseEventLog,
    
        /**
         * 同原生配置 [BotConfiguration.cacheDir]
         */
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
        
        @Transient
        private val deviceInfo: (Bot) -> DeviceInfo = deviceInfoConfiguration.also {
            deviceInfoCompatibleCheck()
        }
        
        private fun deviceInfoCompatibleCheck() {
            // deviceInfoJson
            if (deviceInfoJson != null) {
                val illegalProp = "deviceInfoJson"
                throw SimbotIllegalStateException(
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
            }
            
            // simpleDeviceInfoJson
            if (simpleDeviceInfoJson != null) {
                val illegalProp = "simpleDeviceInfoJson"
                throw SimbotIllegalStateException(
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
            }
            
            // deviceInfoFile
            if (deviceInfoFile != null) {
                val illegalProp = "deviceInfoFile"
                throw SimbotIllegalStateException(
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