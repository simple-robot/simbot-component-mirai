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
