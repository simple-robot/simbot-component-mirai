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

package love.forte.simbot.component.mirai.announcement

import love.forte.simbot.CharSequenceID
import love.forte.simbot.LongID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.JST
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMappingType
import love.forte.simbot.component.mirai.MiraiMember
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.announcement.AnnouncementParameters
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.contact.announcement.OnlineAnnouncement

/**
 * 在线获取的、已经存在于服务器的公告，是 [mirai OnlineAnnouncement][OnlineAnnouncement] 的映射类型。
 *
 * @see OnlineAnnouncement
 */
@JST
@MiraiMappingType(OnlineAnnouncement::class)
public interface MiraiOnlineAnnouncement : MiraiAnnouncement {
    /**
     * 获取原始的 [mirai OnlineAnnouncement][OnlineAnnouncement] 实例。
     */
    override val originalAnnouncement: OnlineAnnouncement

    /**
     * 唯一识别属性
     */
    public val fid: CharSequenceID

    /**
     * 公告所属群
     */
    public val group: MiraiGroup

    /**
     * 公告发送者 `id`
     */
    public val senderId: LongID

    /**
     * 公告发送者. 当该成员已经离开群后为 `null`
     */
    public val sender: MiraiMember?

    /**
     * 所有人都已阅读, 如果 [AnnouncementParameters.requireConfirmation] 为 `true` 则为所有人都已确认.
     *
     * @see OnlineAnnouncement.allConfirmed
     */
    public val allConfirmed: Boolean
        get() = originalAnnouncement.allConfirmed

    /**
     * 已经阅读的成员数量，如果 [AnnouncementParameters.requireConfirmation] 为 `true` 则为已经确认的成员数量
     *
     * @see OnlineAnnouncement.confirmedMembersCount
     */
    public val confirmedMembersCount: Int
        get() = originalAnnouncement.confirmedMembersCount

    /**
     * 公告发出的时间。
     *
     * @see OnlineAnnouncement.publicationTime
     * @see java.time.Instant.ofEpochSecond
     */
    public val publicationTime: Timestamp

    /**
     * 删除这个公告. 需要管理员权限. 使用 [Announcements.delete] 与此方法效果相同.
     *
     * @return 成功返回 `true`, 群公告已被删除时返回 `false`
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineAnnouncement.delete
     * @see Announcements.delete
     */
    public suspend fun delete(): Boolean = originalAnnouncement.delete()

    /**
     * 获取 已确认/未确认 的群成员
     *
     * @param confirmed 是否确认
     * @return 群成员列表
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineAnnouncement.members
     * @see Announcements.members
     */
    public suspend fun members(confirmed: Boolean): List<MiraiMember>

    /**
     * 提醒 未确认 的群成员
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineAnnouncement.remind
     * @see Announcements.remind
     */
    public suspend fun remind() {
        originalAnnouncement.remind()
    }

}
