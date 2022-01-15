package love.forte.simbot.component.mirai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.IntID
import love.forte.simbot.component.mirai.MiraiRole.*
import love.forte.simbot.definition.Permission
import love.forte.simbot.definition.PermissionStatus
import love.forte.simbot.definition.Role
import net.mamoe.mirai.contact.MemberPermission

/**
 * 在Mirai中，也就是在QQ中，只有三种角色：
 * - [群主][OWNER]
 * - [管理员][ADMINISTRATOR]
 * - [成员][MEMBER]
 *
 * @property nativeMiraiPermission Mirai中的 [MemberPermission] 类型。
 */
@Suppress("MemberVisibilityCanBePrivate")
public enum class MiraiRole(
    public val nativeMiraiPermission: MemberPermission,
    public val permission: MiraiPermission
) : Role {

    OWNER(MemberPermission.OWNER, MiraiPermission.OWNER),
    ADMINISTRATOR(MemberPermission.ADMINISTRATOR, MiraiPermission.ADMINISTRATOR),
    MEMBER(MemberPermission.MEMBER, MiraiPermission.MEMBER),
    ;

    /**
     * ID, 等同于 [MemberPermission.level].
     */
    override val id: IntID = nativeMiraiPermission.level.ID


    /**
     * 权限列表。在Mirai中实际上的权限，一种角色只有一个。
     */
    @OptIn(Api4J::class)
    override val permissions: List<MiraiPermission> = listOf(permission)

    /**
     * 权限列表。在Mirai中实际上的权限，一种角色只有一个。
     */
    override suspend fun permissions(): Flow<MiraiPermission> = permissions.asFlow()

}


public inline val MemberPermission.simbotRole: MiraiRole
    get() =
        when (this) {
            MemberPermission.OWNER -> OWNER
            MemberPermission.MEMBER -> MEMBER
            MemberPermission.ADMINISTRATOR -> ADMINISTRATOR
        }

public inline val NativeMiraiMember.simbotRole: MiraiRole
    get() = permission.simbotRole


/**
 * Mirai中成员权限对应的 [Permission].
 */
@Suppress("MemberVisibilityCanBePrivate")
public enum class MiraiPermission(
    @Suppress("CanBeParameter")
    public val miraiPermission: MemberPermission,
    override val status: PermissionStatus
) : Permission {
    OWNER(
        MemberPermission.OWNER,
        PermissionStatus.builder()
            .owner()
            .admin()
            .superAdmin()
            .memberAdmin()
            .build()
    ),
    ADMINISTRATOR(
        MemberPermission.ADMINISTRATOR,
        PermissionStatus.builder()
            .admin()
            .memberAdmin()
            .build()
    ),
    MEMBER(
        MemberPermission.MEMBER,
        PermissionStatus.builder().build()
    ),
    ;

    override val id: IntID = miraiPermission.level.ID
}