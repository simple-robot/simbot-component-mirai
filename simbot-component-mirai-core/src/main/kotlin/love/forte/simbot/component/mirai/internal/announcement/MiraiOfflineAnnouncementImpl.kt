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

import love.forte.simbot.component.mirai.announcement.MiraiOfflineAnnouncement
import net.mamoe.mirai.contact.announcement.OfflineAnnouncement


/**
 *
 * @author ForteScarlet
 */
internal data class MiraiOfflineAnnouncementImpl(override val originalAnnouncement: OfflineAnnouncement) : MiraiOfflineAnnouncement


internal fun OfflineAnnouncement.asSimbot() = MiraiOfflineAnnouncementImpl(this)
