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

package love.forte.simbot.component.mirai.extra.catcode

import catcode.CatCodeUtil
import catcode.CatEncoder
import catcode.Neko
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import love.forte.simbot.ID
import love.forte.simbot.LoggerFactory
import love.forte.simbot.SimbotIllegalArgumentException
import love.forte.simbot.component.mirai.ID
import love.forte.simbot.component.mirai.extra.catcode.AppJsonCatCodeSerializer.encoder
import love.forte.simbot.component.mirai.extra.catcode.XmlCatCodeSerializer.encoder
import love.forte.simbot.component.mirai.internal.InternalApi
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.literal
import love.forte.simbot.message.At
import love.forte.simbot.message.AtAll
import love.forte.simbot.message.Face
import love.forte.simbot.message.Message
import love.forte.simbot.message.Messages
import love.forte.simbot.message.Text
import love.forte.simbot.message.toText
import love.forte.simbot.resources.toResource
import love.forte.simbot.utils.runWithInterruptible
import love.forte.simbot.utils.toHex
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFolder
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
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
import net.mamoe.mirai.message.data.At as MiraiAt
import net.mamoe.mirai.message.data.AtAll as MiraiAtAll
import net.mamoe.mirai.message.data.Face as MiraiFace


private val logger = LoggerFactory.getLogger("love.forte.simbot.component.mirai.extra.catcode.CatCodeSerializer")

/**
 * 猫猫码解析器，用于将一个 [Neko] 解析为 [Messages] 消息实例。
 *
 *
 * @author ForteScarlet
 */
public fun interface CatCodeDecoder {

    /**
     * 将一个 [Neko] 转化为 [Messages].
     */
    public fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*>
}


/**
 * 猫猫码解析器，用于将一个原始的Mirai消息对象解析为 [Neko] 实例。
 *
 *
 * @author ForteScarlet
 */
public fun interface CatCodeEncoder {

    /**
     * 将一个 [SingleMessage] 转化为 [Neko] 实例。
     */
    public fun encode(singleMessage: SingleMessage): Neko

}

private const val CLASSPATH_HEAD = "classpath:"
private const val FILE_HEAD = "file:"


/**
 * Cat Code序列化器。
 * @author ForteScarlet
 */
public abstract class CatCodeSerializer : CatCodeDecoder, CatCodeEncoder {
    /**
     * 猫猫码解码器。
     */
    public abstract val decoder: CatCodeDecoder

    /**
     * 猫猫码编码器。
     */
    public abstract val encoder: CatCodeEncoder


    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> =
        decoder.decode(neko, baseMessageChain)

    override fun encode(singleMessage: SingleMessage): Neko = encoder.encode(singleMessage)

}


/**
 * 针对 [Text] 类型消息的猫猫码序列化器。
 */
public object TextCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ -> neko["text"]?.toText() ?: Text() }
    override val encoder: CatCodeEncoder =
        catCodeEncoder { CatCodeUtil.toNeko("text", false, "text=${CatEncoder.encodeParams(content)}") }

}

/**
 * 针对 [MiraiAt] 类型消息的猫猫码序列化器。
 */
public object AtCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->
        if (neko["all"].toBoolean()) {
            AtAll
        }

        neko["code"]?.let { At(it.ID) } ?: throw SimbotIllegalArgumentException("no valid argument 'code'")
    }
    override val encoder: CatCodeEncoder = catCodeEncoderFor<MiraiAt> { CatCodeUtil.nekoTemplate.at(target) }
}

/**
 * 针对 [MiraiAtAll] 类型消息的猫猫码序列化器。
 */
public object AtAllCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { _, _ -> AtAll }
    override val encoder: CatCodeEncoder = catCodeEncoder { CatCodeUtil.nekoTemplate.atAll() }
}

/**
 * 针对 [Face] 类型消息的猫猫码序列化器。
 */
public object FaceCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->
        neko["id"]?.let { Face(it.ID) } ?: throw SimbotIllegalArgumentException("no valid argument 'id'")
    }
    override val encoder: CatCodeEncoder = catCodeEncoderFor<MiraiFace> { CatCodeUtil.nekoTemplate.face(id.toString()) }
}

/**
 * 针对 [MarketFace] 类型消息的猫猫码序列化器。
 */
public object MarketFaceCatCodeSerializer : CatCodeSerializer() {
    @OptIn(InternalApi::class, MiraiExperimentalApi::class)
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, baseMessageChain ->
        val id = neko["id"] ?: return@CatCodeDecoder EmptySingleMessage.simbotMessage.also {
            logger.warn("Market Face does not support sending. cat code: {}", neko)
        }

        baseMessageChain?.find { it is MarketFace && it.id.toString() == id }?.asSimbotMessage()
            ?: EmptySingleMessage.simbotMessage.also {
                logger.warn("Market Face does not support sending. cat code: {}", neko)
            }
    }

    @OptIn(MiraiExperimentalApi::class)
    override val encoder: CatCodeEncoder = catCodeEncoderFor<MarketFace> {
        CatCodeUtil.getNekoBuilder("marketFace", true)
            .key("id").value(id)
            .key("name").value(name)
            .build()
    }
}

/**
 * 针对 [VipFace] 类型消息的猫猫码序列化器。
 */
public object VipFaceCatCodeSerializer : CatCodeSerializer() {
    @OptIn(InternalApi::class, MiraiExperimentalApi::class)
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->
        logger.warn("Vip Face does not support sending. cat code: {}", neko)
        EmptySingleMessage.simbotMessage
    }
    override val encoder: CatCodeEncoder = catCodeEncoderFor<VipFace> {
        CatCodeUtil.getNekoBuilder("vipFace", true)
            .key("kindId").value(this.kind.id)
            .key("kindName").value(this.kind.name)
            .key("count").value(this.count)
            .build()
    }
}

/**
 * 针对 [PokeMessage] 类型消息的猫猫码序列化器。
 */
public object PokeCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->
        val type = neko["type"]?.toInt() ?: return@CatCodeDecoder PokeMessage.ChuoYiChuo.asSimbotMessage()
        val id = neko["id"]?.toInt() ?: -1

        val poke = PokeMessage.values
            .find { p -> p.pokeType == type && p.id == id }
            ?: PokeMessage.ChuoYiChuo

        poke.asSimbotMessage()
    }
    override val encoder: CatCodeEncoder = catCodeEncoderFor<PokeMessage> {
        CatCodeUtil.getNekoBuilder("poke", false)
            .key("type").value(pokeType)
            .key("id").value(id)
            .build()
    }
}

/**
 * 针对 [Nudge] 类型消息的猫猫码序列化器。
 */
public object NudgeCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ -> MiraiNudge(neko["target"]?.ID) }

    @Deprecated("Normally should not be used")
    override val encoder: CatCodeEncoder = catCodeEncoder { CatCodeUtil.toNeko("nudge") } // 不可能触发
}

/**
 * 针对 [Image] 类型消息的猫猫码序列化器。
 */
public object ImageCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, baseMessageChain ->
        val id = neko["id"]
        val isFlash = neko["flash"].toBoolean()

        if (id != null) {
            // find from base chain
            val foundImg = baseMessageChain?.find {
                (it is Image && it.imageId == id) ||
                        (it is FlashImage && it.image.imageId == id)
            }

            if (foundImg != null) {
                if (foundImg is Image) {
                    return@CatCodeDecoder MiraiImage.of(foundImg, isFlash)
                }
                if (foundImg is FlashImage) {
                    return@CatCodeDecoder MiraiImage.of(foundImg, isFlash)
                }
            }

            // not found
            try {
                return@CatCodeDecoder MiraiImage.of(Image(id), isFlash)
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
                    return@CatCodeDecoder MiraiSendOnlyImage.of(
                        classPathUrl.toResource(
                            name ?: id ?: classPathUrl.toString()
                        )
                    )
                }
            }

            if (filePath.startsWith(FILE_HEAD)) {
                filePath = filePath.substring(FILE_HEAD.length)
            }

            // not 'classpath'
            val path = Path(filePath).takeIf { it.exists() }
            if (path != null) {
                return@CatCodeDecoder MiraiSendOnlyImage.of(path.toResource(name ?: id ?: path.toString()))
            }
        }

        // not exists file. find url
        val urlString = filePath?.takeIf { it.startsWith("http") }
            ?: neko["url"]
            ?: throw SimbotIllegalArgumentException("No valid property 'file' or 'url'.")


        MiraiSendOnlyImage.of(URL(urlString).toResource(name ?: id ?: urlString))
    }
    override val encoder: CatCodeEncoder = catCodeEncoder {
        val image: Image
        val isFlash: Boolean
        when (this) {
            is Image -> {
                image = this
                isFlash = false
            }
            is FlashImage -> {
                image = this.image
                isFlash = true
            }
            else -> {
                throw SimbotIllegalArgumentException("Must be type of Image or FlashImage.")
            }
        }

        CatCodeUtil.getLazyNekoBuilder("image", true)
            .key("id").value(image.imageId)
            .key("height").value(image.height)
            .key("width").value(image.width)
            .key("size").value(image.size)
            .key("imageType").value(image.imageType)
            .key("isEmoji").value(image.isEmoji)
            .key("url").value { runBlocking { image.queryUrl() } }
            .key("flash").value(isFlash)
            .build()
    }
}

/**
 * 针对 [Audio] 类型消息的猫猫码序列化器。
 */
public object AudioCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, baseMessageChain ->
        val id = neko["id"]

        if (baseMessageChain != null && id != null) {
            val found = baseMessageChain.find { it is Audio && it.id == id }
            if (found != null) {
                return@CatCodeDecoder MiraiAudio.of(found as Audio)
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
                    return@CatCodeDecoder MiraiSendOnlyAudio(classPathUrl.toResource(name ?: id ?: filePath))
                }
            }

            if (filePath.startsWith(FILE_HEAD)) {
                filePath = filePath.substring(FILE_HEAD.length)
            }

            val file = Path(filePath).takeIf { it.exists() }
            if (file != null) {
                return@CatCodeDecoder MiraiSendOnlyAudio(file.toResource(name ?: id ?: filePath))
            }
        }

        // find url
        val urlString = filePath?.takeIf { it.startsWith("http") }
            ?: neko["url"]
            ?: throw SimbotIllegalArgumentException("No valid property 'file' or 'url'")


        MiraiSendOnlyAudio(URL(urlString).toResource(name ?: id ?: urlString))
    }

    override val encoder: CatCodeEncoder = catCodeEncoderFor<Audio> {
        CatCodeUtil.getLazyNekoBuilder("voice", true)
            .key("id").value { id }
            .key("name").value(filename)
            .key("size").value(fileSize).apply {
                if (this is OnlineAudio) {
                    key("url").value { this.urlForDownload }
                }
            }
            .key("md5").value { this.fileMd5.toHex() }
            .build()
    }

    private val Audio.id: String
        get() = fileMd5.toHex()
}

/**
 * 针对 [FileMessage] 类型消息的猫猫码序列化器。
 */
public object FileCatCodeSerializer : CatCodeSerializer() {
    @OptIn(InternalApi::class)
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->
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
                return@CatCodeDecoder SimpleMiraiSendOnlyComputableMessage { c ->
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

                // return Mirai
                return@CatCodeDecoder SimpleMiraiSendOnlyComputableMessage { c ->
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
        return@CatCodeDecoder if (file != null) {
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
    override val encoder: CatCodeEncoder = catCodeEncoderFor<FileMessage> {
        CatCodeUtil.getNekoBuilder("file", true)
            .key("id").value(id)
            .key("internalId").value(internalId)
            .key("name").value(name)
            .key("size").value(size)
            .build()
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

/**
 * 针对 `Share` 类型消息的猫猫码序列化器。
 */
public object ShareCatCodeSerializer : CatCodeSerializer() {
    @OptIn(MiraiExperimentalApi::class)
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->
        // 至少需要一个url
        val url: String =
            neko["url"] ?: throw SimbotIllegalArgumentException("The 'url' could not be found in $this.")
        val title: String = neko["title"] ?: "链接分享"
        val content: String = neko["content"] ?: "链接分享"
        val coverUrl: String? = neko["coverUrl"] ?: neko["image"]

        MiraiShare(url, title, content, coverUrl)
    }

    @Deprecated("Normally should not be used")
    override val encoder: CatCodeEncoder = catCodeEncoder { CatCodeUtil.toNeko("share") } // 不会使用

}

/**
 * 针对 [RichMessage] 类型消息的猫猫码序列化器。
 */
public object RichCatCodeSerializer : CatCodeSerializer() {
    @OptIn(MiraiExperimentalApi::class)
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->
        val content: String = neko["content"] ?: "{}"
        // 如果没有serviceId，认为其为lightApp
        val serviceId: Int = neko["serviceId"]?.toInt() ?: return@CatCodeDecoder LightApp(content).asSimbotMessage()

        SimpleServiceMessage(serviceId, content).asSimbotMessage()
    }
    override val encoder: CatCodeEncoder = catCodeEncoderFor<RichMessage> {
        CatCodeUtil.getNekoBuilder("rich", true)
            .key("content").value(content).let {
                if (this is ServiceMessage) {
                    it.key("serviceId").value(this.serviceId)
                } else it
            }.build()
    }

}

/**
 * 针对 `app` 、 `json` 类型消息的猫猫码序列化器。
 *
 * [encoder] 等同于 [RichCatCodeSerializer.encoder].
 */
public object AppJsonCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder =
        CatCodeDecoder { neko, _ -> LightApp(neko["content"] ?: "{}").asSimbotMessage() }
    override val encoder: CatCodeEncoder get() = RichCatCodeSerializer.encoder
}

/**
 * 针对 [Dice] 类型消息的猫猫码序列化器。
 * 是 [MarketFace] 的子类型，应该在其之前判断。
 */
public object DiceCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->
        val dice: Dice = if (neko["random"] == "true") Dice.random()
        else neko["value"]?.let { v ->
            v.toInt().absoluteValue.let { vInt ->
                if (vInt in 1..6) Dice(vInt)
                else Dice((vInt % 6) + 1)
            }
        } ?: Dice.random()


        dice.asSimbotMessage()
    }
    override val encoder: CatCodeEncoder = catCodeEncoderFor<Dice> {
        CatCodeUtil.getNekoBuilder("dice", false)
            .key("value").value(value)
            .key("random").value(true)
            .build()
    }
}

/**
 * 针对 `Xml` 类型消息的猫猫码序列化器。
 *
 * [encoder] 等同于 [RichCatCodeSerializer.encoder]。
 */
public object XmlCatCodeSerializer : CatCodeSerializer() {
    @OptIn(MiraiExperimentalApi::class)
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->
        // 解析的参数
        val serviceId = neko["serviceId"]?.toInt() ?: 60

        // 构建xml
        val xml = neko["content"]?.let { content ->
            SimpleServiceMessage(serviceId, content)
        } ?: buildXmlMessage(serviceId) {
            // action
            neko["action"]?.also { this.action = it }
            // 一般为点击这条消息后跳转的链接
            neko["actionData"]?.also { this.actionData = it }
            /*
               摘要, 在官方客户端内消息列表中显示
             */
            neko["brief"]?.also { this.brief = it }
            neko["flag"]?.also { this.flag = it.toInt() }
            neko["url"]?.also { this.url = it }
            // sourceName 好像是名称
            neko["sourceName"]?.also { this.sourceName = it }
            // sourceIconURL 好像是图标
            neko["sourceIconURL"]?.also { this.sourceIconURL = it }

            // builder
//                val keys = xmlCode.params.keys

            item {
                neko["bg"]?.also { this.bg = it.toInt() }
                neko["layout"]?.also { this.layout = it.toInt() }
                // picture(coverUrl: String)
                neko["picture_coverUrl"]?.also { this.picture(it) }
                // summary(text: String, color: String = "#000000")
                neko["summary_text"]?.also {
                    val color: String = neko["summary_color"] ?: "#000000"
                    this.summary(it, color)
                }
                // title(text: String, size: Int = 25, color: String = "#000000")
                neko["title_text"]?.also {
                    val size: Int = neko["title_size"]?.toInt() ?: 25
                    val color: String = neko["title_color"] ?: "#000000"
                    this.title(it, size, color)
                }

            }
        }

        xml.asSimbotMessage()
    }
    override val encoder: CatCodeEncoder get() = RichCatCodeSerializer.encoder
}

/**
 * 针对 [MusicShare] 类型消息的猫猫码序列化器。
 */
public object MusicShareCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->

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
                    "https://www.kugou.com/common/images/logo.png"
                musicJumpUrl = "https://www.kugou.com/"
            }
            "kuwo", "Kuwo", MusicKind.KuwoMusic.name -> MusicKind.KuwoMusic.also {
                musicKindDisplay = "酷我音乐"
                musicPictureUrl =
                    "https://h5static.kuwo.cn/www/kw-www/img/logo.dac7499.png"
                musicJumpUrl = "https://www.kuwo.cn/"
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


        MiraiMusicShare(
            kind = musicKind,
            title = title,
            summary = summary,
            jumpUrl = jumpUrl,
            pictureUrl = pictureUrl,
            musicUrl = musicUrl,
            brief = brief
        )

    }
    override val encoder: CatCodeEncoder = catCodeEncoderFor<MusicShare> {
        // 音乐分享
        CatCodeUtil.getNekoBuilder("music", true)
            .key("kind").value(kind.name)
            .key("musicUrl").value(musicUrl)
            .key("title").value(title)
            .key("jumpUrl").value(jumpUrl)
            .key("pictureUrl").value(pictureUrl)
            .key("summary").value(summary)
            .key("brief").value(brief)
            .build()
    }
}

/**
 * 针对 [QuoteReply] 类型消息的猫猫码序列化器。
 */
public object QuoteCatCodeSerializer : CatCodeSerializer() {
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, baseMessageChain ->
        neko["id"]?.let { id ->
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
    override val encoder: CatCodeEncoder = catCodeEncoderFor<QuoteReply> {
        val id = source.id
        CatCodeUtil.getLazyNekoBuilder("quote", true)
            .key("id").value(id)
            .build()
    }

    private val MessageSource.id: String
        get() {
            val fromIdStr = fromId.str
            val targetIdStr = targetId.str
            val kindStr = kind.ordinal.str
            val idsStr = ids.str
            val internalIdsStr = internalIds.str
            val timeStr = time.str

            return arrayOf(fromIdStr, targetIdStr, kindStr, idsStr, internalIdsStr, timeStr).joinToString("-")
        }

    private inline val Int.str get() = Integer.toHexString(this)
    private inline val Long.str get() = java.lang.Long.toHexString(this)
    private inline val IntArray.str: String get() = joinToString(":") { i -> i.str }

}

/**
 * 针对 [UnsupportedMessage] 类型消息的猫猫码序列化器。
 */
public object UnsupportedCatCodeSerializer : CatCodeSerializer() {
    @OptIn(InternalApi::class)
    override val decoder: CatCodeDecoder = CatCodeDecoder { neko, _ ->
        val struct: String = neko["struct"] ?: return@CatCodeDecoder EmptySingleMessage.simbotMessage
        val structByteArray = struct.hexStringToByteArray()
        UnsupportedMessage(structByteArray).asSimbotMessage()
    }
    override val encoder: CatCodeEncoder = catCodeEncoderFor<UnsupportedMessage> {
        CatCodeUtil.getNekoBuilder("unsupported", true)
            .key("struct").value(struct.toHex())
            .build()
    }
}


internal fun String.hexStringToByteArray(): ByteArray {
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


private inline fun <reified M : SingleMessage> catCodeEncoderFor(crossinline encoder: M.() -> Neko): CatCodeEncoder =
    CatCodeEncoder {
        if (it is M) {
            encoder(it)
        } else {
            throw SimbotIllegalArgumentException("The encoding target must be of type ${M::class}.")
        }
    }

private inline fun catCodeEncoder(crossinline encoder: SingleMessage.() -> Neko): CatCodeEncoder =
    CatCodeEncoder { encoder(it) }