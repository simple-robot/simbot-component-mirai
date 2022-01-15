package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import net.mamoe.mirai.event.events.operatorOrBot


internal abstract class BaseMiraiGroupSettingEvent<T, E : NativeMiraiGroupSettingChangeEvent<T>>(
    final override val bot: MiraiBotImpl, nativeEvent: E
) : MiraiGroupSettingEvent<T, E> {
    override val source = nativeEvent.group.asSimbot(bot)
    override val changedTime: Timestamp = Timestamp.now()
    override val before = nativeEvent.origin
    override val after = nativeEvent.new
    override val metadata = nativeEvent.toSimpleMetadata()
}


internal class MiraiGroupNameChangeEventImpl(
    bot: MiraiBotImpl, nativeEvent: NativeMiraiGroupNameChangeEvent
) : BaseMiraiGroupSettingEvent<String, NativeMiraiGroupNameChangeEvent>(bot, nativeEvent), MiraiGroupNameChangeEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, source)
    override val metadata = nativeEvent.toSimpleMetadata()
}

internal class MiraiGroupEntranceAnnouncementChangeEventImpl(
    bot: MiraiBotImpl, nativeEvent: NativeMiraiGroupEntranceAnnouncementChangeEvent
) : BaseMiraiGroupSettingEvent<String, NativeMiraiGroupEntranceAnnouncementChangeEvent>(bot, nativeEvent),
    MiraiGroupEntranceAnnouncementChangeEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, source)
    override val metadata = nativeEvent.toSimpleMetadata()
}

internal class MiraiGroupMuteAllEventImpl(
    bot: MiraiBotImpl, nativeEvent: NativeMiraiGroupMuteAllEvent
) : BaseMiraiGroupSettingEvent<Boolean, NativeMiraiGroupMuteAllEvent>(bot, nativeEvent), MiraiGroupMuteAllEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, source)
    override val metadata = nativeEvent.toSimpleMetadata()
}

internal class MiraiGroupAllowAnonymousChatEventImpl(
    bot: MiraiBotImpl, nativeEvent: NativeMiraiGroupAllowAnonymousChatEvent
) : BaseMiraiGroupSettingEvent<Boolean, NativeMiraiGroupAllowAnonymousChatEvent>(bot, nativeEvent),
    MiraiGroupAllowAnonymousChatEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, source)
    override val metadata = nativeEvent.toSimpleMetadata()
}

internal class MiraiGroupAllowConfessTalkEventImpl(
    bot: MiraiBotImpl, nativeEvent: NativeMiraiGroupAllowConfessTalkEvent
) : BaseMiraiGroupSettingEvent<Boolean, NativeMiraiGroupAllowConfessTalkEvent>(bot, nativeEvent),
    MiraiGroupAllowConfessTalkEvent {
    override val metadata = nativeEvent.toSimpleMetadata()
}

internal class MiraiGroupAllowMemberInviteEventImpl(
    bot: MiraiBotImpl, nativeEvent: NativeMiraiGroupAllowMemberInviteEvent
) : BaseMiraiGroupSettingEvent<Boolean, NativeMiraiGroupAllowMemberInviteEvent>(bot, nativeEvent),
    MiraiGroupAllowMemberInviteEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, source)
    override val metadata = nativeEvent.toSimpleMetadata()
}
