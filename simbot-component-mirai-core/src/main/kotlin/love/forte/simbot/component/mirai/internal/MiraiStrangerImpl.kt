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

package love.forte.simbot.component.mirai.internal

import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.MiraiStranger
import love.forte.simbot.component.mirai.MiraiUserProfile
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.utils.runInBlocking
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger


/**
 *
 * @author ForteScarlet
 */
internal class MiraiStrangerImpl(
    override val bot: MiraiBotImpl,
    override val originalContact: OriginalMiraiStranger,
) : MiraiStranger {
    override val id: LongID = originalContact.id.ID

    override suspend fun queryProfile(): MiraiUserProfile {
        return originalContact.queryProfile().asSimbot()
    }

    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceiptImpl<OriginalMiraiStranger> {
        val nativeMessage = message.toOriginalMiraiMessage(originalContact)
        val receipt = originalContact.sendMessage(nativeMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(text: String): SimbotMiraiMessageReceiptImpl<OriginalMiraiStranger> {
        val receipt = originalContact.sendMessage(text)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceiptImpl<OriginalMiraiStranger> {
        val nativeMessage = message.messages.toOriginalMiraiMessage(originalContact)
        val receipt = originalContact.sendMessage(nativeMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceiptImpl<OriginalMiraiStranger> {
        return runInBlocking { send(text) }
    }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceiptImpl<OriginalMiraiStranger> {
        return runInBlocking { send(message) }
    }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceiptImpl<OriginalMiraiStranger> {
        return runInBlocking { send(message) }
    }
}


internal fun OriginalMiraiStranger.asSimbot(bot: MiraiBotImpl): MiraiStrangerImpl = MiraiStrangerImpl(bot, this)
