package love.forte.simbot.component.mirai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.IntID
import love.forte.simbot.component.mirai.MemberRole.*
import love.forte.simbot.definition.Permission
import love.forte.simbot.definition.PermissionStatus
import love.forte.simbot.definition.Role

public typealias NativeMiraiMemberPermission = net.mamoe.mirai.contact.MemberPermission

/**
 * 在Mirai中，也就是在QQ中，只有三种角色：
 * - [群主][OWNER]
 * - [管理员][ADMINISTRATOR]
 * - [成员][MEMBER]
 *
 * @property nativeMiraiPermission Mirai中的 [MemberPermission] 类型。
 */
@Suppress("MemberVisibilityCanBePrivate")
public enum class MemberRole(
    public val nativeMiraiPermission: NativeMiraiMemberPermission,
    public val permission: MemberPermission
) : Role {

    OWNER(NativeMiraiMemberPermission.OWNER, MemberPermission.OWNER),
    ADMINISTRATOR(NativeMiraiMemberPermission.ADMINISTRATOR, MemberPermission.ADMINISTRATOR),
    MEMBER(NativeMiraiMemberPermission.MEMBER, MemberPermission.MEMBER),
    ;

    /**
     * ID, 等同于 [net.mamoe.mirai.contact.MemberPermission.level].
     */
    override val id: IntID = nativeMiraiPermission.level.ID


    /**
     * 权限列表。在Mirai中实际上的权限，一种角色只有一个。
     */
    @OptIn(Api4J::class)
    override val permissions: List<MemberPermission> = listOf(permission)

    /**
     * 权限列表。在Mirai中实际上的权限，一种角色只有一个。
     */
    override suspend fun permissions(): Flow<MemberPermission> = permissions.asFlow()

}


public inline val NativeMiraiMemberPermission.simbotRole: MemberRole
    get() =
        when (this) {
            NativeMiraiMemberPermission.OWNER -> OWNER
            NativeMiraiMemberPermission.MEMBER -> MEMBER
            NativeMiraiMemberPermission.ADMINISTRATOR -> ADMINISTRATOR
        }

public inline val NativeMiraiMember.simbotRole: MemberRole
    get() = permission.simbotRole


/**
 * Mirai中成员权限对应的 [Permission].
 */
@Suppress("MemberVisibilityCanBePrivate")
public enum class MemberPermission(
    @Suppress("CanBeParameter")
    public val nativePermission: NativeMiraiMemberPermission,
    override val status: PermissionStatus
) : Permission {
    OWNER(
        NativeMiraiMemberPermission.OWNER,
        PermissionStatus.builder()
            .owner()
            .admin()
            .superAdmin()
            .memberAdmin()
            .build()
    ),
    ADMINISTRATOR(
        NativeMiraiMemberPermission.ADMINISTRATOR,
        PermissionStatus.builder()
            .admin()
            .memberAdmin()
            .build()
    ),
    MEMBER(
        NativeMiraiMemberPermission.MEMBER,
        PermissionStatus.builder().build()
    ),
    ;

    override val id: IntID = nativePermission.level.ID
}