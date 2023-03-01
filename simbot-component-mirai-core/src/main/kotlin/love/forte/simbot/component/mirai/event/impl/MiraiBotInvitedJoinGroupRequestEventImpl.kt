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

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.event.InvitorUserInfo
import love.forte.simbot.component.mirai.event.MiraiBotInvitedJoinGroupRequestEvent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.definition.GroupInfo
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent as OriginalMiraiBotInvitedJoinGroupRequestEvent

/**
 *
 * @author ForteScarlet
 */
internal class MiraiBotInvitedJoinGroupRequestEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiBotInvitedJoinGroupRequestEvent,
) : MiraiBotInvitedJoinGroupRequestEvent {
    override val id: ID = originalEvent.eventId.ID
    override val timestamp: Timestamp = Timestamp.now()
    private val _group: GroupInfo = InvitedJoinGroupInfo(originalEvent.groupId, originalEvent.groupName)
    private val _inviter: InvitorUserInfo = InvitorUserInfo(
        originalEvent.invitor,
        originalEvent.invitorId,
        originalEvent.invitorNick
    )
    
    override suspend fun group(): GroupInfo = _group
    override suspend fun inviter(): InvitorUserInfo = _inviter
    
    //// api
    
    
    override suspend fun accept(): Boolean {
        originalEvent.accept()
        return true
    }
    
    /**
     * 拒绝即代表忽略。
     *
     * @see OriginalMiraiBotInvitedJoinGroupRequestEvent
     */
    override suspend fun reject(): Boolean {
        originalEvent.ignore()
        return true
    }
    
    
}

private data class InvitedJoinGroupInfo(private val groupId: Long, private val groupName: String) : GroupInfo {
    override val id: ID = groupId.ID
    override val createTime: Timestamp get() = Timestamp.NotSupport
    override val currentMember: Int get() = -1
    override val description: String get() = ""
    override val icon: String
        get() = "https://p.qlogo.cn/gh/$groupId/$groupId/640"
    override val maximumMember: Int get() = -1
    override val name: String get() = groupName
    override val ownerId: ID get() = emptyID
    
    companion object {
        private val emptyID = "".ID
    }
}
