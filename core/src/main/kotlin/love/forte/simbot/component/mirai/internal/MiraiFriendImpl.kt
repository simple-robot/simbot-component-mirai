package love.forte.simbot.component.mirai.internal

import love.forte.simbot.ID
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.NativeMiraiFriend
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.message.Message


/**
 *
 * @author ForteScarlet
 */
internal class MiraiFriendImpl(
    override val bot: MiraiBotImpl,
    override val nativeContact: NativeMiraiFriend
) : MiraiFriend {

    override val id = nativeContact.id.ID
    override val status: UserStatus get() = NormalStatus

    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend> {
        val receipt = nativeContact.sendMessage(message.toNativeMiraiMessage(nativeContact))
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend> {
        return SimbotMiraiMessageReceiptImpl(nativeContact.sendMessage(text))
    }
}

internal fun NativeMiraiFriend.asSimbot(bot: MiraiBotImpl): MiraiFriendImpl =
    bot.computeFriend(this) { MiraiFriendImpl(bot, this) }