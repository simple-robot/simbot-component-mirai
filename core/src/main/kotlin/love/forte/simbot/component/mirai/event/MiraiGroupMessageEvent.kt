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

import love.forte.simbot.Api4J
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.action.SendSupport
import love.forte.simbot.component.mirai.*
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.Event
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import love.forte.simbot.utils.runInBlocking
import net.mamoe.mirai.message.data.MessageSource.Key.recall


/**
 * @see net.mamoe.mirai.event.events.FriendMessageEvent
 */
public typealias NativeMiraiGroupMessageEvent = net.mamoe.mirai.event.events.GroupMessageEvent

/**
 * 群消息事件。
 *
 * Mirai [NativeMiraiFriendMessageEvent] 事件对应的 [FriendMessageEvent] 事件类型。
 *
 * @see NativeMiraiFriendMessageEvent
 * @author ForteScarlet
 */
public interface MiraiGroupMessageEvent :
    MiraiSimbotGroupMessageEvent<NativeMiraiGroupMessageEvent>,
    GroupMessageEvent, ReplySupport, SendSupport {

    override val bot: MiraiBot
    override val metadata: Metadata

    @OptIn(Api4J::class)
    override val author: MiraiMember

    @OptIn(Api4J::class)
    override val group: MiraiGroup
    override val messageContent: MiraiReceivedMessageContent

    /**
     * 删除，或者说撤回这个消息。
     *
     * 当无权限时会抛出异常。
     *
     * @see net.mamoe.mirai.message.data.MessageSource.recall
     */
    override suspend fun delete(): Boolean

    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<NativeMiraiGroup>

    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<NativeMiraiGroup>

    /**
     * 向次事件的群发送消息。
     */
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiGroup>

    /**
     * 向次事件的群发送消息。
     */
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiGroup>


    //// Impl

    override val key: Event.Key<MiraiGroupMessageEvent> get() = Key

    /**
     * 对于群消息，其可见性是 [公开的][Event.VisibleScope.PUBLIC].
     * */
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC


    override suspend fun author(): MiraiMember = author
    override suspend fun group(): MiraiGroup = group
    override suspend fun organization(): MiraiGroup = group
    override suspend fun source(): MiraiGroup = group

    @OptIn(Api4J::class)
    override val source: MiraiGroup
        get() = group

    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group


    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiGroup> =
        reply(message.messages)

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiGroup> =
        runInBlocking { reply(text) }

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiGroup> =
        runInBlocking { reply(message) }

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiGroup> =
        runInBlocking { reply(message) }


    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiGroup> =
        send(message.messages)

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiGroup> = runInBlocking { send(text) }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiGroup> =
        runInBlocking { send(message) }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiGroup> =
        runInBlocking { send(message) }

    /**
     * [MiraiGroupMessageEvent] 的元数据类型。
     */
    public interface Metadata : MiraiSimbotEvent.Metadata<NativeMiraiGroupMessageEvent>

    public companion object Key :
        BaseEventKey<MiraiGroupMessageEvent>(
            "mirai.group_message",
            MiraiSimbotGroupMessageEvent, GroupMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiGroupMessageEvent? = doSafeCast(value)
    }

}