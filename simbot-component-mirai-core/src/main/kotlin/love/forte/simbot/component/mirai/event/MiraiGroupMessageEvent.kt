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
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.event.*
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup
import net.mamoe.mirai.event.events.GroupMessageEvent as OriginalMiraiGroupMessageEvent


/**
 * 群消息事件。
 *
 * Mirai [OriginalMiraiGroupMessageEvent] 事件对应的 [FriendMessageEvent] 事件类型。
 *
 * @see OriginalMiraiGroupMessageEvent
 * @author ForteScarlet
 */
public interface MiraiGroupMessageEvent :
    MiraiSimbotGroupMessageEvent<OriginalMiraiGroupMessageEvent>,
    GroupMessageEvent, ReplySupport, SendSupport {
    override val bot: MiraiBot

    /**
     * 收到的消息本体。
     */
    override val messageContent: MiraiReceivedMessageContent


    /**
     * 此消息的发送者。
     */
    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(Api4J::class)
    override val author: MiraiMember


    /**
     * 此消息的发送者。
     */
    @JvmSynthetic
    override suspend fun author(): MiraiMember

    /**
     * 收到消息的群。
     */
    @OptIn(Api4J::class)
    override val group: MiraiGroup

    /**
     * 收到消息的群。
     */
    @JvmSynthetic
    override suspend fun group(): MiraiGroup

    /**
     * 收到消息的群。同 [group].
     */
    @OptIn(Api4J::class)
    override val organization: MiraiGroup

    /**
     * 收到消息的群。同 [group].
     */
    @JvmSynthetic
    override suspend fun organization(): MiraiGroup

    /**
     * 收到消息的群。同 [group].
     */
    @OptIn(Api4J::class)
    override val source: MiraiGroup

    /**
     * 收到消息的群。同 [group].
     */
    @JvmSynthetic
    override suspend fun source(): MiraiGroup

    //// api

    /**
     * 删除，或者说撤回这个消息。
     *
     * 当无权限时会抛出异常。
     *
     * @see net.mamoe.mirai.message.data.MessageSource.recall
     */
    @JvmSynthetic
    public suspend fun recall(): Boolean


    //region reply api
    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup>


    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    @JvmSynthetic
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiGroup>
    //endregion


    //region send api
    /**
     * 向此事件的群发送消息。
     */
    @JvmSynthetic
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 向此事件的群发送消息。
     */
    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup>


    /**
     * 向此事件的群发送消息。
     */
    @JvmSynthetic
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 向此事件的群发送消息。
     */
    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 向此事件的群发送消息。
     */
    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 向此事件的群发送消息。
     */
    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiGroup>
    //endregion

    override val key: Event.Key<MiraiGroupMessageEvent> get() = Key


    public companion object Key :
        BaseEventKey<MiraiGroupMessageEvent>(
            "mirai.group_message",
            MiraiSimbotGroupMessageEvent, GroupMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiGroupMessageEvent? = doSafeCast(value)
    }

}