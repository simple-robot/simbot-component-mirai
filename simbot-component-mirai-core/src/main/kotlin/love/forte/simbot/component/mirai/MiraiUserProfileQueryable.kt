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

package love.forte.simbot.component.mirai

import love.forte.simbot.ID
import love.forte.simbot.IntID
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.data.UserProfile


/**
 * 用于表示一个可以获取（查询）到当前对象 [MiraiUserProfile] 的类型，通常为 mirai的 [User] 实现或可以转化为 [User] 的类型（例如 [mirai的Bot][Bot]）。
 *
 * @see love.forte.simbot.component.mirai.bot.MiraiBot
 * @see love.forte.simbot.component.mirai.MiraiFriend
 * @see love.forte.simbot.component.mirai.MiraiMember
 * @see love.forte.simbot.component.mirai.MiraiStranger
 * @author ForteScarlet
 */
public interface MiraiUserProfileQueryable {

    /**
     * 查询当前对象的 [MiraiUserProfile]。
     *
     * @see User.queryProfile
     */
    @JST
    public suspend fun queryProfile(): MiraiUserProfile
}

/**
 * Mirai中所表示的[用户详细信息][UserProfile]类型的封装类型.
 *
 * 可以通过 [originalProfile] 得到mirai原生的 [UserProfile] 实例。
 *
 * @see UserProfile
 *
 */
@MiraiMappingType(UserProfile::class)
public interface MiraiUserProfile {
    /**
     * 当前对象所表示的原生的 [UserProfile] 实例。
     */
    public val originalProfile: UserProfile

    /**
     * 用户昵称
     * @see UserProfile.nickname
     */
    public val nickname: String get() = originalProfile.nickname

    /**
     * 用户邮箱
     * @see UserProfile.email
     */
    public val email: String get() = originalProfile.email

    /**
     * 用户年龄
     * @see UserProfile.age
     */
    public val age: Int get() = originalProfile.age

    /**
     * 用户qq等级
     * @see UserProfile.qLevel
     */
    public val qLevel: Int get() = originalProfile.qLevel

    /**
     * 好友分组 ID, 在非好友情况下或者位于默认分组情况下为 `0`
     * @see UserProfile.friendGroupId
     */
    public val friendGroupId: IntID get() = originalProfile.friendGroupId.ID
    // friendGroupId 的获取基本应当不会很频繁, 随用随造即可

    /**
     * 个性签名
     * @see UserProfile.sign
     */
    public val sign: String get() = originalProfile.sign

    /**
     * 性别，直接使用mirai原生类型 [UserProfile.Sex].
     *
     * @see UserProfile.sex
     */
    public val sex: UserProfile.Sex get() = originalProfile.sex

}
