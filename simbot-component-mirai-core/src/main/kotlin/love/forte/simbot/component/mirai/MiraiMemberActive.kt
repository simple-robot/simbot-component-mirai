/*
 *  Copyright (c) 2022-2022 ForteScarlet <ForteScarlet@163.com>
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

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import net.mamoe.mirai.contact.active.MemberActive
import net.mamoe.mirai.contact.active.MemberMedalInfo
import net.mamoe.mirai.data.GroupHonorType

/**
 * 针对于 [MemberActive] 而提供的封装类型，通过 [MiraiMember.active] 获取。
 *
 * > 群活跃度相关属性
 *
 * @see MemberActive
 */
public interface MiraiMemberActive {
    
    /**
     * 当前类型内使用的原始的 [MemberActive] 实例。
     */
    public val originalMemberActive: MemberActive
    
    /**
     * 群活跃等级.
     *
     * @see MemberActive.rank
     */
    public val rank: Int get() = originalMemberActive.rank
    
    /**
     * 群活跃积分.
     *
     * @see MemberActive.point
     */
    public val point: Int get() = originalMemberActive.point
    
    /**
     * 群荣誉标识.
     * @see MemberActive.honors
     */
    public val honors: Set<GroupHonorType> get() = originalMemberActive.honors
    
    /**
     * 群荣誉等级. 取值为 1~100 (包含)
     *
     * @see MemberActive.temperature
     */
    public val temperature: Int get() = originalMemberActive.temperature
    
    /**
     * 查询头衔佩戴情况.
     *
     * @return Mirai原生的 [MemberMedalInfo] 类结果。
     * @see MemberActive.queryMedal
     */
    @JvmBlocking
    @JvmAsync
    public suspend fun queryMedal(): MemberMedalInfo = originalMemberActive.queryMedal()
    
    
}


