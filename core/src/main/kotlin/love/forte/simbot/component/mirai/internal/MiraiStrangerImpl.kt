package love.forte.simbot.component.mirai.internal

import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.MiraiStranger
import love.forte.simbot.component.mirai.NativeMiraiStranger
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.message.Message


/**
 *
 * @author ForteScarlet
 */
internal class MiraiStrangerImpl(
    override val bot: MiraiBotImpl,
    override val nativeContact: NativeMiraiStranger
) : MiraiStranger {
    override val id: LongID = nativeContact.id.ID

    override suspend fun send(message: Message): SimbotMiraiMessageReceiptImpl<NativeMiraiStranger> {
        val nativeMessage = message.toNativeMiraiMessage(nativeContact)
        val receipt = nativeContact.sendMessage(nativeMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
}


internal fun NativeMiraiStranger.asSimbot(bot: MiraiBotImpl): MiraiStrangerImpl = MiraiStrangerImpl(bot, this)