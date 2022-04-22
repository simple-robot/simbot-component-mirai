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
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.action.SendSupport
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.Event
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.Member as OriginalMiraiMember
import net.mamoe.mirai.event.events.GroupTempMessageEvent as OriginalMiraiGroupTempMessageEvent


/**
 * @see OriginalMiraiGroupTempMessageEvent
 */
public typealias OriginalMiraiGroupTempMessageEvent = OriginalMiraiGroupTempMessageEvent


/**
 * mirai群临时会话事件。
 *
 * @author ForteScarlet
 */
public interface MiraiMemberMessageEvent
    : MiraiSimbotContactMessageEvent<OriginalMiraiGroupTempMessageEvent>,
    ContactMessageEvent, ReplySupport, SendSupport {

    override val bot: MiraiBot

    /**
     * 发送消息的群成员。
     */
    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(Api4J::class)
    override val user: MiraiMember

    /**
     * 发送消息的群成员。
     */
    @JvmSynthetic
    override suspend fun user(): MiraiMember

    /**
     * 发送消息的群成员。
     */
    @OptIn(Api4J::class)
    override val source: MiraiMember

    /**
     * 发送消息的群成员。
     */
    @JvmSynthetic
    override suspend fun source(): MiraiMember



    //region reply api

    /**
     * 回复此群成员的消息。效果等同于 [send].
     */
    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    /**
     * 回复此群成员的消息。效果等同于 [send].
     */
    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    /**
     * 回复此群成员的消息。效果等同于 [send].
     */
    @JvmSynthetic
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    /**
     * 回复此群成员的消息。效果等同于 [send].
     */
    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    /**
     * 回复此群成员的消息。效果等同于 [send].
     */
    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    /**
     * 回复此群成员的消息。效果等同于 [send].
     */
    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    //endregion

    //region send api
    /**
     * 向此群成员发送消息。效果等同于 [reply].
     */
    @JvmSynthetic
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    /**
     * 向此群成员发送消息。效果等同于 [reply].
     */
    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>


    /**
     * 向此群成员发送消息。效果等同于 [reply].
     */
    @JvmSynthetic
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    /**
     * 向此群成员发送消息。效果等同于 [reply].
     */
    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    /**
     * 向此群成员发送消息。效果等同于 [reply].
     */
    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>

    /**
     * 向此群成员发送消息。效果等同于 [reply].
     */
    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    //endregion

    override val key: Event.Key<MiraiMemberMessageEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiMemberMessageEvent>(
        "mirai.group_temp_message",
        MiraiSimbotContactMessageEvent, ContactMessageEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberMessageEvent? = doSafeCast(value)
    }

}
