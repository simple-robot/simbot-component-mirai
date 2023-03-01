/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU Affero通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
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

