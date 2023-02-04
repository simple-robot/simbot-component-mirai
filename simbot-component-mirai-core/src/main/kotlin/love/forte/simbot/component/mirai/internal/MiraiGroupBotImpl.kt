/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
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

import love.forte.simbot.Component
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.bot.MiraiBotManager
import love.forte.simbot.component.mirai.bot.MiraiFriendCategories
import love.forte.simbot.component.mirai.bot.MiraiGroupBot
import love.forte.simbot.component.mirai.message.MiraiImage
import love.forte.simbot.component.mirai.message.MiraiSendOnlyImage
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.resources.Resource
import love.forte.simbot.utils.item.Items
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.message.data.Image
import org.slf4j.Logger

/**
 * [MiraiGroupBot] 实现。应用于 [MiraiGroupImpl.bot].
 * @author ForteScarlet
 */
internal class MiraiGroupBotImpl(
    private val baseBot: MiraiBotImpl,
    override val originalBotMember: NormalMember,
    initGroup: MiraiGroupImpl,
) : MiraiGroupBot {
    override val friendCategories: MiraiFriendCategories
        get() = baseBot.friendCategories
    
    private val member = originalBotMember.asSimbot(baseBot, initGroup)
    
    override suspend fun asMember(): MiraiMember = member

    override suspend fun queryProfile(): MiraiUserProfile {
        return originalBotMember.queryProfile().asSimbot()
    }

    override fun toString(): String {
        return "MiraiGroupMemberBotImpl(baseBot=$baseBot, member=$member)"
    }
    
    // region impl MiraiBot
    override suspend fun contact(id: ID): MiraiContact? = baseBot.contact(id)
    
    override suspend fun stranger(id: ID): MiraiStranger? = baseBot.stranger(id)
    
    override suspend fun friend(id: ID): MiraiFriend? = baseBot.friend(id)
    
    override suspend fun group(id: ID): MiraiGroup? = baseBot.group(id)

    override suspend fun friendCount(): Int = baseBot.friendCount()

    override val strangerCount: Int get() = baseBot.strangerCount

    override suspend fun contactCount(): Int = baseBot.contactCount()

    override suspend fun groupCount(): Int = baseBot.groupCount()

    override suspend fun resolveImage(id: ID): MiraiImage = baseBot.resolveImage(id)
    
    override fun resolveImage(id: ID, flash: Boolean, builderAction: Image.Builder.() -> Unit): MiraiImage =
        baseBot.resolveImage(id, flash, builderAction)
    
    override val component: Component
        get() = baseBot.component
    
    override fun isMe(id: ID): Boolean = baseBot.isMe(id)
    
    override suspend fun start(): Boolean = baseBot.start()
    
    override val originalBot: Bot
        get() = baseBot.originalBot
    override val id: LongID
        get() = baseBot.id
    override val eventProcessor: EventProcessor
        get() = baseBot.eventProcessor
    override val logger: Logger
        get() = baseBot.logger
    override val manager: MiraiBotManager
        get() = baseBot.manager
    override val friends: Items<MiraiFriend>
        get() = baseBot.friends
    override val strangers: Items<MiraiStranger>
        get() = baseBot.strangers
    override val contacts: Items<MiraiContact>
        get() = baseBot.contacts
    override val groups: Items<MiraiGroup>
        get() = baseBot.groups
    
    override fun sendOnlyImage(resource: Resource, flash: Boolean): MiraiSendOnlyImage =
        baseBot.sendOnlyImage(resource, flash)
    
    override fun idImage(id: ID, flash: Boolean, builderAction: Image.Builder.() -> Unit): MiraiImage =
        baseBot.idImage(id, flash, builderAction)
    
    // endregion
    
    
    override fun hashCode(): Int {
        return (baseBot.hashCode() * 59) + member.hashCode()
    }
    
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MiraiGroupBotImpl) return false
        return other.baseBot == baseBot && other.member == member
    }
    
    
    companion object {
        fun MiraiGroupImpl.getGroupBot(bot: MiraiBotImpl): MiraiGroupBotImpl {
            val originalBotMember = originalContact.botAsMember
            return MiraiGroupBotImpl(bot, originalBotMember, this)
        }
        
    }
}

