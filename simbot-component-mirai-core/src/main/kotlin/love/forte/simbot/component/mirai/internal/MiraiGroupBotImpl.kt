/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  This file is part of simbot-component-mirai.
 *
 *  simbot-component-mirai is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  simbot-component-mirai is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with simbot-component-mirai. If not, see <https://www.gnu.org/licenses/>.
 *
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

    override val bot: MiraiBotImpl
        get() = baseBot

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

