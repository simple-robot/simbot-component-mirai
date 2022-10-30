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
import love.forte.simbot.*
import love.forte.simbot.action.DeleteSupport
import love.forte.simbot.component.mirai.bot.MiraiGroupBot
import love.forte.simbot.definition.*
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.utils.item.Items
import kotlin.time.Duration
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup


/**
 * Simbot中针对于 [OriginalMiraiGroup] 的群类型实现。
 *
 * ## [DeleteSupport]
 *
 * [MiraiGroup] 实现 [DeleteSupport] 来允许对bot退群的行为进行描述。更多参考 [delete]。
 *
 * @author ForteScarlet
 */
public interface MiraiGroup : Group, MiraiChatroom, DeleteSupport {
    override val originalContact: OriginalMiraiGroup
    
    override val bot: MiraiGroupBot
    override val id: LongID
    
    /**
     * 群主信息。
     */
    @JvmBlocking(asProperty = true, suffix = "")
    @JvmAsync(asProperty = true)
    override suspend fun owner(): MiraiMember
    
    /**
     * 群主ID。
     */
    override val ownerId: LongID
    
    /**
     * 获取群成员信息流。
     */
    override val members: Items<MiraiMember>
    
    /**
     * 获取群活跃度信息。
     * @see OriginalMiraiGroup.active
     */
    public val active: MiraiGroupActive
    
    /**
     * bot退群。
     *
     * 行为与 [OriginalMiraiGroup.quit] 一致：
     * > 让机器人退出这个群。
     *
     * @return 退出成功时 `true`; 已经退出时 `false`
     * @throws IllegalStateException 当bot为群主时
     *
     * @see OriginalMiraiGroup.quit
     */
    @JvmSynthetic
    override suspend fun delete(): Boolean = originalContact.quit()
    
    
    /**
     * 尝试禁言这个群。(即开启全群禁言。)
     *
     * 如果使用了有效的 [duration] 参数 (大于0)，则会在 bot 内开启一个伴随 bot 的作用域而存在的延时任务，
     * 提供基于内存的群禁言周期功能实现。
     *
     * 当 [duration] 的毫秒值小于等于0时（[Duration.ZERO]），代表无限期禁言。
     *
     */
    @JvmSynthetic
    override suspend fun mute(duration: Duration): Boolean
    
    /**
     * 尝试禁言这个群。(即开启全群禁言。)
     *
     * 如果使用了有效的 [duration] 参数(大于0)，则会在 bot 内开启一个伴随 bot 的作用域而存在的延时任务，
     * 提供基于内存的群禁言周期功能实现。
     *
     * 当 [duration] 的毫秒值小于等于0时（[Duration.ZERO]），代表无限期禁言。
     *
     */
    @Api4J
    override fun muteBlocking(duration: JavaDuration): Boolean
    
    /**
     * 尝试禁言这个群。(即开启全群禁言。)
     */
    @Api4J
    override fun muteBlocking(): Boolean
    
    
    /**
     * 取消全群禁言。[unmute] 的同时会取消此群涉及到的由 [mute] 构建出来的延时任务。
     */
    @JvmSynthetic
    override suspend fun unmute(): Boolean
    
    /**
     * 根据ID获取指定成员信息。
     */
    @JvmBlocking(baseName = "getMember", suffix = "")
    @JvmAsync(baseName = "getMember")
    override suspend fun member(id: ID): MiraiMember?
    
    /**
     * 向群内发送消息。
     */
    @JvmAsync
    @JvmBlocking
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup>
    
    /**
     * 向群内发送消息。
     */
    @JvmAsync
    @JvmBlocking
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup>
    
    
    /**
     * 向群内发送消息。
     */
    @JvmAsync
    @JvmBlocking
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiGroup> =
        send(message.messages)
    
    
    /**
     * Mirai中，一个群内可能出现的权限是固定的。
     *
     * @see MemberRole
     */
    override val roles: Items<MemberRole>
    override val icon: String get() = originalContact.avatarUrl
    override val name: String get() = originalContact.name
    override val createTime: Timestamp get() = Timestamp.NotSupport
    override val currentMember: Int get() = originalContact.members.size
    override val description: String get() = ""
    override val maximumMember: Int get() = -1
    
    
    /**
     * 群没有“上层”概念, 始终得到null。
     */
    @JvmSynthetic
    override suspend fun previous(): Organization? = null
    
}

