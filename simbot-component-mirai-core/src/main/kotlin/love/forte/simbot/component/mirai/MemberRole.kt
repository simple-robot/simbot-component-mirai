/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
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
