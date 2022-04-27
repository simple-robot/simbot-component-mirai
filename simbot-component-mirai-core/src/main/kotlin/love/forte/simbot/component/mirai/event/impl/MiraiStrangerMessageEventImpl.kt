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

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiStranger
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.component.mirai.event.MiraiStrangerMessageEvent
import love.forte.simbot.component.mirai.event.toSimbotMessageContent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.randomID
import love.forte.simbot.utils.runInBlocking
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger
import net.mamoe.mirai.event.events.StrangerMessageEvent as OriginalMiraiStrangerMessageEvent


/**
 *
 * @author ForteScarlet
 */
internal data class MiraiStrangerMessageEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiStrangerMessageEvent,
) : MiraiStrangerMessageEvent {
    override val id: ID = randomID()

    override val timestamp: Timestamp = Timestamp.Companion.bySecond(originalEvent.time.toLong())
    override val user: MiraiStranger = originalEvent.stranger.asSimbot(bot)
    override val messageContent: MiraiReceivedMessageContent = originalEvent.toSimbotMessageContent()
    override val source: MiraiStranger get() = user

    override suspend fun user(): MiraiStranger = user
    override suspend fun source(): MiraiStranger = source


    //region reply api
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger> {
        val miraiMessage = message.toOriginalMiraiMessage(originalEvent.stranger)
        val receipt = originalEvent.stranger.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger> {
        return SimbotMiraiMessageReceiptImpl(originalEvent.stranger.sendMessage(text))
    }

    @JvmSynthetic
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        reply(message.messages)

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        runInBlocking { send(text) }

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        runInBlocking { send(message) }

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        runInBlocking { send(message) }
    //endregion

    //region send api
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger> = reply(message)
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger> = reply(text)

    @JvmSynthetic
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        send(message.messages)

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        runInBlocking { send(text) }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        runInBlocking { send(message) }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        runInBlocking { send(message) }
    //endregion
}