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


