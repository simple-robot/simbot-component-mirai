/*
 *  Copyright (c) 2022-2023 ForteScarlet.
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
@MiraiMappingType(MemberActive::class)
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
    @JST
    public suspend fun queryMedal(): MemberMedalInfo = originalMemberActive.queryMedal()
    
    
}


