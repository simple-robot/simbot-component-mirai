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

package love.forte.simbot.component.mirai

import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.IntID
import love.forte.simbot.LongID
import love.forte.simbot.action.DeleteSupport
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.definition.*
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend


/**
 *
 * 在simbot中 [OriginalMiraiFriend] 的表现形式。
 *
 * ### [DeleteSupport]
 *
 * mirai好友支持 [删除操作][DeleteSupport]. [delete] 相当于删除好友，等同于 [net.mamoe.mirai.contact.Friend.delete].
 *
 * @author ForteScarlet
 */
public interface MiraiFriend : Friend, MiraiContact, DeleteSupport, MiraiUserProfileQueryable {
    override val bot: MiraiBot
    override val id: LongID
    
    /**
     * mirai原生的好友对象。
     */
    override val originalContact: OriginalMiraiFriend
    
    // region send support
    
    /**
     * 向此好友发送消息。
     */
    @JST
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    
    /**
     * 向此好友发送消息。
     */
    @JST
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    
    /**
     * 向此好友发送消息。
     */
    @JST
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend> =
        send(message.messages)
    
    // endregion
    
    /**
     * 同 [net.mamoe.mirai.contact.Friend.delete], 删除当前好友。
     *
     * @see net.mamoe.mirai.contact.Friend.delete
     * @return always true.
     */
    @JvmSynthetic
    override suspend fun delete(): Boolean {
        originalContact.delete()
        return true
    }
    
    /**
     * 头像信息。
     */
    override val avatar: String get() = originalContact.avatarUrl
    
    /**
     * 好友用户名
     * @see OriginalMiraiFriend.nick
     */
    override val username: String get() = originalContact.nick
    
    /**
     * 好友的分组信息。
     *
     * @see OriginalMiraiFriend.friendGroup
     */
    override val category: MiraiFriendCategory
    
    /**
     * 好友备注信息
     * @see OriginalMiraiFriend.remark
     */
    override var remark: String?
        get() = originalContact.remark.takeIf { it.isNotEmpty() }
        set(value) {
            originalContact.remark = value ?: ""
        }
}

/**
 * mirai中的好友分组信息
 *
 */
public interface MiraiFriendCategory : Category, DeleteSupport {
    /**
     * 获取mirai原生的 [好友分组][FriendGroup]。
     */
    public val originalFriendGroup: FriendGroup
    
    /**
     * 分组ID
     * @see FriendGroup.id
     */
    override val id: IntID
    
    /**
     * 分组名
     * @see FriendGroup.name
     */
    override val name: String get() = originalFriendGroup.name
    
    /**
     * 分组内好友数量
     *
     * @see FriendGroup.count
     */
    public val count: Int get() = originalFriendGroup.count
    
    /**
     * 属于本分组的好友集合。
     *
     * @see FriendGroup.friends
     */
    @ExperimentalSimbotApi
    public val friends: Collection<MiraiFriend>
    
    /**
     * 修改分组名称。
     * @see FriendGroup.renameTo
     */
    @JST
    public suspend fun renameTo(newName: String): Boolean {
        return originalFriendGroup.renameTo(newName)
    }
    
    /**
     * 把一名好友移动至本分组内.
     * @see FriendGroup.moveIn
     */
    @JST
    public suspend fun moveIn(friend: MiraiFriend): Boolean {
        return originalFriendGroup.moveIn(friend.originalContact)
    }
    
    /**
     * 把一名好友移动至本分组内.
     * @see FriendGroup.moveIn
     */
    @JST
    public suspend fun moveIn(friend: OriginalMiraiFriend): Boolean {
        return originalFriendGroup.moveIn(friend)
    }
    
    /**
     * 删除本分组.
     * @see FriendGroup.delete
     */
    @JvmSynthetic
    override suspend fun delete(): Boolean {
        return originalFriendGroup.delete()
    }
    
}
