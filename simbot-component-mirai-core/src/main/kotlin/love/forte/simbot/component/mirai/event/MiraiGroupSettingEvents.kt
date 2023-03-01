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

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.event.BaseEvent
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.ChangedEvent
import love.forte.simbot.event.Event
import love.forte.simbot.message.doSafeCast
import org.jetbrains.annotations.ApiStatus
import net.mamoe.mirai.event.events.GroupAllowAnonymousChatEvent as OriginalMiraiGroupAllowAnonymousChatEvent
import net.mamoe.mirai.event.events.GroupAllowConfessTalkEvent as OriginalMiraiGroupAllowConfessTalkEvent
import net.mamoe.mirai.event.events.GroupAllowMemberInviteEvent as OriginalMiraiGroupAllowMemberInviteEvent
import net.mamoe.mirai.event.events.GroupMuteAllEvent as OriginalMiraiGroupMuteAllEvent
import net.mamoe.mirai.event.events.GroupNameChangeEvent as OriginalMiraiGroupNameChangeEvent
import net.mamoe.mirai.event.events.GroupSettingChangeEvent as OriginalMiraiGroupSettingChangeEvent


/**
 * 与mirai的 群设置 相关的事件总类。
 *
 * 这些与设置变更相关的事件均属于 [已变化事件][ChangedEvent], 事件源为群，
 * 变化前后为对应设置项值。
 *
 * 参考 `net.mamoe.mirai.event.events.group.kt`.
 *
 * @see MiraiGroupNameChangeEvent
 * @see MiraiGroupEntranceAnnouncementChangeEvent
 * @see MiraiGroupMuteAllEvent
 * @see MiraiGroupAllowAnonymousChatEvent
 * @see MiraiGroupAllowConfessTalkEvent
 * @see MiraiGroupAllowMemberInviteEvent
 */
@BaseEvent
@JvmBlocking(asProperty = true, suffix = "")
@JvmAsync(asProperty = true)
public interface MiraiGroupSettingEvent<T, E : OriginalMiraiGroupSettingChangeEvent<T>> : MiraiSimbotBotEvent<E>,
    ChangedEvent {
    
    override val bot: MiraiBot
    
    /**
     * 涉及群。
     */
    override suspend fun source(): MiraiGroup
    
    /**
     * 变更前值
     */
    override suspend fun before(): T
    
    /**
     * 变更后值
     */
    override suspend fun after(): T
    
    
    public companion object Key : BaseEventKey<MiraiGroupSettingEvent<*, *>>(
        "mirai.group_setting", MiraiSimbotBotEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupSettingEvent<*, *>? = doSafeCast(value)
    }
}


/**
 * 群名改变事件。
 * @see OriginalMiraiGroupNameChangeEvent
 */
public interface MiraiGroupNameChangeEvent : MiraiGroupSettingEvent<String, OriginalMiraiGroupNameChangeEvent> {
    /**
     * 操作者。
     */
    public val operator: MiraiMember
    
    
    override val key: Event.Key<MiraiGroupNameChangeEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupNameChangeEvent>(
        "mirai.group_name_change", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupNameChangeEvent? = doSafeCast(value)
    }
}

/**
 * 入群公告改变.
 * @see net.mamoe.mirai.event.events.GroupEntranceAnnouncementChangeEvent
 */
@Suppress("DEPRECATION", "DEPRECATION_ERROR")
@Deprecated("This event (net.mamoe.mirai.event.events.GroupEntranceAnnouncementChangeEvent) is not being triggered anymore.", level = DeprecationLevel.ERROR)
@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0.0")
public interface MiraiGroupEntranceAnnouncementChangeEvent :
    MiraiGroupSettingEvent<String, net.mamoe.mirai.event.events.GroupEntranceAnnouncementChangeEvent> {
    
    /**
     * 操作者。
     */
    public val operator: MiraiMember
    
    
    override val key: Event.Key<MiraiGroupEntranceAnnouncementChangeEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupEntranceAnnouncementChangeEvent>(
        "mirai.group_entrance_announcement", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupEntranceAnnouncementChangeEvent? = doSafeCast(value)
    }
}

/**
 * 群 "全员禁言" 功能状态改变
 * @see OriginalMiraiGroupMuteAllEvent
 */
public interface MiraiGroupMuteAllEvent : MiraiGroupSettingEvent<Boolean, OriginalMiraiGroupMuteAllEvent> {
    
    /**
     * 操作者。
     */
    public val operator: MiraiMember
    
    
    override val key: Event.Key<MiraiGroupMuteAllEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupMuteAllEvent>(
        "mirai.group_mute_all", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupMuteAllEvent? = doSafeCast(value)
    }
}

/**
 * 群 "匿名聊天" 功能状态改变
 * @see OriginalMiraiGroupAllowAnonymousChatEvent
 */
public interface MiraiGroupAllowAnonymousChatEvent :
    MiraiGroupSettingEvent<Boolean, OriginalMiraiGroupAllowAnonymousChatEvent> {
    
    /**
     * 操作者。
     */
    public val operator: MiraiMember
    
    override val key: Event.Key<MiraiGroupAllowAnonymousChatEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupAllowAnonymousChatEvent>(
        "mirai.group_allow_anonymous_chat", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupAllowAnonymousChatEvent? = doSafeCast(value)
    }
}

/**
 * 群 "坦白说" 功能状态改变.
 * @see OriginalMiraiGroupAllowConfessTalkEvent
 */
public interface MiraiGroupAllowConfessTalkEvent :
    MiraiGroupSettingEvent<Boolean, OriginalMiraiGroupAllowConfessTalkEvent> {
    
    override val key: Event.Key<MiraiGroupAllowConfessTalkEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupAllowConfessTalkEvent>(
        "mirai.group_allow_confess_talk", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupAllowConfessTalkEvent? = doSafeCast(value)
    }
}

/**
 * 群 "允许群员邀请好友加群" 功能状态改变.
 * @see OriginalMiraiGroupAllowMemberInviteEvent
 */
public interface MiraiGroupAllowMemberInviteEvent :
    MiraiGroupSettingEvent<Boolean, OriginalMiraiGroupAllowMemberInviteEvent> {
    
    /**
     * 操作者。
     */
    public val operator: MiraiMember
    
    override val key: Event.Key<MiraiGroupAllowMemberInviteEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupAllowMemberInviteEvent>(
        "mirai.group_allow_member_invite", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupAllowMemberInviteEvent? = doSafeCast(value)
    }
}
