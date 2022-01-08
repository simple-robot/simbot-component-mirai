package love.forte.simbot.component.mirai.internal

import love.forte.simbot.Bot
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.NativeMiraiFriend
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
    override val status: UserStatus get() = NormalUserStatus

    override suspend fun send(message: Message): MessageReceipt {
        TODO("Not yet implemented")
    }
}

private val NormalUserStatus = UserStatus.builder().normal().build()
private val AnonymousUserStatus = UserStatus.builder().anonymous().fakeUser().build()