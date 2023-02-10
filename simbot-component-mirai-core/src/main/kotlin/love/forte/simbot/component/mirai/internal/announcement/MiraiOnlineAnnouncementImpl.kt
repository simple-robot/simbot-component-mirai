/*
 *  Copyright (c) 2023-2023 ForteScarlet <ForteScarlet@163.com>
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
