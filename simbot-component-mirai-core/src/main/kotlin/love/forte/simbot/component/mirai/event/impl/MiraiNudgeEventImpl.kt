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
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.randomID
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.action.Nudge.Companion.sendNudge
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup
import net.mamoe.mirai.contact.Member as OriginalMiraiMember
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger


internal abstract class BaseMiraiNudgeEvent<C : Contact>(
    final override val bot: MiraiBotImpl,
    final override val originalEvent: NudgeEvent,
) : MiraiNudgeEvent {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.now()
    override val messageContent: MiraiReceivedNudgeMessageContent = MiraiReceivedNudgeMessageContent(originalEvent)
    
    abstract override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<C>
    abstract override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<C>
    abstract override suspend fun reply(text: String): SimbotMiraiMessageReceipt<C>
    
    @JvmSynthetic
    override suspend fun replyNudge(): Boolean {
        if (this is User) {
            return sendNudge(originalEvent.from.nudge())
        }
        
        return false
    }
}


internal class MiraiGroupNudgeEventImpl(
    bot: MiraiBotImpl,
    originalEvent: NudgeEvent,
    miraiGroup: OriginalMiraiGroup,
    miraiMember: OriginalMiraiMember,
) : MiraiGroupNudgeEvent, BaseMiraiNudgeEvent<OriginalMiraiGroup>(bot, originalEvent) {
    private val _group = miraiGroup.asSimbot(bot)
    private val _author = miraiMember.asSimbot(bot, _group)
    
    override suspend fun group() = _group
    override suspend fun author() = _author
    
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<net.mamoe.mirai.contact.Group> {
        return _group.send(message)
    }
    
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<net.mamoe.mirai.contact.Group> {
        return _group.send(message)
    }
    
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<net.mamoe.mirai.contact.Group> {
        return _group.send(text)
    }
}


internal class MiraiMemberNudgeEventImpl(
    bot: MiraiBotImpl,
    originalEvent: NudgeEvent,
    miraiMember: OriginalMiraiMember,
) : MiraiMemberNudgeEvent, BaseMiraiNudgeEvent<OriginalMiraiMember>(bot, originalEvent) {
    private val _user = miraiMember.asSimbot(bot)
    
    override suspend fun user() = _user
    
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<net.mamoe.mirai.contact.Member> {
        return _user.send(message)
    }
    
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<net.mamoe.mirai.contact.Member> {
        return _user.send(message)
    }
    
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<net.mamoe.mirai.contact.Member> {
        return _user.send(text)
    }
}


internal class MiraiFriendNudgeEventImpl(
    bot: MiraiBotImpl,
    originalEvent: NudgeEvent,
    miraiFriend: OriginalMiraiFriend,
) : MiraiFriendNudgeEvent, BaseMiraiNudgeEvent<OriginalMiraiFriend>(bot, originalEvent) {
    private val _friend = miraiFriend.asSimbot(bot)
    
    override suspend fun friend() = _friend
    
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend> {
        return _friend.send(message)
    }
    
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend> {
        return _friend.send(message)
    }
    
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend> {
        return _friend.send(text)
    }
}


internal class MiraiStrangerNudgeEventImpl(
    bot: MiraiBotImpl,
    originalEvent: NudgeEvent,
    miraiStranger: OriginalMiraiStranger,
) : MiraiStrangerNudgeEvent, BaseMiraiNudgeEvent<OriginalMiraiStranger>(bot, originalEvent) {
    private val _user = miraiStranger.asSimbot(bot)
    
    override suspend fun user() = _user
    
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<net.mamoe.mirai.contact.Stranger> {
        return _user.send(message)
    }
    
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<net.mamoe.mirai.contact.Stranger> {
        return _user.send(message)
        
    }
    
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<net.mamoe.mirai.contact.Stranger> {
        return _user.send(text)
    }
}

