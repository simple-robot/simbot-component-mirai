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

import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.announcement.MiraiAnnouncements
import love.forte.simbot.component.mirai.internal.MiraiGroupBotImpl.Companion.getGroupBot
import love.forte.simbot.component.mirai.internal.announcement.MiraiAnnouncementsImpl
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.utils.item.Items
import love.forte.simbot.utils.item.Items.Companion.asItems
import love.forte.simbot.utils.item.map
import net.mamoe.mirai.contact.GroupSettings
import net.mamoe.mirai.contact.active.GroupActive
import net.mamoe.mirai.contact.getMember
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup


/**
 *
 * @author ForteScarlet
 */
internal class MiraiGroupImpl(
    internal val baseBot: MiraiBotImpl,
    override val originalContact: OriginalMiraiGroup,
    initOwner: MiraiMemberImpl? = null,
) : MiraiGroup {
    private lateinit var memberBot: MiraiGroupBotImpl

    override val bot: MiraiGroupBotImpl
        get() {
            if (::memberBot.isInitialized) {
                return memberBot
            }

            // 不关心线程安全
            return getGroupBot(baseBot).also {
                memberBot = it
            }
        }

    override val id: LongID = originalContact.id.ID

    override val active: MiraiGroupActive
        get() = MiraiGroupActiveImpl(originalContact.active)

    override val settings: MiraiGroupSettings
        get() = MiraiGroupSettingsImpl(originalContact.settings)

    override val announcements: MiraiAnnouncements
        get() = MiraiAnnouncementsImpl(this, originalContact.announcements)

    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val receipt = originalContact.sendMessage(message.toOriginalMiraiMessage(originalContact))
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        return SimbotMiraiMessageReceiptImpl(originalContact.sendMessage(text))
    }

    override suspend fun member(id: ID): MiraiMember? {
        return originalContact.getMember(id.tryToLong())?.asSimbot(baseBot, this)
    }

    private val _owner = initOwner ?: originalContact.owner.asSimbot(baseBot, this)

    override suspend fun owner(): MiraiMember = _owner

    override val ownerId: LongID get() = _owner.id

    override val members: Items<MiraiMember>
        get() = originalContact.members.asItems().map { it.asSimbot(baseBot) }


    override val roles: Items<MemberRole>
        get() = MemberRole.values().asList().asItems()

    override suspend fun mute(duration: Duration): Boolean {
        baseBot.groupMute(originalContact, duration.inWholeMilliseconds)
        return true
    }

    override suspend fun mute(time: Long, timeUnit: TimeUnit): Boolean {
        baseBot.groupMute(originalContact, timeUnit.toMillis(time))
        return true
    }

    @Api4J
    override fun muteBlocking(duration: JavaDuration): Boolean {
        baseBot.groupMute(originalContact, duration.toMillis())
        return true
    }

    @Api4J
    override fun muteBlocking(): Boolean {
        baseBot.groupMute(originalContact, 0)
        return true
    }

    override suspend fun unmute(): Boolean {
        return baseBot.groupUnmute(originalContact)
    }

}

internal fun OriginalMiraiGroup.asSimbot(bot: MiraiBotImpl): MiraiGroupImpl =
    bot.computeGroup(this) { MiraiGroupImpl(bot, this) }

internal fun OriginalMiraiGroup.asSimbot(bot: MiraiBotImpl, initOwner: MiraiMemberImpl): MiraiGroupImpl =
    bot.computeGroup(this) { MiraiGroupImpl(bot, this, initOwner) }

internal class MiraiGroupActiveImpl(override val originalGroupActive: GroupActive) : MiraiGroupActive {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MiraiGroupActiveImpl

        if (originalGroupActive != other.originalGroupActive) return false

        return true
    }

    override fun hashCode(): Int {
        return originalGroupActive.hashCode()
    }

    override fun toString(): String {
        return "MiraiGroupActiveImpl(originalGroupActive=$originalGroupActive)"
    }


}


internal class MiraiGroupSettingsImpl(override val originalGroupSettings: GroupSettings) : MiraiGroupSettings {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MiraiGroupSettingsImpl

        if (originalGroupSettings != other.originalGroupSettings) return false

        return true
    }

    override fun hashCode(): Int {
        return originalGroupSettings.hashCode()
    }

    override fun toString(): String {
        return "MiraiGroupSettingsImpl(originalGroupSettings=$originalGroupSettings)"
    }


}


