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

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.event.MiraiFriendMessageEvent
import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.component.mirai.event.toSimbotMessageContent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.randomID
import love.forte.simbot.utils.runInBlocking
import java.time.Instant
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend
import net.mamoe.mirai.event.events.FriendMessageEvent as OriginalMiraiFriendMessageEvent


internal class MiraiFriendMessageEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: OriginalMiraiFriendMessageEvent,
) : MiraiFriendMessageEvent {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.byInstant(Instant.ofEpochSecond(originalEvent.time.toLong()))
    override val messageContent: MiraiReceivedMessageContent = originalEvent.toSimbotMessageContent()
    
    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(Api4J::class)
    private val _friend = originalEvent.friend.asSimbot(bot)
    
    override suspend fun friend(): MiraiFriend = _friend
    
    
    // region send api
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend> = reply(message)
    
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend> = reply(text)
    
    
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend> =
        send(message.messages)
    // endregion
    
    
    // region reply api
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend> {
        val miraiMessage = message.toOriginalMiraiMessage(originalEvent.friend)
        val receipt = originalEvent.friend.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend> {
        val receipt = originalEvent.friend.sendMessage(text)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend> =
        runInBlocking { reply(message.messages) }
    // endregion
    
    
}
