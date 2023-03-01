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
