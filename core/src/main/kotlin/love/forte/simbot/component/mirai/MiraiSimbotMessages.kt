package love.forte.simbot.component.mirai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.Component
import love.forte.simbot.ID
import love.forte.simbot.message.Message
import love.forte.simbot.resources.Resource
import love.forte.simbot.resources.StreamableResource
import love.forte.simbot.resources.toResource
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import java.net.URL
import kotlin.reflect.KClass
import net.mamoe.mirai.message.data.Image as NativeMiraiImage

/**
 *
 *
 * @see SimbotSendOnlyComputableMiraiMessage
 */
public sealed class MiraiSimbotMessage<E : MiraiSimbotMessage<E>> : Message.Element<E> {
    public abstract suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage
}


/**
 * 直接将一个 [NativeMiraiSingleMessage] 作为 [Message] 使用，将会忽略掉 [MiraiSimbotMessage.nativeMiraiMessage] 的参数 [Contact].
 * [NativeMiraiSingleMessage] -> [Message]
 */
@SerialName("mirai.nativeMessage")
@Serializable
public class SimbotNativeMiraiMessage(
    public val nativeMiraiMessage: NativeMiraiSingleMessage
) : MiraiSimbotMessage<SimbotNativeMiraiMessage>() {
    override val key: Message.Key<SimbotNativeMiraiMessage> get() = Key

    override suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiSingleMessage {
        return nativeMiraiMessage
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SimbotNativeMiraiMessage) return false
        return other.nativeMiraiMessage == this.nativeMiraiMessage
    }

    override fun toString(): String = nativeMiraiMessage.toString()
    override fun hashCode(): Int = nativeMiraiMessage.hashCode()

    public companion object Key : Message.Key<SimbotNativeMiraiMessage> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<SimbotNativeMiraiMessage>
            get() = SimbotNativeMiraiMessage::class
    }
}


/**
 * 通过函数 suspend ([Contact]) -> [NativeMiraiMessage] 得到一个 **仅用于发送** 的 [Message].
 *
 * [SimbotSendOnlyComputableMiraiMessage] **不可序列化**，仅用于在发送的时候使用任意的 [NativeMiraiMessage] 作为 [Message] 进行发送。
 *
 * [SimbotSendOnlyComputableMiraiMessage] 通过一个挂起函数 `suspend (Contact) -> NativeMiraiMessage`
 * 根据一个 [Contact] 来根据当前发送消息的目标来获取一个消息实例。
 *
 */
public open class SimbotSendOnlyComputableMiraiMessage(
    private val factory: suspend (Contact) -> NativeMiraiMessage,
) : MiraiSimbotMessage<SimbotSendOnlyComputableMiraiMessage>() {
    override val key: Message.Key<SimbotSendOnlyComputableMiraiMessage> get() = Key


    override suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage {
        return factory(contact)
    }


    override fun equals(other: Any?): Boolean {
        if (other !is SimbotSendOnlyComputableMiraiMessage) return false
        return other === this
    }

    override fun toString(): String = "SimbotSendOnlyMiraiMessage($factory)"
    override fun hashCode(): Int = factory.hashCode()


    public companion object Key : Message.Key<SimbotSendOnlyComputableMiraiMessage> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<SimbotSendOnlyComputableMiraiMessage>
            get() = SimbotSendOnlyComputableMiraiMessage::class
    }
}

/**
 * 一个仅用于发送的临时 [NativeMiraiImage] 类型，通过 [MiraiBot.uploadImage] 有可能会得到。
 * 不建议对其进行长久的序列化，因此其内部的 [resource] 中保存的内容很有可能是 **临时** 内容。
 */
@SerialName("mirai.sendOnlyImage")
@Serializable
public class SendOnlyImageMessage(private val resource: StreamableResource) :
    love.forte.simbot.message.Image<SendOnlyImageMessage> {
    override val id: ID get() = ID
    override suspend fun resource(): Resource = resource
    override val key: Message.Key<SendOnlyImageMessage>
        get() = Key

    override fun equals(other: Any?): Boolean {
        if (other !is SendOnlyImageMessage) return false
        return this === other || resource === other.resource
    }

    override fun toString(): String = resource.toString()
    override fun hashCode(): Int = resource.hashCode()

    public companion object Key : Message.Key<SendOnlyImageMessage> {
        private val ID = "".ID
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<SendOnlyImageMessage>
            get() = SendOnlyImageMessage::class
    }
}


/**
 * 几个标准的mirai simbot message.
 */
public sealed class StandardMiraiSimbotMessage<E : StandardMiraiSimbotMessage<E>> : MiraiSimbotMessage<E>()

/**
 * 将一个 [NativeMiraiImage] 作为 simbot的 [love.forte.simbot.message.Image] 进行使用。
 *
 */
@SerialName("mirai.image")
@Serializable
public class MiraiImage(
    public val nativeImage: NativeMiraiImage,
) : love.forte.simbot.message.Image<MiraiImage> {
    public override val id: ID = nativeImage.imageId.ID

    override suspend fun resource(): Resource {
        return URL(nativeImage.queryUrl()).toResource()
    }

    override val key: Message.Key<MiraiImage> get() = Key

    override fun equals(other: Any?): Boolean {
        if (other !is MiraiImage) return false
        return nativeImage == other.nativeImage
    }

    override fun toString(): String = nativeImage.toString()
    override fun hashCode(): Int = nativeImage.hashCode()


    public companion object Key : Message.Key<MiraiImage> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<MiraiImage>
            get() = MiraiImage::class
    }
}