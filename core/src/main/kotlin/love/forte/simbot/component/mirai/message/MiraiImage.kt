package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import love.forte.simbot.Component
import love.forte.simbot.ID
import love.forte.simbot.component.mirai.ComponentMirai
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.message.Image
import love.forte.simbot.message.Message
import love.forte.simbot.resources.Resource
import love.forte.simbot.resources.StreamableResource
import love.forte.simbot.resources.toResource
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.message.data.flash
import java.net.URL
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

/**
 * Mirai的原生图片类型 [net.mamoe.mirai.message.data.Image]
 */
public typealias NativeMiraiImage = net.mamoe.mirai.message.data.Image

/**
 * Mirai的原生图片类型 [net.mamoe.mirai.message.data.FlashImage]
 */
public typealias NativeMiraiFlashImage = net.mamoe.mirai.message.data.FlashImage

/**
 * 一个仅用于发送的临时 [NativeMiraiImage] 类型，通过 [MiraiBot.uploadImage] 有可能会得到。
 * 不建议对其进行长久的序列化，因为其内部的 [resource] 中保存的内容很有可能是 **临时** 内容。
 *
 * [MiraiSendOnlyImage] 是通过 [StreamableResource] 作为上传资源使用的，
 * 如果你希望仅通过一个ID来获取图片，请参考 [love.forte.simbot.resources.IDResource] 并使用在 [MiraiBot.uploadImage] 中。
 *
 * @see StreamableResource
 * @see MiraiBot.uploadImage
 *
 */
public interface MiraiSendOnlyImage :
    MiraiSendOnlyComputableSimbotMessage<MiraiSendOnlyImage>,
    Image<MiraiSendOnlyImage> {

    /**
     * 图片发送所使用的资源对象。
     */
    public val resource: StreamableResource

    /**
     * 是否作为一个闪照。
     */
    public val isFlash: Boolean

    /**
     * 作为仅用于发送的图片时的类型，
     * 在真正获取过图片(执行过一次 [nativeMiraiMessage])之前将无法获取到ID，因此在那之前 [id] 的值将为空。
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
    override val resource: StreamableResource,
    override val isFlash: Boolean
) : MiraiSendOnlyImage {
    @Transient
    private val _id = AtomicReference(ID)

    override val id: ID get() = _id.get()

    override suspend fun resource(): Resource = resource
    override val key: Message.Key<MiraiSendOnlyImage>
        get() = MiraiSendOnlyImage.Key

    override fun equals(other: Any?): Boolean {
        if (other !is MiraiSendOnlyImageImpl) return false
        return this === other || resource === other.resource
    }

    /**
     * 返回值只可能是 [NativeMiraiFlashImage] 或 [NativeMiraiImage].
     */
    override suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage {
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
 * 将一个 [NativeMiraiImage] 作为 simbot的 [love.forte.simbot.message.Image] 进行使用。
 *
 */
public interface MiraiImage :
    MiraiSimbotMessage<MiraiImage>,
    Image<MiraiImage> {

    /**
     * 得到Mirai的原生图片类型 [NativeMiraiImage].
     */
    public val nativeImage: NativeMiraiImage

    /**
     * 此图片是否为一个 `闪照`。
     * @see NativeMiraiFlashImage
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
    public val width: Int get() = nativeImage.width

    /**
     * 图片的高度 (px), 当无法获取时为 0
     */
    public val height: Int get() = nativeImage.height

    /**
     * 图片的大小（字节）, 当无法获取时为 0
     */
    public val size: Long get() = nativeImage.size

    /**
     * 图片的类型, 当无法获取时为未知 [ImageType.UNKNOWN]
     * @see ImageType
     */
    public val imageType: ImageType get() = nativeImage.imageType

    /**
     * 判断该图片是否为 `动画表情`
     */
    public val isEmoji: Boolean get() = nativeImage.isEmoji


    /**
     * 通过 [queryUrl] 查询并得到 [Resource] 对象。
     */
    override suspend fun resource(): Resource {
        return URL(nativeImage.queryUrl()).toResource()
    }


    public companion object Key : Message.Key<MiraiImage> {

        @JvmStatic
        public fun of(nativeImage: NativeMiraiImage): MiraiImage {
            return MiraiImageImpl(nativeImage, false)
        }

        @JvmStatic
        public fun of(nativeImage: NativeMiraiFlashImage): MiraiImage {
            return MiraiImageImpl(nativeImage.image, true)
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
    override val nativeImage: NativeMiraiImage,
    override val isFlash: Boolean
) : MiraiImage {
    override val id: ID = nativeImage.imageId.ID
    override val key: Message.Key<MiraiImage> get() = MiraiImage.Key


    override fun equals(other: Any?): Boolean {
        if (other !is MiraiImage) return false
        return nativeImage == other.nativeImage
    }

    override fun toString(): String = nativeImage.toString()
    override fun hashCode(): Int = nativeImage.hashCode()


}

public fun NativeMiraiImage.asSimbot(): MiraiImage = MiraiImage.of(this)
public fun NativeMiraiFlashImage.asSimbot(): MiraiImage = MiraiImage.of(this)