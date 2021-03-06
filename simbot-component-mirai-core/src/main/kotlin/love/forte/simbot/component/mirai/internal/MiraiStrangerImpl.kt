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

package love.forte.simbot.component.mirai.internal

import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.MiraiStranger
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