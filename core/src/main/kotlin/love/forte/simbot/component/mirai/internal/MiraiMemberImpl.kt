package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.action.SendSupport
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageReceipt
import net.mamoe.mirai.contact.AnonymousMember
import java.util.stream.Stream


/**
 *
 * @author ForteScarlet
 */
internal class MiraiMemberImpl(
    override val bot: MiraiBotImpl,
    override val nativeContact: NativeMiraiMember,
    private val initGroup: MiraiGroupImpl? = null
) : MiraiMember, SendSupport {
    override val id: LongID = nativeContact.id.ID

    override suspend fun send(message: Message): MessageReceipt {
        val receipt = nativeContact.sendMessage(message.toNativeMiraiMessage(nativeContact))
        return SimbotMiraiMessageReceipt(receipt)
    }

    override val group: MiraiGroupImpl get() = initGroup ?: nativeContact.group.asSimbot(bot)
    override val roles: Stream<MemberRole> = Stream.of(nativeContact.simbotRole)
    override suspend fun roles(): Flow<MemberRole> = flowOf(nativeContact.simbotRole)

    override val status: UserStatus =
        when (nativeContact) {
            is AnonymousMember -> AnonymousStatus
            else -> NormalStatus
        }
}

internal val NormalStatus = UserStatus.builder().normal().build()
internal val AnonymousStatus = UserStatus.builder().anonymous().build()


internal fun NativeMiraiMember.asSimbot(bot: MiraiBotImpl): MiraiMemberImpl =
    bot.computeMember(this) { MiraiMemberImpl(bot, this) }

internal fun NativeMiraiMember.asSimbot(bot: MiraiBotImpl, initGroup: MiraiGroupImpl): MiraiMemberImpl =
    bot.computeMember(this) { MiraiMemberImpl(bot, this, initGroup) }
