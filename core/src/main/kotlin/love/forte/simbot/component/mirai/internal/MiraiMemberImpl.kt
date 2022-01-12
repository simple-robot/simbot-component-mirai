package love.forte.simbot.component.mirai.internal

import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.action.SendSupport
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
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
    override val nativeContact: NativeMiraiMember,
) : MiraiMember, SendSupport {
    override val id: LongID = nativeContact.id.ID

    override suspend fun send(message: Message): MessageReceipt {
        val receipt = nativeContact.sendMessage(message.toNativeMiraiMessage(nativeContact))
        return SimbotMiraiMessageReceipt(receipt)
    }

    override val group: MiraiGroup = MiraiGroupImpl(bot, nativeContact.group)
    override val roles: List<MiraiRole> = listOf(nativeContact.simbotRole)

    override val status: UserStatus =
        when (nativeContact) {
            is AnonymousMember -> AnonymousStatus
            else -> NormalStatus
        }
}

internal val NormalStatus = UserStatus.builder().normal().build()
internal val AnonymousStatus = UserStatus.builder().anonymous().build()


internal fun NativeMiraiMember.asSimbotMember(bot: MiraiBotImpl): MiraiMemberImpl = this.asSimbotMember(bot)
