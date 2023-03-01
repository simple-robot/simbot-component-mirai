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

package love.forte.simbot.component.mirai.event

import love.forte.simbot.action.ReplySupport
import love.forte.simbot.action.SendSupport
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.event.*
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.source
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
    @JSTP
    override suspend fun author(): MiraiMember

    /**
     * 收到消息的群。
     */
    @JSTP
    override suspend fun group(): MiraiGroup

    /**
     * 收到消息的群。同 [group].
     */
    @JSTP
    override suspend fun organization(): MiraiGroup = group()

    /**
     * 收到消息的群。同 [group].
     */
    @JSTP
    override suspend fun source(): MiraiGroup = group()

    //// api

    /**
     * 删除，或者说撤回这个消息。
     *
     * 当无权限时会抛出异常。
     *
     * @see net.mamoe.mirai.message.data.MessageSource.recall
     */
    @JST
    public suspend fun recall(): Boolean


    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    @JST
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    @JST
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup>


    /**
     * 在当前群内**引用回复**发消息的人。会在消息开头拼接一个 QuoteReply。
     */
    @JST
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiGroup>


    //region send api
    /**
     * 向此事件的群发送消息。
     */
    @JST
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 向此事件的群发送消息。
     */
    @JST
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup>

    /**
     * 向此事件的群发送消息。
     */
    @JST
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiGroup>
    //endregion

    /**
     * 将当前消息事件中的消息设置为群精华消息。
     *
     * [setAsEssenceMessage] 内直接操作mirai原生的事件类型，相对于 [MiraiGroup.setEssenceMessage] 而言
     * 有更高的可靠性。
     *
     * @throws PermissionDeniedException 没有权限时抛出
     * @return 是否操作成功
     */
    @JST
    public suspend fun setAsEssenceMessage(): Boolean {
        val group = originalEvent.group
        return group.setEssenceMessage(originalEvent.message.source)
    }

    override val key: Event.Key<MiraiGroupMessageEvent> get() = Key


    public companion object Key :
        BaseEventKey<MiraiGroupMessageEvent>(
            "mirai.group_message",
            MiraiSimbotGroupMessageEvent, GroupMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiGroupMessageEvent? = doSafeCast(value)
    }

}
