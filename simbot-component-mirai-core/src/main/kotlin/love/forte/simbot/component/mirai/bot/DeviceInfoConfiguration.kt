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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import love.forte.simbot.FragileSimbotApi
import love.forte.simbot.LoggerFactory
import love.forte.simbot.Simbot
import love.forte.simbot.component.mirai.DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED
import love.forte.simbot.component.mirai.SimpleDeviceInfo
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration.Resource.Companion.CLASSPATH_PREFIX
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration.Resource.Companion.LOCAL_FILE_PREFIX
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration.Resource.Companion.MULTI_CLASSPATH_PREFIX
import love.forte.simbot.component.mirai.simbotMiraiDeviceInfo
import love.forte.simbot.component.mirai.toDeviceInfo
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.DeviceInfo
import java.io.InputStream
import java.nio.file.StandardOpenOption
import kotlin.io.path.*
import kotlin.random.Random

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
    public data class SimbotRandom @JvmOverloads constructor(public val seed: Long = DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED) :
        DeviceInfoConfiguration() {
        override fun invoke(bot: Bot): DeviceInfo = simbotMiraiDeviceInfo(bot.id, seed)
        
        public companion object {
            public const val TYPE: String = "simbot_random"
            
            @JvmField
            internal val DEFAULT: SimbotRandom = SimbotRandom()
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
    public data class Auto(public val baseDir: String? = null) : DeviceInfoConfiguration() {
        
        override fun invoke(bot: Bot): DeviceInfo {
            val code = bot.id.toString()
            val json = Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
            
            if (baseDir != null) {
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
                        return json.decodeFromString(DeviceInfo.serializer(), reader.readText())
                    } ?: apply {
                        logger.debug("Resource [{}] does not exist", resourcePath)
                    }
                }
                
                logger.debug(
                    "No device info file is found in target paths: {}. The device info will be generated using SimbotRandom.DEFAULT.",
                    resolvedPaths
                )
                
            }
            
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
        
        /**
         * 得到一个 [SimbotRandom]
         */
        @JvmStatic
        @JvmOverloads
        public fun simbotRandom(seed: Long = DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED): SimbotRandom = SimbotRandom(seed)
        
        
        /**
         * 得到一个 [OriginalRandom]
         */
        @JvmStatic
        public fun random(seed: Long): OriginalRandom = OriginalRandom(seed)
        
        
        /**
         * 得到一个 [OriginalRandom]
         */
        @JvmStatic
        public fun random(): OriginalRandom = OriginalRandom()
        
        /**
         * 得到一个 [Resource]，最终使用的列表为 [paths] 的副本。
         *
         * 注意 [Resource] 的约束：[paths] 的数量应当**至少为1**。
         *
         */
        @JvmStatic
        public fun resource(paths: Iterable<String>): Resource = Resource(paths.toList())
        
        
        /**
         * 得到一个 [Resource]，最终使用的列表为 [paths] 的副本。
         *
         * 注意 [Resource] 的约束：[paths] 的数量应当**至少为1**。
         *
         */
        @JvmStatic
        public fun resource(vararg paths: String): Resource = Resource(paths.toList())
        
        /**
         * 得到一个 [JsonObj]
         */
        @JvmStatic
        public fun obj(info: DeviceInfo): JsonObj = JsonObj(info)
        
        /**
         * 得到一个 [SimpleJsonObj]
         *
         * @see SimpleDeviceInfo
         */
        @FragileSimbotApi
        @JvmStatic
        public fun obj(info: SimpleDeviceInfo): SimpleJsonObj = SimpleJsonObj(info)
        
        /**
         * 得到一个 [Auto]
         */
        @JvmStatic
        @JvmOverloads
        public fun auto(baseDir: String? = null): Auto = Auto(baseDir)
        
    }
}