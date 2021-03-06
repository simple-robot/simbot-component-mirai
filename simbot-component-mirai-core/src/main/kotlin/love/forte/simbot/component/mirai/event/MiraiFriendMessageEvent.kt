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
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.MiraiStranger
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.event.*
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger
import net.mamoe.mirai.event.events.FriendMessageEvent as OriginalMiraiFriendMessageEvent
import net.mamoe.mirai.event.events.StrangerMessageEvent as OriginalMiraiStrangerMessageEvent


/**
 * 好友消息事件。
 *
 * Mirai [OriginalMiraiFriendMessageEvent] 事件对应的 [FriendMessageEvent] 事件类型。
 *
 * @see OriginalMiraiFriendMessageEvent
 * @author ForteScarlet
 */
public interface MiraiFriendMessageEvent :
    MiraiSimbotContactMessageEvent<OriginalMiraiFriendMessageEvent>,
    MiraiFriendEvent<OriginalMiraiFriendMessageEvent>,
    FriendMessageEvent, ReplySupport, SendSupport {
    override val key: Event.Key<MiraiFriendMessageEvent> get() = Key


    @OptIn(Api4J::class)
    override val user: MiraiFriend

    @JvmSynthetic
    override suspend fun user(): MiraiFriend

    @OptIn(Api4J::class)
    override val source: MiraiFriend


    @JvmSynthetic
    override suspend fun source(): MiraiFriend

    //region send api
    @JvmSynthetic
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>


    @JvmSynthetic
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    //endregion


    //region reply api
    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>


    @JvmSynthetic
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    //endregion

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
    MiraiSimbotContactMessageEvent<OriginalMiraiStrangerMessageEvent>,
    ContactMessageEvent, ReplySupport, SendSupport {
    override val key: Event.Key<MiraiStrangerMessageEvent> get() = Key

    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(Api4J::class)
    override val user: MiraiStranger

    @JvmSynthetic
    override suspend fun user(): MiraiStranger


    @OptIn(Api4J::class)
    override val source: MiraiStranger

    @JvmSynthetic
    override suspend fun source(): MiraiStranger



    //region reply api
    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @JvmSynthetic
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    //endregion


    //region send api
    @JvmSynthetic
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>


    @JvmSynthetic
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    //endregion


    public companion object Key :
        BaseEventKey<MiraiStrangerMessageEvent>(
            "mirai.stranger_message",
            MiraiSimbotContactMessageEvent, ContactMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiStrangerMessageEvent? = doSafeCast(value)
    }
}