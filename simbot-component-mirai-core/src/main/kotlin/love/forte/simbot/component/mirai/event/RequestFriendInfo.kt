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

import love.forte.simbot.ID
import love.forte.simbot.definition.FriendInfo

/**
 * [MiraiFriendRequestEvent] 中的 [friend][MiraiFriendRequestEvent.friend] 属性返回值。
 *
 *  @see MiraiFriendRequestEvent
 */
public data class RequestFriendInfo(
    public val fromId: Long,
    public val fromGroupId: Long,
    public val fromGroupName: String?,
    public val fromNick: String,
) : FriendInfo {
    override val avatar: String get() = "https://q1.qlogo.cn/g?b=qq&nk=$fromId&s=640"
    override val id: ID = fromId.ID
    override val remark: String? get() = null
    override val username: String get() = fromNick
}

