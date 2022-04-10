package love.forte.simbot.component.mirai.extra.catcode

import catcode.CatCodeUtil
import catcode.Neko
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import love.forte.simbot.ID
import love.forte.simbot.LoggerFactory
import love.forte.simbot.SimbotIllegalArgumentException
import love.forte.simbot.component.mirai.ID
import love.forte.simbot.component.mirai.internal.InternalApi
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.literal
import love.forte.simbot.message.*
import love.forte.simbot.message.At
import love.forte.simbot.message.AtAll
import love.forte.simbot.message.Face
import love.forte.simbot.message.Message
import love.forte.simbot.resources.toResource
import love.forte.simbot.utils.runWithInterruptible
import love.forte.simbot.utils.toHex
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.math.absoluteValue


private val logger = LoggerFactory.getLogger("love.forte.simbot.component.mirai.extra.catcode.CatCodeDecoders")

/**
 * 猫猫码解析器，用于将一个 [Neko] 解析为 [Messages] 消息实例。
 *
 *
 * @author ForteScarlet
 */
public interface CatCodeDecoder {

    /**
     * 将一个 [Neko] 转化为 [Messages].
     */
    public fun decode(neko: Neko, baseMessageChain: MessageChain? = null): Message.Element<*>
}



internal const val CLASSPATH_HEAD = "classpath:"
internal const val FILE_HEAD = "file:"

internal object TextDecoder : CatCodeDecoder {
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        return neko["text"]?.toText() ?: Text()
    }
}

internal object AtDecoder : CatCodeDecoder {
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        if (neko["all"].toBoolean()) {
            return AtAll
        }

        return neko["code"]?.let { At(it.ID) } ?: throw SimbotIllegalArgumentException("no valid argument 'code'")

    }
}

internal object FaceDecoder : CatCodeDecoder {
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        return neko["id"]?.let { Face(it.ID) } ?: throw SimbotIllegalArgumentException("no valid argument 'id'")
    }
}

internal object PokeDecoder : CatCodeDecoder {
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        val type = neko["type"]?.toInt() ?: return PokeMessage.ChuoYiChuo.asSimbotMessage()
        val id = neko["id"]?.toInt() ?: -1

        val poke = PokeMessage.values
            .find { p -> p.pokeType == type && p.id == id }
            ?: PokeMessage.ChuoYiChuo

        return poke.asSimbotMessage()
    }
}

internal object NudgeDecoder : CatCodeDecoder {
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        return MiraiNudge(neko["target"]?.ID)
    }
}

internal object ImageDecoder : CatCodeDecoder {
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        val id = neko["id"]
        val isFlash = neko["flash"].toBoolean()

        if (id != null) {
            // find from base chain
            val foundImg = baseMessageChain?.find {
                (it is OriginalMiraiImage && it.imageId == id) ||
                        (it is FlashImage && it.image.imageId == id)
            }

            if (foundImg != null) {
                if (foundImg is OriginalMiraiImage) {
                    return MiraiImage.of(foundImg, isFlash)
                }
                if (foundImg is FlashImage) {
                    return MiraiImage.of(foundImg, isFlash)
                }
            }

            // not found
            try {
                return MiraiImage.of(Image(id), isFlash)
            } catch (anyException: Exception) {
                if (logger.isDebugEnabled) {
                    logger.debug("Cannot create [Image] by id $id", anyException)
                } else {
                    logger.warn("Cannot create [Image] by id {}, {}", id, anyException.localizedMessage)
                }
            }
        }

        // no id
        var filePath = neko["file"]
        val name = neko["name"]

        if (filePath != null) {
            if (filePath.startsWith(CLASSPATH_HEAD)) {
                filePath = filePath.substring(CLASSPATH_HEAD.length)
                val classPathUrl: URL? = javaClass.classLoader.getResource(filePath)
                if (classPathUrl != null) {
                    return MiraiSendOnlyImage.of(classPathUrl.toResource(name ?: id ?: classPathUrl.toString()))
                }
            }

            if (filePath.startsWith(FILE_HEAD)) {
                filePath = filePath.substring(FILE_HEAD.length)
            }

            // not 'classpath'
            val path = Path(filePath).takeIf { it.exists() }
            if (path != null) {
                return MiraiSendOnlyImage.of(path.toResource(name ?: id ?: path.toString()))
            }
        }

        // not exists file. find url
        val urlString = filePath?.takeIf { it.startsWith("http") }
            ?: neko["url"]
            ?: throw SimbotIllegalArgumentException("No valid property 'file' or 'url'.")

        return MiraiSendOnlyImage.of(URL(urlString).toResource(name ?: id ?: urlString))
    }
}

internal object VoiceDecoder : CatCodeDecoder {
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        val id = neko["id"]

        if (baseMessageChain != null && id != null) {
            val found = baseMessageChain.find { it is Audio && it.id == id }
            if (found != null) {
                return MiraiAudio.of(found as Audio)
            }
        }

        // file, or url
        var filePath = neko["file"]
        val name = neko["name"]

        if (filePath != null) {
            if (filePath.startsWith(CLASSPATH_HEAD)) {
                filePath = filePath.substring(CLASSPATH_HEAD.length)
                val classPathUrl: URL? = javaClass.classLoader.getResource(filePath)
                if (classPathUrl != null) {
                    return MiraiSendOnlyAudio(classPathUrl.toResource(name ?: id ?: filePath))
                }
            }

            if (filePath.startsWith(FILE_HEAD)) {
                filePath = filePath.substring(FILE_HEAD.length)
            }

            val file = Path(filePath).takeIf { it.exists() }
            if (file != null) {
                return MiraiSendOnlyAudio(file.toResource(name ?: id ?: filePath))
            }
        }

        // find url
        val urlString = filePath?.takeIf { it.startsWith("http") }
            ?: neko["url"]
            ?: throw SimbotIllegalArgumentException("No valid property 'file' or 'url'")

        return MiraiSendOnlyAudio(URL(urlString).toResource(name ?: id ?: urlString))
    }

    private val Audio.id: String
        get() = fileMd5.toHex()
}

internal object FileDecoder : CatCodeDecoder {
    @OptIn(InternalApi::class)
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        // 上传文件路径
        val filePath = neko["file"] ?: neko["url"]
        val formatName = neko["formatName"]
        val fileName = neko["fileName"]

        val basePath = neko["path"]

        // 上传到的路径
        val path: String? = basePath?.let { p ->
            if (p.endsWith('/')) p.substringBeforeLast('/') else p
        }?.let { p ->
            if (p.startsWith("/")) p.substringAfter('/') else p
        }

        if (filePath == null) {
            if (path == null) {
                throw IllegalArgumentException("No valid property 'file' or 'path'")
            } else {
                // mkdir
                return SimpleMiraiSendOnlyComputableMessage { c ->
                    if (c is FileSupported) {
                        val root = c.files.root
                        root.createFolder(path)
                        EmptySingleMessage
                    } else throw IllegalStateException("File only support upload to 'FileSupported' instance, like 'Group'. but '${c::class.java}'")
                }
            }
        }

        if (path == null) {
            throw IllegalArgumentException("Cannot found upload file target 'path'")
        }


        suspend fun AbsoluteFolder.folder(): AbsoluteFolder {
            if (path.isEmpty() || path == "/") return this

            return path.split("/")
                .reversed()
                .foldRight(this) { path, parent ->
                    kotlin.runCatching { parent.createFolder(path) }.getOrElse {
                        throw IllegalStateException("Failed to create folder '$path'", it)
                    }
                }
        }

        // if classpath
        if (filePath.startsWith(CLASSPATH_HEAD)) {
            val substring = filePath.substring(CLASSPATH_HEAD.length)
            val filePath0 = substring.substringBeforeLast('/')
            val fileName0 = fileName ?: substring.substringAfterLast('/')


            val classPathUrl: URL? = javaClass.classLoader.getResource(filePath0)
            if (classPathUrl != null) {
                val fileNeko = CatCodeUtil.getNekoBuilder("file", true)
                    .key("file").value(filePath0)
                    .key("path").value(path)
                    .apply {
                        if (formatName != null) {
                            key("formatName").value(formatName)
                        }
                        if (fileName != null) {
                            key("fileName").value(fileName)
                        }
                    }
                    .build()

                // return Mirai
                return SimpleMiraiSendOnlyComputableMessage { c ->
                    if (c is FileSupported) {
                        val folder = c.files.root.folder()
                        classPathUrl.externalResource(formatName).use { res ->
                            folder.uploadNewFile(fileName0, res).toMessage()
                        }
                    } else throw IllegalStateException("File only support upload to 'FileSupported' instance, like 'Group'. but not '${c::class.java}'")
                }
            } else throw IllegalArgumentException("Cannot resolve classpath file: $filePath0")
        }

        // file ?
        val file: File? = if (filePath.startsWith(FILE_HEAD)) {
            val filePath0 = filePath.substring(FILE_HEAD.length)
            File(filePath0).also {
                if (!it.exists()) {
                    throw NoSuchFileException(it)
                }
            }
        } else {
            File(filePath).takeIf { it.exists() }
        }

        // if file
        return if (file != null) {
            // 存在文件
            val fileName0 = fileName ?: file.name

            SimpleMiraiSendOnlyComputableMessage { c ->
                if (c is FileSupported) {
                    val folder = c.files.root.folder()
                    file.toExternalResource(formatName).use { res ->
                        folder.uploadNewFile(fileName0, res).toMessage()
                    }
                } else throw IllegalStateException("File only support upload to 'FileSupported' instance. but not ${c::class.java}")
            }
        } else {
            // 没有文件，看看有没有url
            val urlString = filePath.takeIf { it.startsWith("http") }
                ?: throw IllegalArgumentException("There is no 'file' or 'url' starts with 'http' in $this")

            SimpleMiraiSendOnlyComputableMessage { c ->
                if (c is FileSupported) {
                    URL(urlString).externalResource(formatName).use {
                        c.files.root.folder().uploadNewFile(fileName ?: "NETWORK_FILE", it).toMessage()
                    }

                } else throw IllegalStateException("Remote file only support upload to 'FileSupported' instance, like 'Group'. but '${c::class.java}'")
            }
        }

    }

    /**
     * 获取 [URL] 的 [ExternalResource]。
     */
    private suspend fun URL.externalResource(formatName: String? = null): ExternalResource {
        val stream = stream()
        return withContext(Dispatchers.IO) {
            stream.use { s -> s.toExternalResource(formatName) }
        }
    }

    /**
     * 获取 [URL] 的输入流。
     */
    private suspend fun URL.stream(): InputStream = runWithInterruptible { openStream() }

}

internal object ShareDecoder : CatCodeDecoder {
    @OptIn(MiraiExperimentalApi::class)
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        // 至少需要一个url
        val url: String =
            neko["url"] ?: throw SimbotIllegalArgumentException("The 'url' could not be found in $this.")
        val title: String = neko["title"] ?: "链接分享"
        val content: String = neko["content"] ?: "链接分享"
        val coverUrl: String? = neko["coverUrl"] ?: neko["image"]

        return MiraiShare(url, title, content, coverUrl)
    }
}

internal object RichDecoder : CatCodeDecoder {
    @OptIn(MiraiExperimentalApi::class)
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        val content: String = neko["content"] ?: "{}"
        // 如果没有serviceId，认为其为lightApp
        val serviceId: Int = neko["serviceId"]?.toInt() ?: return LightApp(content).asSimbotMessage()

        return SimpleServiceMessage(serviceId, content).asSimbotMessage()
    }
}

internal object AppJsonDecoder : CatCodeDecoder {
    @OptIn(MiraiExperimentalApi::class)
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        return LightApp(neko["content"] ?: "{}").asSimbotMessage()
    }
}

internal object DiceDecoder : CatCodeDecoder {
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        val dice: Dice = if (neko["random"] == "true") Dice.random()
        else neko["value"]?.let { v ->
            v.toInt().absoluteValue.let { vInt ->
                if (vInt in 1..6) Dice(vInt)
                else Dice((vInt % 6) + 1)
            }
        } ?: Dice.random()


        return dice.asSimbotMessage()
    }
}

internal object XmlDecoder : CatCodeDecoder {
    @OptIn(MiraiExperimentalApi::class)
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        val xmlCode = neko
        // 解析的参数
        val serviceId = neko["serviceId"]?.toInt() ?: 60

        // 构建xml
        val xml = neko["content"]?.let { content ->
            SimpleServiceMessage(serviceId, content)
        } ?: buildXmlMessage(serviceId) {
            // action
            xmlCode["action"]?.also { this.action = it }
            // 一般为点击这条消息后跳转的链接
            xmlCode["actionData"]?.also { this.actionData = it }
            /*
               摘要, 在官方客户端内消息列表中显示
             */
            xmlCode["brief"]?.also { this.brief = it }
            xmlCode["flag"]?.also { this.flag = it.toInt() }
            xmlCode["url"]?.also { this.url = it }
            // sourceName 好像是名称
            xmlCode["sourceName"]?.also { this.sourceName = it }
            // sourceIconURL 好像是图标
            xmlCode["sourceIconURL"]?.also { this.sourceIconURL = it }

            // builder
//                val keys = xmlCode.params.keys

            item {
                xmlCode["bg"]?.also { this.bg = it.toInt() }
                xmlCode["layout"]?.also { this.layout = it.toInt() }
                // picture(coverUrl: String)
                xmlCode["picture_coverUrl"]?.also { this.picture(it) }
                // summary(text: String, color: String = "#000000")
                xmlCode["summary_text"]?.also {
                    val color: String = xmlCode["summary_color"] ?: "#000000"
                    this.summary(it, color)
                }
                // title(text: String, size: Int = 25, color: String = "#000000")
                xmlCode["title_text"]?.also {
                    val size: Int = xmlCode["title_size"]?.toInt() ?: 25
                    val color: String = xmlCode["title_color"] ?: "#000000"
                    this.title(it, size, color)
                }

            }
        }

        return xml.asSimbotMessage()
    }
}

internal object MusicShareDecoder : CatCodeDecoder {
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        val kindString =
            neko["type"] ?: neko["kind"] ?: throw IllegalArgumentException("No valid property 'type' or 'kind'")
        val musicUrl =
            neko["musicUrl"] ?: neko["audio"]
            ?: throw IllegalArgumentException("No valid property 'musicUrl' or 'audio'")

        // `neteaseCloud`、`qq`、`migu`

        var musicKindDisplay: String
        var musicPictureUrl: String
        var musicJumpUrl: String

        @Suppress("SpellCheckingInspection")
        val musicKind = when (kindString) {
            "neteaseCloud", "NeteaseCloud", "neteaseCloudMusic", "NeteaseCloudMusic", "163", MusicKind.NeteaseCloudMusic.name -> MusicKind.NeteaseCloudMusic.also {
                musicKindDisplay = "网易云音乐"
                musicPictureUrl = "https://s4.music.126.net/style/web2/img/default/default_album.jpg"
                musicJumpUrl = "https://music.163.com/"
            }
            "QQ", "qq", "qqMusic", "QQMusic", MusicKind.QQMusic.name -> MusicKind.QQMusic.also {
                musicKindDisplay = "QQ音乐"
                musicPictureUrl = "https://y.gtimg.cn/mediastyle/app/download/img/logo.png?max_age=2592000"
                musicJumpUrl = "https://y.qq.com/"
            }
            "migu", "Migu", "miguMusic", "MiguMusic", MusicKind.MiguMusic.name -> MusicKind.MiguMusic.also {
                musicKindDisplay = "咪咕音乐"
                musicPictureUrl =
                    "https://cdnmusic.migu.cn/tycms_picture/20/10/294/201020171104983_90x26_2640.png"
                musicJumpUrl = "https://music.migu.cn/"
            }
            "kugou", "Kugou", MusicKind.KugouMusic.name -> MusicKind.KugouMusic.also {
                musicKindDisplay = "酷狗音乐"
                musicPictureUrl =
                    "https://staticssl.kugou.com/public/root/images/logo.png"
                musicJumpUrl = "https://www.kugou.com/"
            }
            "kuwo", "Kuwo", MusicKind.KuwoMusic.name -> MusicKind.KuwoMusic.also {
                musicKindDisplay = "酷我音乐"
                musicPictureUrl =
                    "https://h5static.kuwo.cn/www/kw-www/img/logo.dac7499.png"
                musicJumpUrl = "http://www.kuwo.cn/"
            }
            else -> throw NoSuchElementException("Music kind: $kindString")
        }

        // title
        val title = neko["title"] ?: musicKindDisplay

        // jump url
        val jumpUrl = neko["jumpUrl"] ?: neko["jump"] ?: musicJumpUrl

        // 消息图片url
        val pictureUrl = neko["pictureUrl"] ?: neko["picture"] ?: musicPictureUrl

        val brief = neko["brief"] ?: "[分享]$musicKindDisplay"

        // 消息卡片内容
        val summary = neko["summary"] ?: neko["content"] ?: brief

        return MiraiMusicShare(
            kind = musicKind,
            title = title,
            summary = summary,
            jumpUrl = jumpUrl,
            pictureUrl = pictureUrl,
            musicUrl = musicUrl,
            brief = brief
        )

    }
}

internal object QuoteDecoder : CatCodeDecoder {
    @OptIn(InternalApi::class)
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        return neko["id"]?.let { id ->
            val cacheMsg = baseMessageChain?.findIsInstance<MessageSource>()?.takeIf { it.ID.literal == id }

            if (cacheMsg == null) {
                MiraiQuoteReply(id.ID)
            } else {
                MiraiQuoteReply(cacheMsg)
            }
        } ?: run {
            // 不存在ID，尝试通过messageChain
            baseMessageChain?.quote()?.asSimbotMessage()
                // ?: EmptySingleMessage.simbotMessage // 严格or宽松？
                ?: throw SimbotIllegalArgumentException("No valid property 'id' or quotable baseMessageChain.")
        }
    }
}

internal object UnsupportedDecoder : CatCodeDecoder {
    @OptIn(InternalApi::class)
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {
        val struct: String = neko["struct"] ?: return EmptySingleMessage.simbotMessage
        val structByteArray = struct.hexStringToByteArray()
        return UnsupportedMessage(structByteArray).asSimbotMessage()
    }
}

private fun String.hexStringToByteArray(): ByteArray {
    if (length % 2 != 0) {
        throw IllegalArgumentException("Hex str need % 2 == 0, but length $length in $this")
    }

    val arrayLength = length / 2

    val byteArray = ByteArray(arrayLength)

    val builder = StringBuilder()

    for (i in 0 until arrayLength) {
        val hex0 = builder.append(this[i * 2]).append(this[(i * 2) + 1]).toString()
        byteArray[i] = hex0.toByte(16)
        builder.clear()
    }

    return byteArray
}