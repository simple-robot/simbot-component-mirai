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
 *
 */

package love.forte.simbot.component.mirai

import love.forte.simbot.ID
import love.forte.simbot.IntID
import love.forte.simbot.component.mirai.MemberRole.*
import love.forte.simbot.definition.Role
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.isOwner

/**
 * @see net.mamoe.mirai.contact.MemberPermission
 */
public typealias NativeMiraiMemberPermission = net.mamoe.mirai.contact.MemberPermission

/**
 * 在Mirai中，也就是在QQ群中，只有三种角色：
 * - [群主][OWNER]
 * - [管理员][ADMINISTRATOR]
 * - [成员][MEMBER]
 *
 */
@Suppress("MemberVisibilityCanBePrivate")
public enum class MemberRole(
    public val nativeMiraiPermission: NativeMiraiMemberPermission,
) : Role {

    MEMBER(NativeMiraiMemberPermission.MEMBER),
    ADMINISTRATOR(NativeMiraiMemberPermission.ADMINISTRATOR),
    OWNER(NativeMiraiMemberPermission.OWNER),
    ;

    /**
     * ID, 等同于 [net.mamoe.mirai.contact.MemberPermission.level].
     */
    override val id: IntID = nativeMiraiPermission.level.ID

    override val isAdmin: Boolean
        get() = nativeMiraiPermission.isOperator()

    override val isOwner: Boolean
        get() = nativeMiraiPermission.isOwner()

    //
    // /**
    //  * 权限列表。在Mirai中实际上的权限，一种角色只有一个。
    //  */
    // @OptIn(Api4J::class)
    // override val permissions: List<MemberPermission> = listOf(permission)
    //
    // /**
    //  * 权限列表。在Mirai中实际上的权限，一种角色只有一个。
    //  */
    // @JvmSynthetic
    // override suspend fun permissions(): Flow<MemberPermission> = permissions.asFlow()

}


public inline val NativeMiraiMemberPermission.simbotRole: MemberRole
    get() =
        when (this) {
            NativeMiraiMemberPermission.MEMBER -> MEMBER
            NativeMiraiMemberPermission.ADMINISTRATOR -> ADMINISTRATOR
            NativeMiraiMemberPermission.OWNER -> OWNER
        }

public inline val NativeMiraiMember.simbotRole: MemberRole
    get() = permission.simbotRole
