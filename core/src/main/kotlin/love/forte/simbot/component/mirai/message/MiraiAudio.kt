package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.Component
import love.forte.simbot.component.mirai.ComponentMirai
import love.forte.simbot.message.Message
import love.forte.simbot.resources.StreamableResource
import net.mamoe.mirai.contact.AudioSupported
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.AudioCodec
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import kotlin.reflect.KClass

/**
 * Mirai的原生图片类型 [net.mamoe.mirai.message.data.Audio]
 */
public typealias NativeMiraiAudio = net.mamoe.mirai.message.data.Audio


@SerialName("mirai.sendOnlyAudio")
@Serializable
public class MiraiSendOnlyAudio(
    private val resource: StreamableResource
) : MiraiSendOnlyComputableSimbotMessage<MiraiSendOnlyAudio> {

    override val key: Message.Key<MiraiSendOnlyAudio>
        get() = Key

    override fun equals(other: Any?): Boolean {
        if (other !is MiraiSendOnlyAudio) return false
        return other === this || other.resource == resource
    }

    override fun toString(): String {
        TODO("Not yet implemented")
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage {
        return resource.openStream().use {
            if (contact is AudioSupported) {
                it.toExternalResource().use { external ->
                    contact.uploadAudio(external)
                }
            } else {
                throw IllegalStateException("Contact $contact of type ${contact::class} does not support audio")
            }
        }
    }

    public companion object Key : Message.Key<MiraiSendOnlyAudio> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<MiraiSendOnlyAudio>
            get() = MiraiSendOnlyAudio::class
    }
}


/**
 * 将一个 [NativeMiraiAudio] 作为 simbot的 [love.forte.simbot.message.Message.Element] 进行使用。
 *
 * @author ForteScarlet
 *
 * @see NativeMiraiAudio
 * @see MiraiAudio.of
 */
public interface MiraiAudio : MiraiNativeComputableSimbotMessage<MiraiAudio> {

    /**
     * Mirai的原生 [NativeMiraiAudio] 对象实例。
     */
    public val nativeAudio: NativeMiraiAudio

    public val filename: String get() = nativeAudio.filename
    public val fileMd5: ByteArray get() = nativeAudio.fileMd5
    public val fileSize: Long get() = nativeAudio.fileSize

    public val codec: AudioCodec get() = nativeAudio.codec
    public val extraData: ByteArray? get() = nativeAudio.extraData

    override suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage = nativeAudio

    public companion object Key : Message.Key<MiraiAudio> {

        /**
         * 将一个 [NativeMiraiAudio] 转化为 [MiraiAudio].
         */
        @JvmStatic
        public fun of(nativeAudio: NativeMiraiAudio): MiraiAudio {
            return MiraiAudioImpl(nativeAudio)
        }

        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<MiraiAudio>
            get() = MiraiAudio::class
    }
}


@SerialName("mirai.audio")
@Serializable
internal class MiraiAudioImpl(
    override val nativeAudio: NativeMiraiAudio
) : MiraiAudio {
    override val key: Message.Key<MiraiAudio>
        get() = MiraiAudio.Key

    override fun equals(other: Any?): Boolean {
        if (other !is MiraiAudio) return false
        return nativeAudio == other.nativeAudio
    }

    override fun toString(): String = nativeAudio.toString()
    override fun hashCode(): Int = nativeAudio.hashCode()
}


public fun NativeMiraiAudio.asSimbot(): MiraiAudio = MiraiAudio.of(this)