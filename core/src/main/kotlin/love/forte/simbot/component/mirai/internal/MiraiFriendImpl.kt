package love.forte.simbot.component.mirai.internal

import love.forte.simbot.ID
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.NativeMiraiFriend
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageReceipt


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

    override suspend fun send(message: Message): MessageReceipt {
        val receipt = nativeContact.sendMessage(message.toNativeMiraiMessage(nativeContact))
        return SimbotMiraiMessageReceipt(receipt)
    }
}

internal fun NativeMiraiFriend.asSimbot(bot: MiraiBotImpl): MiraiFriendImpl =
    bot.computeFriend(this) { MiraiFriendImpl(bot, this) }