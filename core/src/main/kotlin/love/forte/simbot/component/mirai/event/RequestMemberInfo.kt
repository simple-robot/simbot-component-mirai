package love.forte.simbot.component.mirai.event

import love.forte.simbot.LongID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.definition.MemberInfo

/**
 * [MiraiMemberJoinRequestEvent] 中的 [requester][MiraiMemberJoinRequestEvent.requester] 等相关属性的返回值。
 * @see MiraiMemberJoinRequestEvent
 */
public data class RequestMemberInfo(
    override val id: LongID,
    public val fromGroupId: Long,
    public val fromGroupName: String?,
    public val fromNick: String
) : MemberInfo {
    override val joinTime: Timestamp get() = Timestamp.NotSupport
    override val avatar: String get() = "https://q1.qlogo.cn/g?b=qq&nk=$id&s=640"
    override val username: String get() = fromNick
    override val nickname: String get() = ""
}


/**
 * [MiraiMemberJoinRequestEvent] 中的 [requester][MiraiMemberJoinRequestEvent.inviter] 等相关属性的返回值。
 *
 * 如果在尝试获取此邀请人的时候，此人已经离开了群，那么 [member] 便可能为null。
 *
 * 当 [member] 为null的时候，只能获取到 [id], 而 [nickname] 和 [username] 只会得到空字符串。
 *
 * @see MiraiMemberJoinRequestEvent
 */
public data class RequestMemberInviterInfo(
    override val id: LongID,
    public val member: MiraiMember?
) : MemberInfo {
    override val joinTime: Timestamp get() = Timestamp.NotSupport
    override val avatar: String get() = "https://q1.qlogo.cn/g?b=qq&nk=$id&s=640"
    override val nickname: String get() = member?.nickname ?: ""
    override val username: String get() = member?.username ?: ""
}
