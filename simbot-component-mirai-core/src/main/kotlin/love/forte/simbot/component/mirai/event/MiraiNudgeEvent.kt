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

import kotlinx.coroutines.launch
import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.action.MessageReplyReceipt
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.toMessage
import love.forte.simbot.definition.Objectives
import love.forte.simbot.event.*
import love.forte.simbot.message.*
import love.forte.simbot.randomID
import love.forte.simbot.utils.runInBlocking
import net.mamoe.mirai.event.events.NudgeEvent

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
    override val messageContent: ReceivedMessageContent

    /**
     * 事件涉及的bot。
     */
    override val bot: MiraiBot

    //// Impl apis
    override suspend fun reply(message: Message): MessageReplyReceipt
    override suspend fun reply(message: MessageContent): MessageReplyReceipt
    override suspend fun reply(text: String): MessageReplyReceipt

    /**
     * 回复此目标一个戳一戳。相当于针对当前的 target 发送一个戳一戳。
     */
    @JvmSynthetic
    public suspend fun replyNudge() {
        // TODO
    }

    /**
     * 回复此目标一个戳一戳。相当于针对当前的 target 发送一个戳一戳。
     */
    @Api4J
    public fun replyNudgeBlocking() {
        runInBlocking { replyNudge() }
    }

    /**
     * 回复此目标一个戳一戳。相当于针对当前的 target 发送一个戳一戳。
     */
    @Api4J
    public fun replyNudgeAsync() {
        bot.launch { replyNudge() }
    }


    //// Impl props
    @OptIn(Api4J::class)
    override val source: Objectives
    override suspend fun source(): Objectives = source

    override val key: Event.Key<out MiraiNudgeEvent>

    public companion object Key : BaseEventKey<MiraiNudgeEvent>(
        "mirai.nudge", MiraiSimbotEvent, MessageEvent
    ) {
        override fun safeCast(value: Any): MiraiNudgeEvent? = doSafeCast(value)
    }
}


/**
 * 针对于 [戳一戳事件][MiraiNudgeEvent] 所使用的 [ReceivedMessageContent] 实现。
 */
@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
public class MiraiReceivedNudgeMessageContent(public val nudgeEvent: NudgeEvent) : ReceivedMessageContent() {
    override val messages: Messages = nudgeEvent.toMessage().toMessages()
    override val messageId: ID = randomID()
}


/**
 * 在群中收到的戳一戳事件。
 *
 * 实现 [GroupMessageEvent], 但是不同于 [MiraiGroupMessageEvent].
 *
 */
public interface MiraiGroupNudgeEvent : MiraiNudgeEvent, GroupMessageEvent {
    @OptIn(Api4J::class)
    override val source: MiraiGroup
    override suspend fun source(): MiraiGroup = source

}


/**
 * 在群成员处收到的戳一戳事件。
 *
 * 实现 [ContactMessageEvent], 但是不同于 [MiraiMemberMessageEvent].
 */
public interface MiraiMemberNudgeEvent : MiraiNudgeEvent, ContactMessageEvent {
    @OptIn(Api4J::class)
    override val source: MiraiMember
    override suspend fun source(): MiraiMember = source

}


/**
 * 在好友处收到的戳一戳事件。
 *
 * 实现 [FriendMessageEvent], 但是不同于 [MiraiFriendMessageEvent].
 */
public interface MiraiFriendNudgeEvent : MiraiNudgeEvent, FriendMessageEvent {
    @OptIn(Api4J::class)
    override val source: MiraiFriend
    override suspend fun source(): MiraiFriend = source


}


/**
 * 在陌生人处收到的戳一戳事件。
 *
 * 实现 [ContactMessageEvent], 但是不同于 [MiraiStrangerMessageEvent].
 */
public interface MiraiStrangerNudgeEvent : MiraiNudgeEvent, ContactMessageEvent {
    @OptIn(Api4J::class)
    override val source: MiraiStranger
    override suspend fun source(): MiraiStranger = source

}