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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.action.SendSupport
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.message.Message
import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.NormalMember
import java.util.stream.Stream
import net.mamoe.mirai.contact.Member as OriginalMiraiMember


/**
 *
 * @author ForteScarlet
 */
internal class MiraiMemberImpl(
    override val bot: MiraiBotImpl,
    override val originalContact: OriginalMiraiMember,
    private val initGroup: MiraiGroupImpl? = null
) : MiraiMember, SendSupport {
    override val id: LongID = originalContact.id.ID

    override val group: MiraiGroupImpl get() = initGroup ?: originalContact.group.asSimbot(bot)
    override val roles: Stream<MemberRole> = Stream.of(originalContact.simbotRole)
    override suspend fun roles(): Flow<MemberRole> = flowOf(originalContact.simbotRole)

    override val status: UserStatus =
        when (originalContact) {
            is AnonymousMember -> AnonymousStatus
            else -> NormalStatus
        }


    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember> {
        val receipt = originalContact.sendMessage(message.toOriginalMiraiMessage(originalContact))
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember> {
        return SimbotMiraiMessageReceiptImpl(originalContact.sendMessage(text))
    }


    override suspend fun kick(message: String, block: Boolean): Boolean {
        val contact = originalContact
        if (contact is NormalMember) {
            contact.kick(message, block)
            return true
        }
        return false
    }

}

internal val NormalStatus = UserStatus.builder().normal().build()
internal val AnonymousStatus = UserStatus.builder().anonymous().build()


internal fun OriginalMiraiMember.asSimbot(bot: MiraiBotImpl): MiraiMemberImpl =
    bot.computeMember(this) { MiraiMemberImpl(bot, this) }

internal fun OriginalMiraiMember.asSimbot(bot: MiraiBotImpl, initGroup: MiraiGroupImpl): MiraiMemberImpl =
    bot.computeMember(this) { MiraiMemberImpl(bot, this, initGroup) }
