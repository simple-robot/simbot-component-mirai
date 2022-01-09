package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.flow.Flow
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.action.SendSupport
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.NativeMiraiMember
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.toNativeMiraiMessage
import love.forte.simbot.definition.Role
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageReceipt
import net.mamoe.mirai.contact.AnonymousMember


/**
 *
 * @author ForteScarlet
 */
internal class MiraiMemberImpl(
    override val bot: MiraiBotImpl,
    override val nativeMember: NativeMiraiMember,
) : MiraiMember, SendSupport {
    override val id: LongID = nativeMember.id.ID

    override suspend fun send(message: Message): MessageReceipt {
        val receipt = nativeMember.sendMessage(message.toNativeMiraiMessage(nativeMember))
        return SimbotMiraiMessageReceipt(receipt)
    }


    override suspend fun group(): MiraiGroupImpl {
        return MiraiGroupImpl(bot, nativeMember.group)
    }

    override suspend fun roles(): Flow<Role> {
        TODO("Not yet implemented")
    }

    override val status: UserStatus =
        when (nativeMember) {
            is AnonymousMember -> AnonymousStatus
            else -> NormalStatus
        }
}

internal val NormalStatus = UserStatus.builder().normal().build()
internal val AnonymousStatus = UserStatus.builder().anonymous().build()


internal fun NativeMiraiMember.asSimbotMember(bot: MiraiBotImpl): MiraiMemberImpl = this.asSimbotMember(bot)
