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

package love.forte.simbot.component.mirai.announcement

import kotlinx.coroutines.flow.Flow
import love.forte.simbot.*
import love.forte.simbot.component.mirai.JST
import love.forte.simbot.component.mirai.MiraiMappingType
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.util.toExternalResource
import love.forte.simbot.resources.Resource
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.announcement.*
import net.mamoe.mirai.utils.ExternalResource
import java.util.stream.Stream


/**
 * 表示一个群的公告列表 (管理器)，相当于 [mirai Announcements][Announcements]。
 *
 * API大多与 [mirai Announcements][Announcements] 相对应，部分参数和类型会被替换为simbot或组件中的类型。
 *
 * @author ForteScarlet
 *
 * @see Announcements
 */
@JST
@MiraiMappingType(Announcements::class)
public interface MiraiAnnouncements {

    /**
     * 得到当前所对应的 [mirai Announcements][Announcements]
     */
    public val originalAnnouncements: Announcements

    /**
     * 以流的形式获取所有的公告。
     *
     * @see Announcements.asFlow
     */
    public val announcementFlow: Flow<MiraiOnlineAnnouncement>

    /**
     * 以流的形式获取所有的公告。
     *
     * @see Announcements.asStream
     */
    @Api4J
    public val announcementStream: Stream<MiraiOnlineAnnouncement>

    /**
     * 删除一条群公告. 需要管理员权限. 使用 [MiraiOnlineAnnouncement.delete] 与此方法效果相同.
     *
     * @param fid 公告的 [MiraiOnlineAnnouncement.fid]
     * @return 成功返回 `true`, 群公告不存在时返回 `false`
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see Announcements.delete
     */
    public suspend fun delete(fid: ID): Boolean = delete(fid.literal)


    /**
     * 删除一条群公告. 需要管理员权限. 使用 [MiraiOnlineAnnouncement.delete] 与此方法效果相同.
     *
     * @param fid 公告的 [MiraiOnlineAnnouncement.fid]。
     * @return 成功返回 `true`, 群公告不存在时返回 `false`
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see Announcements.delete
     */
    public suspend fun delete(fid: String): Boolean = originalAnnouncements.delete(fid)

    /**
     * 获取一条群公告.
     * @param fid 公告的 [OnlineAnnouncement.fid]
     * @return 返回 `null` 表示不存在该 [fid] 的群公告
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see Announcements.get
     */
    public suspend fun get(fid: String): MiraiOnlineAnnouncement?

    /**
     * 在该群发布群公告并获得 [OnlineAnnouncement], 需要管理员权限. 发布公告后群内将会出现 "有新公告" 系统提示.
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see Announcements.publish
     * @see Announcement.publishTo
     */
    public suspend fun publish(announcement: MiraiAnnouncement): MiraiOnlineAnnouncement {
        return publish(announcement.originalAnnouncement)
    }

    /**
     * 在该群发布群公告并获得 [OnlineAnnouncement], 需要管理员权限. 发布公告后群内将会出现 "有新公告" 系统提示.
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see Announcements.publish
     * @see Announcement.publishTo
     */
    public suspend fun publish(announcement: Announcement): MiraiOnlineAnnouncement

    /**
     * 上传资源作为群公告图片. 返回值可用于 [AnnouncementParameters.image].
     *
     * **注意**: 需要由调用方[关闭][ExternalResource.close] [resource].
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see Announcements.uploadImage
     */
    public suspend fun uploadImage(resource: ExternalResource): AnnouncementImage

    /**
     * 上传资源作为群公告图片. 返回值可用于 [AnnouncementParameters.image].
     *
     * **注意**: 需要由调用方[关闭][ExternalResource.close] [resource].
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see Announcements.uploadImage
     */
    public suspend fun uploadImage(resource: Resource): AnnouncementImage {
        return resource.toExternalResource().use { uploadImage(it) }
    }

    /**
     * 获取 已确认/未确认 的群成员
     *
     * @param fid 公告的 [MiraiOnlineAnnouncement.fid]
     * @param confirmed 是否确认
     * @return 群成员列表
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineAnnouncement.members
     */
    public suspend fun members(fid: ID, confirmed: Boolean): List<MiraiMember> = members(fid.literal, confirmed)

    /**
     * 获取 已确认/未确认 的群成员
     *
     * @param fid 公告的 [MiraiOnlineAnnouncement.fid]
     * @param confirmed 是否确认
     * @return 群成员列表
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineAnnouncement.members
     */
    public suspend fun members(fid: String, confirmed: Boolean): List<MiraiMember>

    /**
     * 提醒 未确认 的群成员
     *
     * @param fid 公告的 [MiraiOnlineAnnouncement.fid]
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineAnnouncement.remind
     */
    public suspend fun remind(fid: ID) {
        remind(fid.literal)
    }

    /**
     * 提醒 未确认 的群成员
     *
     * @param fid 公告的 [MiraiOnlineAnnouncement.fid]
     *
     * @throws PermissionDeniedException 当没有权限时抛出
     * @throws IllegalStateException 当协议异常时抛出
     *
     * @see OnlineAnnouncement.remind
     */
    public suspend fun remind(fid: String) {
        originalAnnouncements.remind(fid)
    }


}


