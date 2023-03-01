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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import love.forte.simbot.Api4J
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.announcement.MiraiAnnouncements
import love.forte.simbot.component.mirai.announcement.MiraiOnlineAnnouncement
import love.forte.simbot.component.mirai.internal.MiraiGroupImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import net.mamoe.mirai.contact.announcement.Announcement
import net.mamoe.mirai.contact.announcement.AnnouncementImage
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.utils.ExternalResource
import java.util.stream.Stream


/**
 *
 * @author ForteScarlet
 */
internal class MiraiAnnouncementsImpl(
    private val group: MiraiGroupImpl,
    override val originalAnnouncements: Announcements,
) : MiraiAnnouncements {

    private val bot get() = group.baseBot

    override val announcementFlow: Flow<MiraiOnlineAnnouncement>
        get() = originalAnnouncements.asFlow().map { it.asSimbot(bot) }

    @Api4J
    override val announcementStream: Stream<MiraiOnlineAnnouncement>
        get() = originalAnnouncements.asStream().map { it.asSimbot(bot) }

    override suspend fun get(fid: String): MiraiOnlineAnnouncement? {
        return originalAnnouncements.get(fid)?.asSimbot(bot)
    }

    override suspend fun publish(announcement: Announcement): MiraiOnlineAnnouncement {
        return originalAnnouncements.publish(announcement).asSimbot(bot)
    }

    override suspend fun uploadImage(resource: ExternalResource): AnnouncementImage {
        return originalAnnouncements.uploadImage(resource)
    }

    override suspend fun members(fid: String, confirmed: Boolean): List<MiraiMember> {
        return originalAnnouncements.members(fid, confirmed).map { it.asSimbot(bot, group) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MiraiAnnouncementsImpl

        if (originalAnnouncements != other.originalAnnouncements) return false

        return true
    }

    override fun hashCode(): Int {
        return originalAnnouncements.hashCode()
    }

    override fun toString(): String {
        return "MiraiAnnouncementsImpl(originalAnnouncements=$originalAnnouncements)"
    }
}
