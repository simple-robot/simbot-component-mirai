/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
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

package love.forte.simbot.component.mirai.event

import love.forte.simbot.ID
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.component.mirai.message.MiraiMessageContent
import love.forte.simbot.component.mirai.message.toMessage
import love.forte.simbot.definition.Objective
import love.forte.simbot.event.*
import love.forte.simbot.message.*
import love.forte.simbot.randomID
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.contact.Contact as OriginalMiraiContact
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend
import net.mamoe.mirai.contact.Member as OriginalMiraiMember
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger

/**
 * mirai中与戳一戳相关的事件。
 *
 * 戳一戳事件也算所相对应的消息事件。
 *
 * 戳一戳事件算作消息事件，消息中将只包含一个 nudge 对象，不存在plainText.
 *
 * 不会接收 `from == bot.id` 的戳一戳事件。
 *
 * 针对 [mirai的戳一戳事件][NudgeEvent] 中 [subject][NudgeEvent.subject]
 * 所示的可能类型提供4个不同的 [MiraiNudgeEvent] 子类型：
 *
 * - `GROUP` -> [MiraiGroupNudgeEvent]
 * - `STRANGER` -> [MiraiMemberNudgeEvent]
 * - `FRIEND` -> [MiraiFriendNudgeEvent]
 * - `MEMBER` -> [MiraiStrangerNudgeEvent]
 *
 * @author ForteScarlet
 */
public interface MiraiNudgeEvent : MiraiSimbotEvent<NudgeEvent>, MessageEvent, ReplySupport {
    override val originalEvent: NudgeEvent
    override val messageContent: MiraiReceivedNudgeMessageContent
    
    /**
     * 事件涉及的bot。
     */
    override val bot: MiraiBot
    
    /**
     * 发送这个戳一戳的源头。如果来自私聊，则可能是 [MiraiFriend]、[MiraiStranger]、[MiraiMember],
     * 如果来自群聊，则为 [MiraiGroup].
     */
    @JSTP
    override suspend fun source(): Objective
    
    //// apis
    
    // region reply api
    /**
     * 回复消息发送者。
     */
    @JST
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiContact>
    
    /**
     * 回复消息发送者。
     */
    @JST
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiContact>
    
    /**
     * 回复消息发送者。
     */
    @JST
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiContact>
    // endregion
    
    
    // region reply nudge api
    /**
     * 回复此目标一个戳一戳。相当于针对当前的 target 发送一个戳一戳。
     */
    @JST
    public suspend fun replyNudge(): Boolean
    // endregion
    
    
    override val key: Event.Key<out MiraiNudgeEvent>
    
    public companion object Key : BaseEventKey<MiraiNudgeEvent>(
        "mirai.nudge", MiraiSimbotEvent, MessageEvent
    ) {
        override fun safeCast(value: Any): MiraiNudgeEvent? = doSafeCast(value)
    }
}


/**
 * 针对于 [戳一戳事件][MiraiNudgeEvent] 所使用的 [ReceivedMessageContent] 实现。
 *
 * [MiraiReceivedNudgeMessageContent] 来源于戳一戳事件，戳一戳事件不支持撤回，
 * [MiraiReceivedNudgeMessageContent.delete] 将会始终返回 `false`。
 *
 */
@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
public class MiraiReceivedNudgeMessageContent(public val nudgeEvent: NudgeEvent) : ReceivedMessageContent(),
    MiraiMessageContent {
    /**
     * 戳一戳消息不存在 [MessageSource], 不可撤回、引用, 因此也不存在可用的真正消息ID, 使用 [randomID] 作为消息ID。
     *
     */
    override val messageId: ID = randomID()

    /**
     * 与 [messageId] 一致。
     */
    override val fullMessageId: ID get() = messageId

    override val messages: Messages = nudgeEvent.toMessage().toMessages()
    
    /**
     * 戳一戳事件不支持撤回，[delete] 将会始终返回 `false`。
     */
    @JvmSynthetic
    override suspend fun delete(): Boolean = false
}


/**
 * 在群中收到的戳一戳事件。
 *
 * 实现 [GroupMessageEvent], 但是不同于 [MiraiGroupMessageEvent].
 *
 */
@JSTP
public interface MiraiGroupNudgeEvent : MiraiNudgeEvent, GroupMessageEvent {
    /**
     * 这个戳一戳消息所在群。
     */
    override suspend fun group(): MiraiGroup
    
    /**
     * 这个戳一戳消息所在群。同 [group]。
     */
    override suspend fun source(): MiraiGroup = group()
    
    /**
     * 这个戳一戳消息所在群。同 [group]。
     */
    override suspend fun organization(): MiraiGroup = group()
    
    /**
     * 戳一戳消息发送者。
     */
    override suspend fun author(): MiraiMember
    
    
    override val key: Event.Key<out MiraiGroupNudgeEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiGroupNudgeEvent>(
        "mirai.group_nudge", MiraiNudgeEvent, GroupMessageEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupNudgeEvent? = doSafeCast(value)
    }
    
}


/**
 * 在群成员（临时会话）处收到的戳一戳事件。
 *
 * 实现 [ContactMessageEvent], 但不同于 [MiraiMemberMessageEvent]。
 */
public interface MiraiMemberNudgeEvent : MiraiNudgeEvent, ContactMessageEvent {
    
    /**
     * 发送戳一戳的成员。
     */
    @JSTP
    override suspend fun user(): MiraiMember
    
    /**
     * 发送戳一戳的成员。同 [user]。
     */
    @JSTP
    override suspend fun source(): MiraiMember = user()
    
    // region reply api
    @JST
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    
    @JST
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    
    @JST
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    // endregion
    
    
    override val key: Event.Key<out MiraiMemberNudgeEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiMemberNudgeEvent>(
        "mirai.member_nudge", MiraiNudgeEvent, ContactMessageEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberNudgeEvent? = doSafeCast(value)
    }
}


/**
 * 在好友处收到的戳一戳事件。
 *
 * 实现 [FriendMessageEvent], 但是不同于 [MiraiFriendMessageEvent].
 */
public interface MiraiFriendNudgeEvent : MiraiNudgeEvent, FriendMessageEvent {
    
    /**
     * 发送戳一戳消息的好友。
     */
    @JSTP
    override suspend fun friend(): MiraiFriend
    
    /**
     * 发送戳一戳消息的好友。同 [friend]。
     */
    @JSTP
    override suspend fun source(): MiraiFriend = friend()
    
    /**
     * 发送戳一戳消息的好友。同 [friend]。
     */
    @JSTP
    override suspend fun user(): MiraiFriend = friend()
    
    
    // region reply api
    @JST
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    
    @JST
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    
    @JST
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    // endregion
    
    
    override val key: Event.Key<out MiraiFriendNudgeEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiFriendNudgeEvent>(
        "mirai.friend_nudge", MiraiNudgeEvent, FriendMessageEvent
    ) {
        override fun safeCast(value: Any): MiraiFriendNudgeEvent? = doSafeCast(value)
    }
}


/**
 * 在陌生人处收到的戳一戳事件。
 *
 * 实现 [ContactMessageEvent], 但是不同于 [MiraiStrangerMessageEvent].
 */
public interface MiraiStrangerNudgeEvent : MiraiNudgeEvent, ContactMessageEvent {
    /**
     * 发送戳一戳消息的陌生人。
     */
    @JSTP
    override suspend fun user(): MiraiStranger
    
    /**
     * 发送戳一戳消息的陌生人。
     */
    @JSTP
    override suspend fun source(): MiraiStranger = user()
    
    // region reply api
    @JST
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    @JST
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    @JST
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    // endregion
    
    
    override val key: Event.Key<out MiraiStrangerNudgeEvent> get() = Key
    
    public companion object Key : BaseEventKey<MiraiStrangerNudgeEvent>(
        "mirai.stranger_nudge", MiraiNudgeEvent, ContactMessageEvent
    ) {
        override fun safeCast(value: Any): MiraiStrangerNudgeEvent? = doSafeCast(value)
    }
}
