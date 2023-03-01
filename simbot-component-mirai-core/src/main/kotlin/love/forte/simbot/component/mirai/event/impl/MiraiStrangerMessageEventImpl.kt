/*
 *  Copyright (c) 2022-2023 ForteScarlet.
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

package love.forte.simbot.component.mirai.event.impl

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
    private val _user: MiraiStranger = originalEvent.stranger.asSimbot(bot)
    override val messageContent: MiraiReceivedMessageContent = originalEvent.toSimbotMessageContent()
    
    override suspend fun user(): MiraiStranger = _user
    
    
    // region reply api
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger> {
        val miraiMessage = message.toOriginalMiraiMessage(originalEvent.stranger)
        val receipt = originalEvent.stranger.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger> {
        return SimbotMiraiMessageReceiptImpl(originalEvent.stranger.sendMessage(text))
    }
    
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        reply(message.messages)
    // endregion
    
    // region send api
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger> = reply(message)
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger> = reply(text)
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger> =
        send(message.messages)
    // endregion
}
