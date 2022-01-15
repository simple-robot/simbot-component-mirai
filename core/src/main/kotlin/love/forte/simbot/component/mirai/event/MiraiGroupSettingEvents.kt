package love.forte.simbot.component.mirai.event

import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.ChangedEvent
import love.forte.simbot.event.Event
import love.forte.simbot.message.doSafeCast

/**
 * @see net.mamoe.mirai.event.events.GroupSettingChangeEvent
 */
public typealias NativeMiraiGroupSettingChangeEvent<T> = net.mamoe.mirai.event.events.GroupSettingChangeEvent<T>

/**
 * @see net.mamoe.mirai.event.events.GroupNameChangeEvent
 */
public typealias NativeMiraiGroupNameChangeEvent = net.mamoe.mirai.event.events.GroupNameChangeEvent

/**
 * @see net.mamoe.mirai.event.events.GroupEntranceAnnouncementChangeEvent
 */
public typealias NativeMiraiGroupEntranceAnnouncementChangeEvent = net.mamoe.mirai.event.events.GroupEntranceAnnouncementChangeEvent

/**
 * @see net.mamoe.mirai.event.events.GroupMuteAllEvent
 */
public typealias NativeMiraiGroupMuteAllEvent = net.mamoe.mirai.event.events.GroupMuteAllEvent

/**
 * @see net.mamoe.mirai.event.events.GroupAllowAnonymousChatEvent
 */
public typealias NativeMiraiGroupAllowAnonymousChatEvent = net.mamoe.mirai.event.events.GroupAllowAnonymousChatEvent

/**
 * @see net.mamoe.mirai.event.events.GroupAllowConfessTalkEvent
 */
public typealias NativeMiraiGroupAllowConfessTalkEvent = net.mamoe.mirai.event.events.GroupAllowConfessTalkEvent

/**
 * @see net.mamoe.mirai.event.events.GroupAllowMemberInviteEvent
 */
public typealias NativeMiraiGroupAllowMemberInviteEvent = net.mamoe.mirai.event.events.GroupAllowMemberInviteEvent


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
public interface MiraiGroupSettingEvent<T, E : NativeMiraiGroupSettingChangeEvent<T>>
    : MiraiSimbotBotEvent<E>,
    ChangedEvent<MiraiGroup, T, T> {

    override val bot: MiraiBot
    override val source: MiraiGroup
    override val before: T
    override val after: T

    //// Impl
    override suspend fun after(): T = after
    override suspend fun before(): T = before
    override suspend fun source(): MiraiGroup = source


    public companion object Key : BaseEventKey<MiraiGroupSettingEvent<*, *>>(
        "mirai.group_setting", MiraiSimbotBotEvent, ChangedEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupSettingEvent<*, *>? = doSafeCast(value)
    }
}


/**
 * 群名改变事件。
 * @see NativeMiraiGroupNameChangeEvent
 */
public interface MiraiGroupNameChangeEvent :
    MiraiGroupSettingEvent<String, NativeMiraiGroupNameChangeEvent> {

    override val key: Event.Key<MiraiGroupNameChangeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiGroupNameChangeEvent>(
        "mirai.group_name_change", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupNameChangeEvent? = doSafeCast(value)
    }
}

/**
 * 入群公告改变.
 * @see NativeMiraiGroupEntranceAnnouncementChangeEvent
 */
public interface MiraiGroupEntranceAnnouncementChangeEvent :
    MiraiGroupSettingEvent<String, NativeMiraiGroupEntranceAnnouncementChangeEvent> {

    override val key: Event.Key<MiraiGroupEntranceAnnouncementChangeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiGroupEntranceAnnouncementChangeEvent>(
        "mirai.group_entrance_announcement", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupEntranceAnnouncementChangeEvent? = doSafeCast(value)
    }
}

/**
 * 群 "全员禁言" 功能状态改变
 * @see NativeMiraiGroupMuteAllEvent
 */
public interface MiraiGroupMuteAllEvent :
    MiraiGroupSettingEvent<Boolean, NativeMiraiGroupMuteAllEvent> {

    override val key: Event.Key<MiraiGroupMuteAllEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiGroupMuteAllEvent>(
        "mirai.group_mute_all", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupMuteAllEvent? = doSafeCast(value)
    }
}

/**
 * 群 "匿名聊天" 功能状态改变
 * @see NativeMiraiGroupAllowAnonymousChatEvent
 */
public interface MiraiGroupAllowAnonymousChatEvent :
    MiraiGroupSettingEvent<Boolean, NativeMiraiGroupAllowAnonymousChatEvent> {

    override val key: Event.Key<MiraiGroupAllowAnonymousChatEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiGroupAllowAnonymousChatEvent>(
        "mirai.group_allow_anonymous_chat", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupAllowAnonymousChatEvent? = doSafeCast(value)
    }
}

/**
 * 群 "坦白说" 功能状态改变.
 * @see NativeMiraiGroupAllowConfessTalkEvent
 */
public interface MiraiGroupAllowConfessTalkEvent :
    MiraiGroupSettingEvent<Boolean, NativeMiraiGroupAllowConfessTalkEvent> {

    override val key: Event.Key<MiraiGroupAllowConfessTalkEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiGroupAllowConfessTalkEvent>(
        "mirai.group_allow_confess_talk", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupAllowConfessTalkEvent? = doSafeCast(value)
    }
}

/**
 * 群 "允许群员邀请好友加群" 功能状态改变.
 * @see NativeMiraiGroupAllowMemberInviteEvent
 */
public interface MiraiGroupAllowMemberInviteEvent :
    MiraiGroupSettingEvent<Boolean, NativeMiraiGroupAllowMemberInviteEvent> {

    override val key: Event.Key<MiraiGroupAllowMemberInviteEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiGroupAllowMemberInviteEvent>(
        "mirai.group_allow_member_invite", MiraiGroupSettingEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupAllowMemberInviteEvent? = doSafeCast(value)
    }
}
