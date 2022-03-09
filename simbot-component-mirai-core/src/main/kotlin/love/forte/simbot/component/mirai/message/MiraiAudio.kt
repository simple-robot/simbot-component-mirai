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
import love.forte.simbot.message.Message
import love.forte.simbot.resources.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import kotlin.reflect.*

/**
 * Mirai的原生图片类型 [net.mamoe.mirai.message.data.Audio]
 */
public typealias NativeMiraiAudio = net.mamoe.mirai.message.data.Audio


@SerialName("mirai.sendOnlyAudio")
@Serializable
public class MiraiSendOnlyAudio(
    private val resource: Resource
) : MiraiSendOnlyComputableSimbotMessage<MiraiSendOnlyAudio> {

    override val key: Message.Key<MiraiSendOnlyAudio>
        get() = Key

    override fun equals(other: Any?): Boolean {
        if (other !is MiraiSendOnlyAudio) return false
        return other === this || other.resource == resource
    }

    override fun toString(): String = resource.toString()

    override fun hashCode(): Int = resource.hashCode()

    @JvmSynthetic
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

    @JvmSynthetic
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
        if (other === this) return true
        if (other !is MiraiAudio) return false
        return nativeAudio == other.nativeAudio
    }

    override fun toString(): String = nativeAudio.toString()
    override fun hashCode(): Int = nativeAudio.hashCode()
}


public fun NativeMiraiAudio.asSimbot(): MiraiAudio = MiraiAudio.of(this)