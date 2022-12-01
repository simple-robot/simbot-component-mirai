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

package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import love.forte.simbot.CharSequenceID
import love.forte.simbot.ID
import love.forte.simbot.message.Message
import love.forte.simbot.resources.Resource
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.FlashImage as OriginalMiraiFlashImage
import net.mamoe.mirai.message.data.Image as OriginalMiraiImage


@SerialName("mirai.sendOnlyImage")
@Serializable
internal class MiraiSendOnlyImageImpl(
    private val originalResource: Resource,
    override val isFlash: Boolean,
) : MiraiSendOnlyImage {
    
    @Transient
    override val id: ID = originalResource.name.ID
    
    override suspend fun resource(): Resource = originalResource
    override val key: Message.Key<MiraiSendOnlyImage>
        get() = MiraiSendOnlyImage.Key
    
    override suspend fun upload(contact: Contact): MiraiImage {
        return originalResource.uploadToImage(contact).asSimbot(isFlash)
    }
    
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MiraiSendOnlyImageImpl) return false
        return originalResource == other.originalResource
    }
    
    
    /**
     * 返回值只可能是 [OriginalMiraiFlashImage] 或 [OriginalMiraiImage].
     */
    override suspend fun originalMiraiMessage(
        contact: Contact,
        isDropAction: Boolean,
    ): net.mamoe.mirai.message.data.Message {
        return originalResource.uploadToImage(contact, isFlash)
    }
    
    override fun toString(): String = originalResource.toString()
    override fun hashCode(): Int = originalResource.hashCode()
    
    companion object {
        // private val ID = "".ID
    }
}


@SerialName("mirai.image")
@Serializable
internal class MiraiImageImpl(
    override val originalImage: OriginalMiraiImage,
    override val isFlash: Boolean,
) : MiraiImage {
    override val id: CharSequenceID = originalImage.imageId.ID
    override val key: Message.Key<MiraiImage> get() = MiraiImage.Key
    
    
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MiraiImage) return false
        return originalImage == other.originalImage
    }
    
    override fun toString(): String = originalImage.toString()
    override fun hashCode(): Int = originalImage.hashCode()
}