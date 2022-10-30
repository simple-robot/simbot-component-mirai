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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import love.forte.simbot.FragileSimbotApi
import love.forte.simbot.Simbot
import love.forte.simbot.SimbotIllegalStateException
import love.forte.simbot.component.mirai.DEFAULT_SIMBOT_MIRAI_DEVICE_INFO_SEED
import love.forte.simbot.component.mirai.SimpleDeviceInfo
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration.Resource.Companion.CLASSPATH_PREFIX
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration.Resource.Companion.LOCAL_FILE_PREFIX
import love.forte.simbot.component.mirai.bot.DeviceInfoConfiguration.Resource.Companion.MULTI_CLASSPATH_PREFIX
import love.forte.simbot.component.mirai.simbotMiraiDeviceInfo
import love.forte.simbot.component.mirai.toDeviceInfo
import love.forte.simbot.logger.LoggerFactory
import love.forte.simbot.logger.logger
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.DeviceInfo.Companion.loadAsDeviceInfo
import java.io.File
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
     * 此方案所使用的 `.json` 文件内容应是直接对 [DeviceInfo] 进行反序列化
     * 的结果，其可能也许会与 [FileBased] 方案的结果略有不同。
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
        
        
        public companion object {
            internal fun readStream(path: String): InputStream? {
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
                get() = Resource::class.java.classLoader ?: Thread.currentThread().contextClassLoader
                ?: ClassLoader.getSystemClassLoader()
            
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
    
    /**
     * 通过指定的本地文件来记录设备信息。
     * ```json
     * {
     *   "type": "file_based",
     *   "file": "device.json",
     *   "fromResource": null
     * }
     * ```
     *
     * 与 [Resource] 不同的是，[FileBased] 是基于 [DeviceInfo.loadAsDeviceInfo] 的，
     * 其最终结果与行为会类似于使用 [BotConfiguration.fileBasedDeviceInfo]。
     *
     *[FileBased] **仅支持** 本地文件，且所需要读取的设备信息文件的格式也与
     * [DeviceInfo] 存在些许不同，它们是存在"版本号"的信息格式，因此 [FileBased] 的所需格式与 [Resource]
     * 的所需格式可能并不通用。
     *
     * 好吧，骗你的。[FileBased] 虽然"仅支持"本地文件，但是它提供了一个可选参数 [fromResource]
     * 来允许在读取文件之前进行检测：当 [file] 处的文件不存在时，会尝试从资源路径中的 [fromResource]
     * 文件复制到 [file] 处。
     * 如果此行为尝试失败，则会输出警告日志，但不会终止流程。
     *
     *
     * 与 [BotConfiguration.fileBasedDeviceInfo] 不同的是，[FileBased] 的属性 [file]
     * **不会** 被限制在 [BotConfiguration.workingDir] 中，而是**直接使用**。
     *
     * [file] 和 [fromResource] 支持占位符替换，例如：
     *
     * ```json
     * {
     *   "type": "file_based",
     *   "file": "$CODE$-device.json",
     *   "fromResource": "$CODE$-device.json",
     * }
     * ```
     *
     * @see Resource
     */
    @Serializable
    @SerialName(FileBased.TYPE)
    public data class FileBased(
        /**
         * 配置文件路径。默认为 `device.json`。
         */
        public val file: String = DEFAULT_FILE,
        /**
         * 当 [file] 处文件不存在时，尝试进行复制
         */
        public val fromResource: String? = null,
    ) : DeviceInfoConfiguration() {
        
        override fun invoke(bot: Bot): DeviceInfo {
            val code = bot.id.toString()
            val targetFile = File(file.replaceCodeMark(code))
            val targetFromResource = fromResource?.replaceCodeMark(code)
            try {
                if (targetFromResource != null && (!targetFile.exists() && targetFile.length() <= 0L)) {
                    // file not exist, try copy from resource
                    val loader = javaClass.classLoader ?: Thread.currentThread().contextClassLoader
                    ?: ClassLoader.getSystemClassLoader()
                    loader.getResourceAsStream(targetFromResource)?.buffered()?.use { resource ->
                        targetFile.outputStream().use(resource::copyTo)
                    } ?: logger.warn(
                        "The file at [{}] is suspected to be null or non-existent, but unable to find resource at [{}]. The copy behavior will not be executed",
                        file,
                        fromResource
                    )
                }
            } catch (anyEx: Throwable) {
                logger.warn(
                    "Unable to copy resource [{}] to target file [{}] when configuring bot {}",
                    targetFromResource,
                    file,
                    bot.id.toString(),
                    anyEx
                )
            }
            
            return targetFile.loadAsDeviceInfo()
        }
        
        public companion object {
            private val logger = LoggerFactory.logger<FileBased>()
            public const val TYPE: String = "file_based"
            public const val DEFAULT_FILE: String = "device.json"
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
     * [Auto.baseDir] 扫描时，解析的**本地文件**格式会优先尝试直接通过 [DeviceInfo] 进行反序列化，
     * 如果失败则会尝试通过 [DeviceInfo.loadAsDeviceInfo] 进行。
     * 而对于 **资源文件** 则只会尝试通过 [DeviceInfo] 反序列化进行加载。
     *
     * 如果无法在上述内容中找到存在的资源，则 [Auto] 会采用 [FileBased] 的行为。
     *
     */
    @Serializable
    @SerialName(Auto.TYPE)
    public data class Auto(
        public val baseDir: String? = null,
        /**
         * 当 [baseDir] 中没有配置内容或无法寻找到目标结果时，通过 [FileBased] 进行加载的文件名。
         */
        public val fileBasedFilename: String = "device.json",
    ) : DeviceInfoConfiguration() {
        
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
                    logger.debug("Finding device info [{}] from local file", path)
                    // local file
                    if (path.exists()) {
                        when {
                            !path.isRegularFile() -> {
                                logger.warn(
                                    "Device info path [{}] for bot(code={}) is exists, but is not a regular file.",
                                    path,
                                    code
                                )
                            }
                            
                            !path.isReadable() -> {
                                logger.warn(
                                    "Device info path [{}] for bot(code={}) is exists, but is not readable.",
                                    path,
                                    code
                                )
                            }
                            
                            else -> {
                                // read.
                                return try {
                                    json.decodeFromString(DeviceInfo.serializer(), path.readText())
                                } catch (decodeException: SerializationException) {
                                    logger.debug(
                                        "Device info path [{}] for bot(code={}) direct deserialization fails: [{}], try loading via loadAsDeviceInfo",
                                        path,
                                        code,
                                        decodeException.localizedMessage
                                    )
                                    try {
                                        path.toFile().loadAsDeviceInfo()
                                    } catch (le: Throwable) {
                                        throw SimbotIllegalStateException("Cannot load device info form path $path").also {
                                            it.addSuppressed(decodeException)
                                            it.addSuppressed(le)
                                        }
                                    }
                                    
                                }
                            }
                        }
                    } else {
                        logger.debug("No device info found on path {}", path)
                    }
                    
                    // resource
                    val resourcePath = path.toString()
                    logger.debug("Finding device info [{}] from resource", resourcePath)
                    classLoader.getResourceAsStream(resourcePath)?.bufferedReader()?.use { reader ->
                        try {
                            json.decodeFromString(DeviceInfo.serializer(), reader.readText())
                        } catch (e: Throwable) {
                            throw SimbotIllegalStateException("Cannot load device info form resource $resourcePath", e)
                        }
                    } ?: apply {
                        logger.debug("No device info found on resource {}", resourcePath)
                    }
                }
                
                logger.debug(
                    "No device info file found in target paths: {}. The device info will be generated (or load) using FileBased(file=$fileBasedFilename).",
                    resolvedPaths
                )
                
            }
            
            return fileBased(fileBasedFilename)(bot)
            
        }
        
        private inline val currentClassLoader: ClassLoader
            get() = javaClass.classLoader
                ?: Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()
        
        public companion object {
            private val logger = LoggerFactory.logger<Auto>()
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
         * 得到一个 [FileBased]。
         */
        @JvmStatic
        @JvmOverloads
        public fun fileBased(file: String = FileBased.DEFAULT_FILE, fromResource: String? = null): FileBased =
            FileBased(file, fromResource)
        
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