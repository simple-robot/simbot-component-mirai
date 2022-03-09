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

package love.forte.simbot.component.mirai.message

import kotlinx.serialization.*
import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.message.Image
import love.forte.simbot.message.Message
import love.forte.simbot.resources.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import java.net.*
import java.util.concurrent.atomic.*
import kotlin.reflect.*

/**
 * Mirai的原生图片类型 [net.mamoe.mirai.message.data.Image]
 */
public typealias OriginalMiraiImage = net.mamoe.mirai.message.data.Image

/**
 * Mirai的原生图片类型 [net.mamoe.mirai.message.data.FlashImage]
 */
public typealias OriginalMiraiFlashImage = FlashImage

/**
 * 一个仅用于发送的临时 [OriginalMiraiImage] 类型，通过 [MiraiBot.uploadImage] 有可能会得到。
 * 不建议对其进行长久的序列化，因为其内部的 [resource] 中保存的内容很有可能是 **临时** 内容。
 *
 * [MiraiSendOnlyImage] 是通过 [Resource] 作为上传资源使用的，
 * 如果你希望仅通过一个ID来获取图片，请参考 [love.forte.simbot.resources.Resource] 并使用在 [MiraiBot.uploadImage] 中。
 *
 * @see Resource
 * @see MiraiBot.uploadImage
 *
 */
public interface MiraiSendOnlyImage :
    MiraiSendOnlyComputableMessage<MiraiSendOnlyImage>,
    Image<MiraiSendOnlyImage> {

    /**
     * 图片发送所使用的资源对象。
     */
    public val resource: Resource

    /**
     * 是否作为一个闪照。
     */
    public val isFlash: Boolean

    /**
     * 作为仅用于发送的图片时的类型，
     * 在真正获取过图片(执行过一次 [originalMiraiMessage])之前将无法获取到ID，因此在那之前 [id] 的值将为空。
     * 并且此ID不稳定，图片的上传目前 **没有** 缓存，每次执行后得到的ImageId可能会不同，
     * 但是 [id] 在更新后将不会再次变更。
     *
     * 因此无法保证ID的准确性，应尽可能避免对此ID进行操作。
     */
    override val id: ID

    public companion object Key : Message.Key<MiraiSendOnlyImage> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<MiraiSendOnlyImage>
            get() = MiraiSendOnlyImage::class
    }
}


@SerialName("mirai.sendOnlyImage")
@Serializable
internal class MiraiSendOnlyImageImpl(
    override val resource: Resource,
    override val isFlash: Boolean
) : MiraiSendOnlyImage {
    @Transient
    private val _id = AtomicReference(ID)

    override val id: ID get() = _id.get()

    @JvmSynthetic
    override suspend fun resource(): Resource = resource
    override val key: Message.Key<MiraiSendOnlyImage>
        get() = MiraiSendOnlyImage.Key

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MiraiSendOnlyImageImpl) return false
        return resource === other.resource
    }

    /**
     * 返回值只可能是 [OriginalMiraiFlashImage] 或 [OriginalMiraiImage].
     */
    @JvmSynthetic
    override suspend fun originalMiraiMessage(contact: Contact): OriginalMiraiMessage {
        return resource.openStream().use {
            contact.uploadImage(it).let { image ->
                _id.compareAndSet(ID, image.imageId.ID)

                if (isFlash) image.flash() else image
            }
        }
    }

    override fun toString(): String = resource.toString()
    override fun hashCode(): Int = resource.hashCode()

    companion object {
        private val ID = "".ID
    }
}

/**
 * 将一个 [OriginalMiraiImage] 作为 simbot的 [love.forte.simbot.message.Image] 进行使用。
 *
 */
public interface MiraiImage :
    MiraiSimbotMessage<MiraiImage>,
    Image<MiraiImage> {

    /**
     * 得到Mirai的原生图片类型 [OriginalMiraiImage].
     */
    public val originalImage: OriginalMiraiImage

    /**
     * 此图片是否为一个 `闪照`。
     * @see OriginalMiraiFlashImage
     *
     */
    public val isFlash: Boolean

    /**
     * 此图片的 `imageId`
     */
    override val id: ID

    /**
     * 图片的宽度 (px), 当无法获取时为 0
     */
    public val width: Int get() = originalImage.width

    /**
     * 图片的高度 (px), 当无法获取时为 0
     */
    public val height: Int get() = originalImage.height

    /**
     * 图片的大小（字节）, 当无法获取时为 0
     */
    public val size: Long get() = originalImage.size

    /**
     * 图片的类型, 当无法获取时为未知 [ImageType.UNKNOWN]
     * @see ImageType
     */
    public val imageType: ImageType get() = originalImage.imageType

    /**
     * 判断该图片是否为 `动画表情`
     */
    public val isEmoji: Boolean get() = originalImage.isEmoji


    /**
     * 通过 [queryUrl] 查询并得到 [Resource] 对象。
     */
    @JvmSynthetic
    override suspend fun resource(): Resource {
        return URL(originalImage.queryUrl()).toResource()
    }


    public companion object Key : Message.Key<MiraiImage> {

        @JvmStatic
        @JvmOverloads
        public fun of(nativeImage: OriginalMiraiImage, isFlash: Boolean = false): MiraiImage {
            return MiraiImageImpl(nativeImage, isFlash)
        }

        @JvmStatic
        @JvmOverloads
        public fun of(nativeImage: OriginalMiraiFlashImage, isFlash: Boolean = true): MiraiImage {
            return MiraiImageImpl(nativeImage.image, isFlash)
        }

        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<MiraiImage>
            get() = MiraiImage::class
    }

}


@SerialName("mirai.image")
@Serializable
internal class MiraiImageImpl(
    override val originalImage: OriginalMiraiImage,
    override val isFlash: Boolean
) : MiraiImage {
    override val id: ID = originalImage.imageId.ID
    override val key: Message.Key<MiraiImage> get() = MiraiImage.Key


    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MiraiImage) return false
        return originalImage == other.originalImage
    }

    override fun toString(): String = originalImage.toString()
    override fun hashCode(): Int = originalImage.hashCode()


}

public fun OriginalMiraiImage.asSimbot(isFlash: Boolean = false): MiraiImage = MiraiImage.of(this, isFlash)
public fun OriginalMiraiFlashImage.asSimbot(isFlash: Boolean = true): MiraiImage = MiraiImage.of(this, isFlash)