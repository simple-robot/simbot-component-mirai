package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.flow.Flow
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.NativeMiraiMember
import love.forte.simbot.definition.Organization
import love.forte.simbot.definition.Role
import love.forte.simbot.definition.UserStatus
import net.mamoe.mirai.contact.AnonymousMember


/**
 *
 * @author ForteScarlet
 */
internal class MiraiMemberImpl(
    override val bot: MiraiBotImpl,
    override val nativeMember: NativeMiraiMember,
) : MiraiMember {
    override val id: LongID = nativeMember.id.ID

    override suspend fun organization(): Organization {
        TODO("Not yet implemented")
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

private val NormalStatus = UserStatus.builder().normal().build()
private val AnonymousStatus = UserStatus.builder().anonymous().build()


internal fun NativeMiraiMember.asSimbotMember(bot: MiraiBotImpl): MiraiMemberImpl = this.asSimbotMember(bot)
