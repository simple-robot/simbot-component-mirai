/*
 *  Copyright (c) 2023-2023 ForteScarlet.
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

package love.forte.simbot.component.mirai.internal.announcement

import love.forte.simbot.CharSequenceID
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.announcement.MiraiOnlineAnnouncement
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiGroupImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import net.mamoe.mirai.contact.announcement.OnlineAnnouncement


/**
 *
 * @author ForteScarlet
 */
internal class MiraiOnlineAnnouncementImpl(
    private val bot: MiraiBotImpl,
    override val originalAnnouncement: OnlineAnnouncement
) : MiraiOnlineAnnouncement {
    override val fid: CharSequenceID = originalAnnouncement.fid.ID
    override val group: MiraiGroupImpl = originalAnnouncement.group.asSimbot(bot)
    override val senderId: LongID = originalAnnouncement.senderId.ID
    override val sender: MiraiMember? = originalAnnouncement.sender?.asSimbot(bot, group)
    override val publicationTime: Timestamp = Timestamp.bySecond(originalAnnouncement.publicationTime)

    override suspend fun members(confirmed: Boolean): List<MiraiMember> {
        return originalAnnouncement.members(confirmed).map { it.asSimbot(bot, group) }
    }
}

internal fun OnlineAnnouncement.asSimbot(bot: MiraiBotImpl) = MiraiOnlineAnnouncementImpl(bot, this)
