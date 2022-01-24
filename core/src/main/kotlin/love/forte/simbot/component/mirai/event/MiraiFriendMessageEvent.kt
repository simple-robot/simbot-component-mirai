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
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.Event
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import love.forte.simbot.utils.runInBlocking

/**
 * @see net.mamoe.mirai.event.events.FriendMessageEvent
 */
public typealias NativeMiraiFriendMessageEvent = net.mamoe.mirai.event.events.FriendMessageEvent

/**
 * @see net.mamoe.mirai.event.events.StrangerMessageEvent
 */
public typealias NativeMiraiStrangerMessageEvent = net.mamoe.mirai.event.events.StrangerMessageEvent

/**
 * 好友消息事件。
 *
 * Mirai [NativeMiraiFriendMessageEvent] 事件对应的 [FriendMessageEvent] 事件类型。
 *
 * @see NativeMiraiFriendMessageEvent
 * @author ForteScarlet
 */
public interface MiraiFriendMessageEvent :
    MiraiSimbotContactMessageEvent<NativeMiraiFriendMessageEvent>,
    MiraiFriendEvent<NativeMiraiFriendMessageEvent>,
    FriendMessageEvent, ReplySupport, SendSupport {

    override val bot: MiraiBot
    override val metadata: Metadata

    @OptIn(Api4J::class)
    override val friend: MiraiFriend
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend>
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend>
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend>
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend>

    //// impl

    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PRIVATE
    override val key: Event.Key<MiraiFriendMessageEvent> get() = Key
    override suspend fun user(): MiraiFriend = friend
    override suspend fun source(): MiraiFriend = friend
    override suspend fun friend(): MiraiFriend = friend


    @OptIn(Api4J::class)
    override val source: MiraiFriend
        get() = friend

    @OptIn(Api4J::class)
    override val user: MiraiFriend
        get() = friend


    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiFriend> =
        send(message.messages)

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend> = runInBlocking { send(text) }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend> =
        runInBlocking { send(message) }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiFriend> =
        runInBlocking { send(message) }


    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiFriend> =
        runInBlocking { reply(message.messages) }

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend> =
        runInBlocking { reply(text) }

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend> =
        runInBlocking { reply(message) }

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiFriend> =
        runInBlocking { reply(message) }

    /**
     * Metadata for [MiraiFriendMessageEvent].
     */
    public interface Metadata : MiraiSimbotEvent.Metadata<NativeMiraiFriendMessageEvent>

    public companion object Key :
        BaseEventKey<MiraiFriendMessageEvent>(
            "mirai.friend_message",
            MiraiSimbotContactMessageEvent, MiraiFriendEvent, FriendMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiFriendMessageEvent? = doSafeCast(value)
    }
}


/**
 * Mirai陌生人消息事件。
 */
public interface MiraiStrangerMessageEvent :
    MiraiSimbotContactMessageEvent<NativeMiraiStrangerMessageEvent>,
    ContactMessageEvent, ReplySupport, SendSupport {

    override val bot: MiraiBot
    override val key: Event.Key<MiraiStrangerMessageEvent> get() = Key
    override val metadata: Metadata

    @OptIn(Api4J::class)
    override val user: MiraiStranger


    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiStranger>
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiStranger>
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<NativeMiraiStranger>
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<NativeMiraiStranger>

    //// Impl

    override suspend fun user(): MiraiStranger = user


    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiStranger> =
        reply(message.messages)

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiStranger> =
        runInBlocking { send(text) }

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiStranger> =
        runInBlocking { send(message) }

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiStranger> =
        runInBlocking { send(message) }

    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiStranger> =
        send(message.messages)

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiStranger> =
        runInBlocking { send(text) }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiStranger> =
        runInBlocking { send(message) }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiStranger> =
        runInBlocking { send(message) }


    /**
     * Metadata for [MiraiStrangerMessageEvent].
     */
    public interface Metadata : MiraiSimbotEvent.Metadata<NativeMiraiStrangerMessageEvent>

    public companion object Key :
        BaseEventKey<MiraiStrangerMessageEvent>(
            "mirai.stranger_message",
            MiraiSimbotContactMessageEvent, ContactMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiStrangerMessageEvent? = doSafeCast(value)
    }
}