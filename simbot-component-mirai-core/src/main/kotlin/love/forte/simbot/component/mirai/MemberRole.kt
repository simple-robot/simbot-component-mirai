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

import love.forte.simbot.ID
import love.forte.simbot.IntID
import love.forte.simbot.component.mirai.MemberRole.*
import love.forte.simbot.definition.Role
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.contact.Member as OriginalMiraiMember
import net.mamoe.mirai.contact.MemberPermission as MiraiMemberPermission


/**
 * 在mirai中（也就是在QQ群中），只有三种角色：
 * - [群主][OWNER]
 * - [管理员][ADMINISTRATOR]
 * - [成员][MEMBER]
 *
 */
@Suppress("MemberVisibilityCanBePrivate")
public enum class MemberRole(
    /**
     * 对应的原始 [MiraiMemberPermission] 类型。
     */
    public val originalMiraiPermission: MiraiMemberPermission,
) : Role, Comparable<MemberRole> {

    /** 群主 */
    MEMBER(MiraiMemberPermission.MEMBER),

    /** 管理员 */
    ADMINISTRATOR(MiraiMemberPermission.ADMINISTRATOR),

    /** 成员 */
    OWNER(MiraiMemberPermission.OWNER),
    ;

    /**
     * ID, 等同于 [net.mamoe.mirai.contact.MemberPermission.level].
     */
    override val id: IntID = originalMiraiPermission.level.ID

    /**
     * 判断是否为管理员或群主。等同于 [MiraiMemberPermission.isOperator]
     *
     * @see MiraiMemberPermission.isOperator
     *
     */
    override val isAdmin: Boolean
        get() = originalMiraiPermission.isOperator()

    /**
     * 判断是否为群主。等同于 [MiraiMemberPermission.isOwner]
     *
     * @see MiraiMemberPermission.isOwner
     */
    public val isOwner: Boolean
        get() = originalMiraiPermission.isOwner()

}


public inline val MiraiMemberPermission.simbotRole: MemberRole
    get() =
        when (this) {
            MiraiMemberPermission.MEMBER -> MEMBER
            MiraiMemberPermission.ADMINISTRATOR -> ADMINISTRATOR
            MiraiMemberPermission.OWNER -> OWNER
        }

public inline val OriginalMiraiMember.simbotRole: MemberRole
    get() = permission.simbotRole
