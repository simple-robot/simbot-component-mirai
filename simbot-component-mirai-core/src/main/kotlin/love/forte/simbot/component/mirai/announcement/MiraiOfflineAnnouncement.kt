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

import love.forte.simbot.component.mirai.MiraiMappingType
import love.forte.simbot.component.mirai.internal.announcement.asSimbot
import net.mamoe.mirai.contact.announcement.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 在本地构建的 [MiraiAnnouncement]。是 [mirai OfflineAnnouncement][OfflineAnnouncement] 的映射类型。
 * 更多说明参考 [mirai OfflineAnnouncement][OfflineAnnouncement]
 *
 *
 * @see OfflineAnnouncement
 */
@MiraiMappingType(OfflineAnnouncement::class)
public interface MiraiOfflineAnnouncement : MiraiAnnouncement {
    /**
     * 对应的原生 [OfflineAnnouncement] 实例。
     */
    override val originalAnnouncement: OfflineAnnouncement

    public companion object {

        /**
         * 创建 [MiraiOfflineAnnouncement]. 若 [announcement] 类型为 [MiraiOfflineAnnouncement] 则直接返回 [announcement].
         *
         * 若要转发获取到的公告到一个群, 可直接调用 [Announcement.publishTo] 而不需要构造 [MiraiOfflineAnnouncement].
         *
         * @see OfflineAnnouncement.from
         * @see OnlineAnnouncement.toOffline
         */
        @JvmStatic
        public fun from(announcement: MiraiAnnouncement): MiraiOfflineAnnouncement {
            return (announcement as? MiraiOfflineAnnouncement) ?: announcement.originalAnnouncement.toOffline().asSimbot()
        }

        /**
         * 创建 [MiraiOfflineAnnouncement]. 若 [announcement] 类型为 [MiraiOfflineAnnouncement] 则直接返回 [announcement].
         *
         * 若要转发获取到的公告到一个群, 可直接调用 [Announcement.publishTo] 而不需要构造 [MiraiOfflineAnnouncement].
         *
         * @see OfflineAnnouncement.from
         * @see OnlineAnnouncement.toOffline
         */
        @JvmStatic
        public fun from(announcement: Announcement): MiraiOfflineAnnouncement {
            return announcement.toOffline().asSimbot()
        }


        /**
         * 创建 [MiraiOfflineAnnouncement].
         * @param content 公告内容
         * @param parameters 可选的附加参数
         *
         * @see OfflineAnnouncement.create
         */
        @JvmOverloads
        @JvmStatic
        public fun create(
            content: String,
            parameters: AnnouncementParameters = AnnouncementParameters.DEFAULT
        ): MiraiOfflineAnnouncement = OfflineAnnouncement.create(content, parameters).asSimbot()


        /**
         * 创建 [AnnouncementParameters] 并创建 [MiraiOfflineAnnouncement].
         * @param content 公告内容
         * @param parameters 可选的附加参数
         *  @see OfflineAnnouncement.create
         * @see AnnouncementParametersBuilder
         */
        @OptIn(ExperimentalContracts::class)
        @JvmSynthetic
        public inline fun create(
            content: String,
            parameters: AnnouncementParametersBuilder.() -> Unit
        ): MiraiOfflineAnnouncement {
            contract { callsInPlace(parameters, InvocationKind.EXACTLY_ONCE) }
            return create(content, buildAnnouncementParameters(parameters))
        }

    }

}

