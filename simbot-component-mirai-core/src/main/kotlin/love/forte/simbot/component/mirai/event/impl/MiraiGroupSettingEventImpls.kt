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

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.randomID
import net.mamoe.mirai.event.events.operatorOrBot
import net.mamoe.mirai.event.events.GroupAllowAnonymousChatEvent as OriginalMiraiGroupAllowAnonymousChatEvent
import net.mamoe.mirai.event.events.GroupAllowConfessTalkEvent as OriginalMiraiGroupAllowConfessTalkEvent
import net.mamoe.mirai.event.events.GroupAllowMemberInviteEvent as OriginalMiraiGroupAllowMemberInviteEvent
import net.mamoe.mirai.event.events.GroupMuteAllEvent as OriginalMiraiGroupMuteAllEvent
import net.mamoe.mirai.event.events.GroupNameChangeEvent as OriginalMiraiGroupNameChangeEvent
import net.mamoe.mirai.event.events.GroupSettingChangeEvent as OriginalMiraiGroupSettingChangeEvent


internal abstract class BaseMiraiGroupSettingEvent<T, E : OriginalMiraiGroupSettingChangeEvent<T>>(
    final override val bot: MiraiBotImpl, final override val originalEvent: E,
) : MiraiGroupSettingEvent<T, E> {
    override val id: ID = randomID()
    protected val sourceInternal = originalEvent.group.asSimbot(bot)
    override val changedTime: Timestamp = Timestamp.now()
    override suspend fun source(): MiraiGroup = sourceInternal
    override suspend fun before(): T = originalEvent.origin
    override suspend fun after(): T = originalEvent.new
}


internal class MiraiGroupNameChangeEventImpl(
    bot: MiraiBotImpl,
    nativeEvent: OriginalMiraiGroupNameChangeEvent,
) : BaseMiraiGroupSettingEvent<String, OriginalMiraiGroupNameChangeEvent>(bot, nativeEvent), MiraiGroupNameChangeEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, sourceInternal)
    
}

internal class MiraiGroupMuteAllEventImpl(
    bot: MiraiBotImpl, nativeEvent: OriginalMiraiGroupMuteAllEvent,
) : BaseMiraiGroupSettingEvent<Boolean, OriginalMiraiGroupMuteAllEvent>(bot, nativeEvent), MiraiGroupMuteAllEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, sourceInternal)
}

internal class MiraiGroupAllowAnonymousChatEventImpl(
    bot: MiraiBotImpl, nativeEvent: OriginalMiraiGroupAllowAnonymousChatEvent,
) : BaseMiraiGroupSettingEvent<Boolean, OriginalMiraiGroupAllowAnonymousChatEvent>(bot, nativeEvent),
    MiraiGroupAllowAnonymousChatEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, sourceInternal)
}

internal class MiraiGroupAllowConfessTalkEventImpl(
    bot: MiraiBotImpl, nativeEvent: OriginalMiraiGroupAllowConfessTalkEvent,
) : BaseMiraiGroupSettingEvent<Boolean, OriginalMiraiGroupAllowConfessTalkEvent>(bot, nativeEvent),
    MiraiGroupAllowConfessTalkEvent

internal class MiraiGroupAllowMemberInviteEventImpl(
    bot: MiraiBotImpl, nativeEvent: OriginalMiraiGroupAllowMemberInviteEvent,
) : BaseMiraiGroupSettingEvent<Boolean, OriginalMiraiGroupAllowMemberInviteEvent>(bot, nativeEvent),
    MiraiGroupAllowMemberInviteEvent {
    override val operator = nativeEvent.operatorOrBot.asSimbot(bot, sourceInternal)
}
