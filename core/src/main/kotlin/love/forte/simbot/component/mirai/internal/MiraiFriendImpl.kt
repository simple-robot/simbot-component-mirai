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
    override val nativeFriend: NativeMiraiFriend
) : MiraiFriend {

    override val id = nativeFriend.id.ID
    override val status: UserStatus get() = NormalStatus

    override suspend fun send(message: Message): MessageReceipt {
        val receipt = nativeFriend.sendMessage(message.toNativeMiraiMessage(nativeFriend))
        return SimbotMiraiMessageReceipt(receipt)
    }
}
