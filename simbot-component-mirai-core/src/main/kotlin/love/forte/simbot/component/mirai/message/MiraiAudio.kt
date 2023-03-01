/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  This file is part of simbot-component-mirai.
 *
 *  simbot-component-mirai is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  simbot-component-mirai is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with simbot-component-mirai. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.simbot.SimbotIllegalArgumentException
import love.forte.simbot.component.mirai.JST
import love.forte.simbot.component.mirai.MiraiContact
import love.forte.simbot.component.mirai.MiraiContactContainer
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.message.MiraiAudio.Key.asSimbot
import love.forte.simbot.message.Message
import love.forte.simbot.message.doSafeCast
import love.forte.simbot.resources.Resource
import net.mamoe.mirai.contact.AudioSupported
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.AudioCodec
import net.mamoe.mirai.message.data.OfflineAudio
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.message.data.Audio as OriginalMiraiAudio

/**
 * 一个在simbot中仅用于发送的 _语音_ 消息对象。
 *
 * [MiraiSendOnlyAudio] 构建时不会产生任何挂起、网络交互等行为，
 * 其只是一种 _预处理_ 对象，只有在真正发送时才会进行上传。
 *
 *
 * [MiraiSendOnlyAudio] 可以重复使用，但是**每次发送**都会产生数据流读取和上传的行为。
 * 如果你希望能够得到一个一次上传后可复用的音频对象（就像 [Mirai OfflineAudio][OfflineAudio]），
 * 你可以参考api [uploadTo]。
 *
 */
@SerialName("mirai.sendOnlyAudio")
@Serializable
public class MiraiSendOnlyAudio(
    private val resource: Resource,
) : MiraiSendOnlyComputableMessage<MiraiSendOnlyAudio> {
    
    override val key: Message.Key<MiraiSendOnlyAudio>
        get() = Key
    
    override fun equals(other: Any?): Boolean {
        if (other !is MiraiSendOnlyAudio) return false
        return other === this || other.resource == resource
    }
    
    override fun toString(): String = resource.toString()
    
    override fun hashCode(): Int = resource.hashCode()
    
    
    
    private suspend fun uploadAudioTo(originalAudioSupported: AudioSupported): OfflineAudio {
        return resource.openStream().use {
            it.toExternalResource().use { external ->
                originalAudioSupported.uploadAudio(external)
            }
        }
    }
    
    /**
     * 根据联系目标得到发送用的消息对象。
     *
     * @throws IllegalArgumentException 如果 [contact] 不支持音频上传
     */
    @JvmBlocking(baseName = "getOriginalMiraiMessage", suffix = "")
    @JvmAsync(baseName = "getOriginalMiraiMessage")
    override suspend fun originalMiraiMessage(contact: Contact, isDropAction: Boolean): OfflineAudio {
        return if (contact is AudioSupported) {
            uploadAudioTo(contact)
        } else {
            throw SimbotIllegalArgumentException("Contact $contact of type ${contact::class} does not support audio")
        }
    }
    
    /**
     * 根据联系目标得到发送用的消息对象。
     *
     * @throws IllegalArgumentException 如果 [contact] 不支持音频上传
     */
    @JvmBlocking(baseName = "getOriginalMiraiMessage", suffix = "")
    @JvmAsync(baseName = "getOriginalMiraiMessage")
    override suspend fun originalMiraiMessage(contact: Contact): OfflineAudio = originalMiraiMessage(contact, false)
    
    /**
     * 直接提供一个mirai原生的 [AudioSupported] 对象，上传并得到上传结果 [MiraiAudio].
     *
     * @return 音频上传结果。
     */
    @JST
    public suspend fun uploadTo(originalAudioSupported: AudioSupported): MiraiAudio {
        return uploadAudioTo(originalAudioSupported).asSimbot()
    }
    
    /**
     * 提供一个simbot-mirai组件下的 [MiraiContactContainer] 类型（例如[MiraiContact] 或 [MiraiGroup]），
     * 并尝试使用此对象上传当前音频并得到 [MiraiAudio].
     *
     * @throws SimbotIllegalArgumentException 如果 [miraiContact] 中的原始mirai对象不是 [AudioSupported] 类型
     *
     * @return 音频上传结果。
     */
    @JST
    public suspend fun uploadTo(miraiContact: MiraiContactContainer): MiraiAudio {
        val original = miraiContact.originalContact
        if (original !is AudioSupported) {
            throw SimbotIllegalArgumentException("Original mirai contact $original in $miraiContact dos not support audio")
        }
        return uploadTo(original)
    }
    
    
    public companion object Key : Message.Key<MiraiSendOnlyAudio> {
        override fun safeCast(value: Any): MiraiSendOnlyAudio? = doSafeCast(value)
    }
}


/**
 * 将一个 [OriginalMiraiAudio] 作为simbot的 [love.forte.simbot.message.Message.Element] 进行使用。
 *
 * @author ForteScarlet
 *
 * @see OriginalMiraiAudio
 * @see MiraiAudio.asSimbot
 */
public interface MiraiAudio : OriginalMiraiDirectlySimbotMessage<OriginalMiraiAudio, MiraiAudio> {
    
    /**
     * Mirai的原生 [OriginalMiraiAudio] 对象实例。
     */
    public val originalAudio: OriginalMiraiAudio
    
    public val filename: String get() = originalAudio.filename
    public val fileMd5: ByteArray get() = originalAudio.fileMd5
    public val fileSize: Long get() = originalAudio.fileSize
    
    public val codec: AudioCodec get() = originalAudio.codec
    public val extraData: ByteArray? get() = originalAudio.extraData
    
    /**
     * 同 [originalAudio]
     */
    override val originalMiraiMessage: OriginalMiraiAudio
        get() = originalAudio
    
    public companion object Key : Message.Key<MiraiAudio> {
        
        /**
         * 将一个 [OriginalMiraiAudio] 转化为 [MiraiAudio].
         */
        @JvmStatic
        @JvmName("of")
        public fun OriginalMiraiAudio.asSimbot(): MiraiAudio {
            return MiraiAudioImpl(this)
        }
        
        override fun safeCast(value: Any): MiraiAudio? = doSafeCast(value)
    }
}


@SerialName("mirai.audio")
@Serializable
internal class MiraiAudioImpl(
    override val originalAudio: OriginalMiraiAudio,
) : MiraiAudio {
    override val key: Message.Key<MiraiAudio>
        get() = MiraiAudio.Key
    
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MiraiAudio) return false
        return originalAudio == other.originalAudio
    }
    
    override fun toString(): String = originalAudio.toString()
    override fun hashCode(): Int = originalAudio.hashCode()
}
