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

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.randomID
import net.mamoe.mirai.event.events.operatorOrBot
import net.mamoe.mirai.event.events.GroupAllowAnonymousChatEvent as OriginalMiraiGroupAllowAnonymousChatEvent
import net.mamoe.mirai.event.events.GroupAllowConfessTalkEvent as OriginalMiraiGroupAllowConfessTalkEvent
import net.mamoe.mirai.event.events.GroupAllowMemberInviteEvent as OriginalMiraiGroupAllowMemberInviteEvent
import net.mamoe.mirai.event.events.GroupEntranceAnnouncementChangeEvent as OriginalMiraiGroupEntranceAnnouncementChangeEvent
import net.mamoe.mirai.event.events.GroupMuteAllEvent as OriginalMiraiGroupMuteAllEvent
import net.mamoe.mirai.event.events.GroupNameChangeEvent as OriginalMiraiGroupNameChangeEvent
import net.mamoe.mirai.event.events.GroupSettingChangeEvent as OriginalMiraiGroupSettingChangeEvent


internal abstract class BaseMiraiGroupSettingEvent<T, E : OriginalMiraiGroupSettingChangeEvent<T>>(
    final override val bot: MiraiBotImpl, final override val originalEvent: E
) : MiraiGroupSettingEvent<T, E> {
    override val id: ID = randomID()
    override val source = originalEvent.group.asSimbot(bot)
    override val changedTime: Timestamp = Timestamp.now()
    override val before = originalEvent.origin
    override val after = originalEvent.new
}


internal class MiraiGroupNameChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiGroupNameChangeEvent
) : BaseMiraiGroupSettingEvent<String, OriginalMiraiGroupNameChangeEvent>(bot, nativeEvent), MiraiGroupNameChangeEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, source)
}

internal class MiraiGroupEntranceAnnouncementChangeEventImpl(
    bot: MiraiBotImpl, nativeEvent: OriginalMiraiGroupEntranceAnnouncementChangeEvent
) : BaseMiraiGroupSettingEvent<String, OriginalMiraiGroupEntranceAnnouncementChangeEvent>(bot, nativeEvent),
    MiraiGroupEntranceAnnouncementChangeEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, source)
}

internal class MiraiGroupMuteAllEventImpl(
    bot: MiraiBotImpl, nativeEvent: OriginalMiraiGroupMuteAllEvent
) : BaseMiraiGroupSettingEvent<Boolean, OriginalMiraiGroupMuteAllEvent>(bot, nativeEvent), MiraiGroupMuteAllEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, source)
}

internal class MiraiGroupAllowAnonymousChatEventImpl(
    bot: MiraiBotImpl, nativeEvent: OriginalMiraiGroupAllowAnonymousChatEvent
) : BaseMiraiGroupSettingEvent<Boolean, OriginalMiraiGroupAllowAnonymousChatEvent>(bot, nativeEvent),
    MiraiGroupAllowAnonymousChatEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, source)
}

internal class MiraiGroupAllowConfessTalkEventImpl(
    bot: MiraiBotImpl, nativeEvent: OriginalMiraiGroupAllowConfessTalkEvent
) : BaseMiraiGroupSettingEvent<Boolean, OriginalMiraiGroupAllowConfessTalkEvent>(bot, nativeEvent),
    MiraiGroupAllowConfessTalkEvent {
}

internal class MiraiGroupAllowMemberInviteEventImpl(
    bot: MiraiBotImpl, nativeEvent: OriginalMiraiGroupAllowMemberInviteEvent
) : BaseMiraiGroupSettingEvent<Boolean, OriginalMiraiGroupAllowMemberInviteEvent>(bot, nativeEvent),
    MiraiGroupAllowMemberInviteEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, source)
}
