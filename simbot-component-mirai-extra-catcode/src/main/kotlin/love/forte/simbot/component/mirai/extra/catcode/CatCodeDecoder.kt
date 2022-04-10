package love.forte.simbot.component.mirai.extra.catcode

import catcode.Neko
import love.forte.simbot.ID
import love.forte.simbot.SimbotIllegalArgumentException
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.message.*
import love.forte.simbot.resources.toResource
import net.mamoe.mirai.message.data.FlashImage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PokeMessage
import java.net.URL
import kotlin.io.path.Path
import kotlin.io.path.exists


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
                // TODO log
            }
        }

        // no id
        val filePath = neko["file"]
        val name = neko["name"]

        if (filePath != null) {
            if (filePath.startsWith(CLASSPATH_HEAD)) {
                val filePath0 = filePath.substring(CLASSPATH_HEAD.length)
                val classPathUrl: URL? = javaClass.classLoader.getResource(filePath0)
                if (classPathUrl != null) {
                    return MiraiSendOnlyImage.of(classPathUrl.toResource(name ?: classPathUrl.toString()))
                }
            }

            // not 'classpath'
            val path = Path(filePath).takeIf { it.exists() }
            if (path != null) {
                return MiraiSendOnlyImage.of(path.toResource(name ?: path.toString()))
            }
        }

        // not exists file. find url
        val urlString = filePath?.takeIf { it.startsWith("http") }
            ?: neko["url"]
            ?: throw SimbotIllegalArgumentException("no valid property 'file' or 'url'.")

        return MiraiSendOnlyImage.of(URL(urlString).toResource(name ?: urlString))
    }
}

internal object VoiceDecoder : CatCodeDecoder {
    override fun decode(neko: Neko, baseMessageChain: MessageChain?): Message.Element<*> {

        TODO("Not yet implemented")
    }
}