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
