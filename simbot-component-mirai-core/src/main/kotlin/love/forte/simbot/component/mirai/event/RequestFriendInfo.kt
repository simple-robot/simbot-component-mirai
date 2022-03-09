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

package love.forte.simbot.component.mirai.event

import love.forte.simbot.*
import love.forte.simbot.definition.*

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
