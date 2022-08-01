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

import love.forte.simbot.Api4J
import love.forte.simbot.LongID
import love.forte.simbot.Timestamp
import love.forte.simbot.action.DeleteSupport
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.definition.GroupMember
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.utils.item.Items
import love.forte.simbot.utils.runInBlocking
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.PermissionDeniedException
import kotlin.time.Duration
import net.mamoe.mirai.contact.Member as OriginalMiraiMember
import net.mamoe.mirai.contact.NormalMember as OriginalMiraiNormalMember


/**
 * 一个由simbot包装为 [GroupMember] 的 [OriginalMiraiMember] 对象。
 *
 * ### [DeleteSupport]
 * 一个 mirai 的群成员是 [支持删除][DeleteSupport] 操作的. [delete] 行为相当于 [踢出][net.mamoe.mirai.contact.NormalMember.kick] 操作。
 *
 * 当 [originalContact] 的类型不是 [OriginalMiraiNormalMember] 的时候，[delete] 行为将会无效。
 *
 * @see OriginalMiraiMember
 * @author ForteScarlet
 */
public interface MiraiMember : GroupMember, MiraiContact, DeleteSupport {
    
    override val originalContact: OriginalMiraiMember
    
    override val bot: MiraiBot
    override val id: LongID
    
    @OptIn(Api4J::class)
    override val group: MiraiGroup
    
    /**
     * 向此群成员发送消息。
     */
    @JvmSynthetic
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    
    /**
     * 向此群成员发送消息。
     */
    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    
    
    //// Impl
    
    
    // region send support
    /**
     * 向此群成员发送消息。
     */
    @JvmSynthetic
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember> =
        send(message.messages)
    
    /**
     * 向此群成员发送消息。
     */
    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember> =
        runInBlocking { send(text) }
    
    /**
     * 向此群成员发送消息。
     */
    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember> =
        runInBlocking { send(message) }
    
    /**
     * 向此群成员发送消息。
     */
    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember> =
        runInBlocking { send(message) }
    // endregion
    
    
    /**
     * 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @param message 踢出时提供的消息。可能无实际意义。
     * @param block 是否踢出后加入黑名单。
     * @throws PermissionDeniedException 无权限修改时. see [net.mamoe.mirai.contact.NormalMember.kick].
     * @return 是否为普通成员且踢出执行成功。
     */
    @JvmSynthetic
    public suspend fun kick(message: String, block: Boolean): Boolean
    
    /**
     * 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @param message 踢出时提供的消息。可能无实际意义。
     * @throws PermissionDeniedException 无权限修改时. see [net.mamoe.mirai.contact.NormalMember.kick].
     * @return 是否为普通成员且踢出执行成功。
     */
    @JvmSynthetic
    public suspend fun kick(message: String): Boolean = kick("", false)
    
    /**
     * 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @param message 踢出时提供的消息。可能无实际意义。
     * @param block 是否踢出后加入黑名单。
     * @throws PermissionDeniedException 无权限修改时. see [net.mamoe.mirai.contact.NormalMember.kick].
     * @return 是否为普通成员且踢出执行成功。
     */
    @Api4J
    public fun kickBlocking(message: String, block: Boolean): Boolean = runInBlocking { kick(message, block) }
    
    /**
     * 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @param message 踢出时提供的消息。可能无实际意义。
     * @throws PermissionDeniedException 无权限修改时. see [net.mamoe.mirai.contact.NormalMember.kick].
     * @return 是否为普通成员且踢出执行成功。
     */
    @Api4J
    public fun kickBlocking(message: String): Boolean = runInBlocking { kick(message) }
    
    /**
     * 同 [kick], 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @see kick
     */
    @JvmSynthetic
    override suspend fun delete(): Boolean = kick("")
    
    /**
     * 同 [kick], 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @see kick
     */
    @Api4J
    override fun deleteBlocking(): Boolean = runInBlocking { kick("") }
    
    
    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group
    
    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group
    
    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group
    
    
    @JvmSynthetic
    override suspend fun mute(duration: Duration): Boolean {
        val second = duration.inWholeSeconds.toInt()
        return if (second > 0) {
            originalContact.mute(duration.inWholeSeconds.toInt())
            true
        } else {
            false
        }
    }
    
    @JvmSynthetic
    override suspend fun unmute(): Boolean {
        (originalContact as? NormalMember)?.unmute()
        return true
    }
    
    /**
     * 当前成员角色所属角色。通常内部只有一个元素。
     */
    override val roles: Items<MemberRole>
    
    
    //// Impl
    
    override val joinTime: Timestamp get() = Timestamp.NotSupport
    override val nickname: String get() = originalContact.nameCard
    override val avatar: String get() = originalContact.avatarUrl
    override val username: String get() = originalContact.nick
}