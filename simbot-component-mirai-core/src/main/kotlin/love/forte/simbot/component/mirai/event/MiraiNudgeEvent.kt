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

package love.forte.simbot.component.mirai.event

import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.toMessage
import love.forte.simbot.definition.Objective
import love.forte.simbot.event.*
import love.forte.simbot.message.*
import love.forte.simbot.randomID
import net.mamoe.mirai.event.events.NudgeEvent
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
    @OptIn(Api4J::class)
    override val source: Objective

    /**
     * 发送这个戳一戳的源头。如果来自私聊，则可能是 [MiraiFriend]、[MiraiStranger]、[MiraiMember],
     * 如果来自群聊，则为 [MiraiGroup].
     */
    @JvmSynthetic
    override suspend fun source(): Objective = source

    //// apis

    //region reply api
    /**
     * 回复消息发送者。
     */
    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiContact>

    /**
     * 回复消息发送者。
     */
    @JvmSynthetic
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiContact>

    /**
     * 回复消息发送者。
     */
    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiContact>

    /**
     * 回复消息发送者。
     */
    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiContact>

    /**
     * 回复消息发送者。
     */
    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiContact>

    /**
     * 回复消息发送者。
     */
    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiContact>

    //endregion


    //region reply nudge api
    /**
     * 回复此目标一个戳一戳。相当于针对当前的 target 发送一个戳一戳。
     */
    @JvmSynthetic
    public suspend fun replyNudge(): Boolean

    /**
     * 回复此目标一个戳一戳。相当于针对当前的 target 发送一个戳一戳。
     */
    @Api4J
    public fun replyNudgeBlocking(): Boolean

    /**
     * 回复此目标一个戳一戳。相当于针对当前的 target 发送一个戳一戳。
     */
    @Api4J
    public fun replyNudgeAsync()
    //endregion


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
public class MiraiReceivedNudgeMessageContent(public val nudgeEvent: NudgeEvent) : ReceivedMessageContent() {
    override val messageId: ID = randomID()
    override val messages: Messages = nudgeEvent.toMessage().toMessages()
    
    /**
     * 戳一戳事件不支持撤回，[delete] 将会始终返回 `false`。
     */
    override suspend fun delete(): Boolean = false
    
    
    /**
     * 戳一戳事件不支持撤回，[delete][deleteBlocking] 将会始终返回 `false`。
     */
    @Api4J
    override fun deleteBlocking(): Boolean {
        return false
    }
}


/**
 * 在群中收到的戳一戳事件。
 *
 * 实现 [GroupMessageEvent], 但是不同于 [MiraiGroupMessageEvent].
 *
 */
public interface MiraiGroupNudgeEvent : MiraiNudgeEvent, GroupMessageEvent {
    /**
     * 这个戳一戳消息所在群。
     */
    @OptIn(Api4J::class)
    override val source: MiraiGroup

    /**
     * 这个戳一戳消息所在群。
     */
    @JvmSynthetic
    override suspend fun source(): MiraiGroup

    /**
     * 这个戳一戳消息所在群。
     */
    @OptIn(Api4J::class)
    override val group: MiraiGroup

    /**
     * 这个戳一戳消息所在群。
     */
    @JvmSynthetic
    override suspend fun group(): MiraiGroup

    /**
     * 这个戳一戳消息所在群。
     */
    @OptIn(Api4J::class)
    override val organization: MiraiGroup

    /**
     * 这个戳一戳消息所在群。
     */
    @JvmSynthetic
    override suspend fun organization(): MiraiGroup

    /**
     * 戳一戳消息发送者。
     */
    @OptIn(Api4J::class)
    override val author: MiraiMember

    /**
     * 戳一戳消息发送者。
     */
    @JvmSynthetic
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
    @OptIn(Api4J::class)
    override val source: MiraiMember

    /**
     * 发送戳一戳的成员。
     */
    @JvmSynthetic
    override suspend fun source(): MiraiMember

    /**
     * 发送戳一戳的成员。
     */
    @OptIn(Api4J::class)
    override val user: MiraiMember

    /**
     * 发送戳一戳的成员。
     */
    @JvmSynthetic
    override suspend fun user(): MiraiMember

    //region reply api
    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    @JvmSynthetic
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    //endregion


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
    @OptIn(Api4J::class)
    override val source: MiraiFriend

    /**
     * 发送戳一戳消息的好友。
     */
    @JvmSynthetic
    override suspend fun source(): MiraiFriend

    /**
     * 发送戳一戳消息的好友。
     */
    @OptIn(Api4J::class)
    override val friend: MiraiFriend

    /**
     * 发送戳一戳消息的好友。
     */
    @JvmSynthetic
    override suspend fun friend(): MiraiFriend

    /**
     * 发送戳一戳消息的好友。
     */
    @OptIn(Api4J::class)
    override val user: MiraiFriend

    /**
     * 发送戳一戳消息的好友。
     */
    @JvmSynthetic
    override suspend fun user(): MiraiFriend


    //region reply api
    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @JvmSynthetic
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    //endregion


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
    @OptIn(Api4J::class)
    override val source: MiraiStranger

    /**
     * 发送戳一戳消息的陌生人。
     */
    @JvmSynthetic
    override suspend fun source(): MiraiStranger

    /**
     * 发送戳一戳消息的陌生人。
     */
    @OptIn(Api4J::class)
    override val user: MiraiStranger

    /**
     * 发送戳一戳消息的陌生人。
     */
    @JvmSynthetic
    override suspend fun user(): MiraiStranger

    //region reply api
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    //endregion


    override val key: Event.Key<out MiraiStrangerNudgeEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiStrangerNudgeEvent>(
        "mirai.stranger_nudge", MiraiNudgeEvent, ContactMessageEvent
    ) {
        override fun safeCast(value: Any): MiraiStrangerNudgeEvent? = doSafeCast(value)
    }
}