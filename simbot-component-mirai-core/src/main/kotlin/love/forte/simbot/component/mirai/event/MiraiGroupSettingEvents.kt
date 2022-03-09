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

import love.forte.simbot.component.mirai.*
import love.forte.simbot.event.*
import love.forte.simbot.message.*

/**
 * @see net.mamoe.mirai.event.events.GroupSettingChangeEvent
 */
public typealias OriginalMiraiGroupSettingChangeEvent<T> = net.mamoe.mirai.event.events.GroupSettingChangeEvent<T>

/**
 * @see net.mamoe.mirai.event.events.GroupNameChangeEvent
 */
public typealias OriginalMiraiGroupNameChangeEvent = net.mamoe.mirai.event.events.GroupNameChangeEvent

/**
 * @see net.mamoe.mirai.event.events.GroupEntranceAnnouncementChangeEvent
 */
public typealias OriginalMiraiGroupEntranceAnnouncementChangeEvent = net.mamoe.mirai.event.events.GroupEntranceAnnouncementChangeEvent

/**
 * @see net.mamoe.mirai.event.events.GroupMuteAllEvent
 */
public typealias OriginalMiraiGroupMuteAllEvent = net.mamoe.mirai.event.events.GroupMuteAllEvent

/**
 * @see net.mamoe.mirai.event.events.GroupAllowAnonymousChatEvent
 */
public typealias OriginalMiraiGroupAllowAnonymousChatEvent = net.mamoe.mirai.event.events.GroupAllowAnonymousChatEvent

/**
 * @see net.mamoe.mirai.event.events.GroupAllowConfessTalkEvent
 */
public typealias OriginalMiraiGroupAllowConfessTalkEvent = net.mamoe.mirai.event.events.GroupAllowConfessTalkEvent

/**
 * @see net.mamoe.mirai.event.events.GroupAllowMemberInviteEvent
 */
public typealias OriginalMiraiGroupAllowMemberInviteEvent = net.mamoe.mirai.event.events.GroupAllowMemberInviteEvent


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
public interface MiraiGroupSettingEvent<T, E : OriginalMiraiGroupSettingChangeEvent<T>> : MiraiSimbotBotEvent<E>,
    ChangedEvent<MiraiGroup, T, T> {

    override val bot: MiraiBot
    override val source: MiraiGroup
    override val before: T
    override val after: T


    //// Impl
    @JvmSynthetic
    override suspend fun after(): T = after
    @JvmSynthetic
    override suspend fun before(): T = before
    @JvmSynthetic
    override suspend fun source(): MiraiGroup = source


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

    //// Impl

    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC
    override val key: Event.Key<MiraiGroupNameChangeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiGroupNameChangeEvent>(
        "mirai.group_name_change", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupNameChangeEvent? = doSafeCast(value)
    }
}

/**
 * 入群公告改变.
 * @see OriginalMiraiGroupEntranceAnnouncementChangeEvent
 */
public interface MiraiGroupEntranceAnnouncementChangeEvent :
    MiraiGroupSettingEvent<String, OriginalMiraiGroupEntranceAnnouncementChangeEvent> {

    /**
     * 操作者。
     */
    public val operator: MiraiMember
    ////

    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC
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

    ////
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC
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

    ////
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC

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
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC
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

    ////
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC
    override val key: Event.Key<MiraiGroupAllowMemberInviteEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiGroupAllowMemberInviteEvent>(
        "mirai.group_allow_member_invite", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupAllowMemberInviteEvent? = doSafeCast(value)
    }
}
