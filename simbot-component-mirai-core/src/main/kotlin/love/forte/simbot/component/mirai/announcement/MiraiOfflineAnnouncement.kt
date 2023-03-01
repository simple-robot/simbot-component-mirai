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

