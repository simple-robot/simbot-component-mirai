/*
 *  Copyright (c) 2023-2023 ForteScarlet.
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

package love.forte.simbot.component.mirai.announcement

import love.forte.simbot.component.mirai.JST
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMappingType
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.announcement.*

/**
 * 表示一个 (群) 公告. 是 [mirai Announcement][Announcement] 的映射类型。
 * 更多说明参考 [mirai Announcement][Announcement]。
 *
 * @see Announcement
 * @see MiraiOnlineAnnouncement
 * @see MiraiOfflineAnnouncement
 */
@JST
@MiraiMappingType(Announcement::class)
public interface MiraiAnnouncement {
    /**
     * 对应的 [Announcement] 实例。
     */
    public val originalAnnouncement: Announcement

    /**
     * 内容
     */
    public val content: String get() = originalAnnouncement.content

    /**
     * 附加参数. 可以通过 [AnnouncementParametersBuilder] 构建获得.
     *
     * @see AnnouncementParameters
     * @see AnnouncementParametersBuilder
     */
    public val parameters: AnnouncementParameters get() = originalAnnouncement.parameters


    /**
     * 在该群发布群公告并获得 [OnlineAnnouncement], 需要管理员权限. 发布公告后群内将会出现 "有新公告" 系统提示.
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see Announcement.publishTo
     */
    public suspend fun publishTo(group: MiraiGroup): MiraiOnlineAnnouncement = group.announcements.publish(this)


}


