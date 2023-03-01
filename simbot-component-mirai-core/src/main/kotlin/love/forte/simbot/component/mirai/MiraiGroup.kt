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

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.simbot.*
import love.forte.simbot.action.DeleteSupport
import love.forte.simbot.action.UnsupportedActionException
import love.forte.simbot.component.mirai.announcement.MiraiAnnouncements
import love.forte.simbot.component.mirai.bot.MiraiGroupBot
import love.forte.simbot.component.mirai.message.MiraiMessageChainContent
import love.forte.simbot.definition.*
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.utils.item.Items
import net.mamoe.mirai.contact.Group.Companion.setEssenceMessage
import net.mamoe.mirai.contact.GroupSettings
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.MiraiExperimentalApi
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
    /**
     * 得到对应的 [mirai Group][OriginalMiraiGroup].
     */
    override val originalContact: OriginalMiraiGroup

    /**
     * 在这个群中的bot自身
     */
    override val bot: MiraiGroupBot

    /**
     * 群号
     */
    override val id: LongID

    /**
     * 群头像链接
     */
    override val icon: String get() = originalContact.avatarUrl

    /**
     * 群名称
     */
    override val name: String get() = originalContact.name

    /**
     * 不支持，将会始终的得到 [Timestamp.NotSupport].
     */
    @Deprecated("Unsupported", ReplaceWith("Timestamp.NotSupport", "love.forte.simbot.Timestamp"))
    override val createTime: Timestamp get() = Timestamp.NotSupport

    /**
     * 当前群人数
     */
    override val currentMember: Int get() = originalContact.members.size

    /**
     * 不支持，将会始终得到空字符串。
     */
    @Deprecated("Unsupported", ReplaceWith("\"\""))
    override val description: String get() = ""

    /**
     * 不支持，将会始终得到 `-1`。
     */
    @Deprecated("Unsupported", ReplaceWith("-1"))
    override val maximumMember: Int get() = -1

    /**
     * 群主ID。
     */
    override val ownerId: LongID

    /**
     * 群主信息。
     */
    @JSTP
    override suspend fun owner(): MiraiMember

    /**
     * 获取群成员信息流。
     */
    override val members: Items<MiraiMember>

    /**
     * Mirai中，一个群内可能出现的权限是固定的。
     *
     * @see MemberRole
     */
    override val roles: Items<MemberRole>

    /**
     * 获取群活跃度信息。
     *
     * 类似于 [OriginalMiraiGroup.active]
     *
     * @see OriginalMiraiGroup.active
     */
    public val active: MiraiGroupActive

    /**
     * 获取群设置信息。
     *
     * 类似于 [OriginalMiraiGroup.settings]
     *
     * @see OriginalMiraiGroup.settings
     */
    public val settings: MiraiGroupSettings

    /**
     * 获取群公告列表（管理器）。
     *
     * 类似于 [OriginalMiraiGroup.announcements]
     *
     * @see OriginalMiraiGroup.announcements
     */
    public val announcements: MiraiAnnouncements

    /**
     * 让bot退出这个群。
     *
     * 行为与 [OriginalMiraiGroup.quit] 一致：
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
    @JST
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup>


    /**
     * 向群内发送消息。
     */
    @JST
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup>


    /**
     * 向群内发送消息。
     */
    @JST
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiGroup> =
        send(message.messages)


    /**
     * 将一个消息设置为群精华消息, 需要管理员或群主权限。
     *
     * 作为参数的 [message] 必须为 [MiraiMessageChainContent] 类型，否则会引发 [ClassCastException].
     *
     * ### 可靠性
     *
     * [setEssenceMessage] 的参数类型为了保证一定程度的兼容性而使用了 [MessageContent],
     * 这会导致此函数内存在一些隐患：如 [message] 的实际类型不符合预期或 [messageSource][MiraiMessageChainContent.messageSourceOrNull] 不存在
     * （例如 [message] 为 [MiraiReceivedNudgeMessageContent][love.forte.simbot.component.mirai.event.MiraiReceivedNudgeMessageContent] 类型时）,
     *
     * 如果希望使用相对而言更可靠的API，[MiraiGroupMessageEvent.setAsEssenceMessage][love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent.setAsEssenceMessage]
     * 是一个不错的选择。
     *
     * @see love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent.setAsEssenceMessage
     * @param message 要被设置的精华消息
     *
     * @throws PermissionDeniedException 没有权限时抛出
     * @throws ClassCastException 当 [message] 不是 [MiraiMessageChainContent] 时
     * @throws UnsupportedActionException 当前消息无法被设置为精华消息时抛出（例如当前消息中不存在 [messageSource][MiraiMessageChainContent.messageSourceOrNull] 等）
     *
     * @return 是否操作成功
     *
     */
    @JST
    public suspend fun setEssenceMessage(message: MessageContent): Boolean {
        val content = message as MiraiMessageChainContent
        val source = content.messageSourceOrNull ?: throw UnsupportedActionException("No messageSource", NullPointerException("messageSourceOrNull is null"))

        return setEssenceMessage(source)
    }

    /**
     * 将一个消息设置为群精华消息, 需要管理员或群主权限。
     *
     * 等同于 [mirai Group.setEssenceMessage][OriginalMiraiGroup.setEssenceMessage]
     *
     * @see OriginalMiraiGroup.setEssenceMessage
     *
     * @param source 要被设置的精华消息
     *
     * @throws PermissionDeniedException 没有权限时抛出
     *
     * @return 是否操作成功
     */
    @JST
    public suspend fun setEssenceMessage(source: MessageSource): Boolean {
        return originalContact.setEssenceMessage(source)
    }


    /**
     * 群没有“上层”概念, 始终得到null。
     */
    @JvmSynthetic
    override suspend fun previous(): Organization? = null
}


/**
 * 群设置。是对 [mirai GroupSettings][GroupSettings] 的映射类型。
 *
 * @see GroupSettings
 */
@MiraiMappingType(GroupSettings::class)
public interface MiraiGroupSettings {
    /**
     * 获取原生的 [mirai GroupSettings][GroupSettings].
     */
    public val originalGroupSettings: GroupSettings

    /**
     * 是否启用了全体禁言
     *
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     * @see GroupSettings.isMuteAll
     */
    public var isMuteAll: Boolean
        get() = originalGroupSettings.isMuteAll
        set(value) {
            originalGroupSettings.isMuteAll = value
        }

    /**
     * 是否群员邀请好友入群
     *
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     * @see GroupSettings.isAllowMemberInvite
     */
    public var isAllowMemberInvite: Boolean
        get() = originalGroupSettings.isAllowMemberInvite
        set(value) {
            originalGroupSettings.isAllowMemberInvite = value
        }

    /**
     * 自动加群审批
     *
     * @see GroupSettings.isAutoApproveEnabled
     */
    @MiraiExperimentalApi
    public val isAutoApproveEnabled: Boolean
        get() = originalGroupSettings.isAutoApproveEnabled

    /**
     * 是否允许匿名聊天
     *
     * @see GroupSettings.isAnonymousChatEnabled
     */
    public var isAnonymousChatEnabled: Boolean
        get() = originalGroupSettings.isAnonymousChatEnabled
        set(value) {
            originalGroupSettings.isAnonymousChatEnabled = value
        }


}
