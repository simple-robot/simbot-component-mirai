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
 */

package love.forte.simbot.component.mirai

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
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
public interface MiraiFriend : Friend, MiraiContact, DeleteSupport {
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
    @JvmAsync
    @JvmBlocking
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    
    /**
     * 向此好友发送消息。
     */
    @JvmAsync
    @JvmBlocking
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    
    /**
     * 向此好友发送消息。
     */
    @JvmAsync
    @JvmBlocking
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
    @JvmBlocking
    @JvmAsync
    public suspend fun renameTo(newName: String): Boolean {
        return originalFriendGroup.renameTo(newName)
    }
    
    /**
     * 把一名好友移动至本分组内.
     * @see FriendGroup.moveIn
     */
    @JvmBlocking
    @JvmAsync
    public suspend fun moveIn(friend: MiraiFriend): Boolean {
        return originalFriendGroup.moveIn(friend.originalContact)
    }
    
    /**
     * 把一名好友移动至本分组内.
     * @see FriendGroup.moveIn
     */
    @JvmBlocking
    @JvmAsync
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