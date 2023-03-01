/*
 *  Copyright (c) 2023-2023 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU Affero通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
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
