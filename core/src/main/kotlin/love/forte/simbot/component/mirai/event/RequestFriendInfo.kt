package love.forte.simbot.component.mirai.event

import love.forte.simbot.Grouping
import love.forte.simbot.ID
import love.forte.simbot.definition.FriendInfo

/**
 * [MiraiFriendRequestEvent] 中的 [friend][MiraiFriendRequestEvent.friend] 属性返回值。
 *
 *  @see NativeMiraiNewFriendRequestEvent
 */
public data class RequestFriendInfo(
    public val fromId: Long,
    public val fromGroupId: Long,
    public val fromGroupName: String?,
    public val fromNick: String
) : FriendInfo {
    override val avatar: String get() = "https://q1.qlogo.cn/g?b=qq&nk=$fromId&s=640"

    /**
     * 如果存在 [fromGroupId], 则此处代表申请来源的群号，如果 [fromGroupId] 为 `0`, 则此处为 [Grouping.EMPTY].
     */
    override val grouping: Grouping =
        if (fromGroupId != 0L) Grouping(fromGroupId.ID, fromGroupName ?: "") else Grouping.EMPTY
    override val id: ID = fromId.ID
    override val remark: String? get() = null
    override val username: String get() = fromNick
}

