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
 *
 */

package love.forte.simbot.component.mirai.internal

import love.forte.simbot.Api4J
import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiGroupMemberBot
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageReceipt
import net.mamoe.mirai.contact.AnonymousMember

/**
 *
 * @author ForteScarlet
 */
internal class MiraiGroupMemberBotImpl(
    private val baseBot: MiraiBotImpl,
    val member: MiraiMemberImpl,
) : MiraiGroupMemberBot, MiraiBot by baseBot, MiraiMember by member {
    override val bot: MiraiBotImpl get() = baseBot
    override val avatar: String
        get() = baseBot.avatar
    override val username: String
        get() = baseBot.username
    
    override val id: LongID
        get() = baseBot.id
    
    @Api4J
    override fun sendIfSupportBlocking(message: Message): MessageReceipt {
        return sendBlocking(message)
    }
    
    override val status: UserStatus = when (originalContact) {
        is AnonymousMember -> AnonymousBotMemberStatus
        else -> NormalBotMemberStatus
    }
    
    override fun toString(): String {
        return "MiraiGroupMemberBotImpl(baseBot=$baseBot, member=$member)"
    }
    
    override fun hashCode(): Int {
        return (baseBot.hashCode() * 59) + member.hashCode()
    }
    
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MiraiGroupMemberBotImpl) return false
        return other.baseBot == baseBot && other.member == member
    }
    
    
    companion object {
        fun MiraiBotImpl.toMemberBot(member: MiraiMemberImpl): MiraiGroupMemberBotImpl =
            MiraiGroupMemberBotImpl(this, member)
    }
}


internal val NormalBotMemberStatus = UserStatus.builder().bot().normal().build()
internal val AnonymousBotMemberStatus = UserStatus.builder().bot().anonymous().build()
