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

import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.*
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.message.*
import java.time.*


internal class MiraiFriendMessageEventImpl(
    override val bot: MiraiBotImpl,
    override val nativeEvent: NativeMiraiFriendMessageEvent
) : MiraiFriendMessageEvent {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.byInstant(Instant.ofEpochSecond(nativeEvent.time.toLong()))
    override val messageContent: MiraiReceivedMessageContent = nativeEvent.toSimbotMessageContent()

    override val friend = nativeEvent.friend.asSimbot(bot)

    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend> {
        val miraiMessage = message.toNativeMiraiMessage(nativeEvent.friend)
        val receipt = nativeEvent.friend.sendMessage(miraiMessage)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend> {
        val receipt = nativeEvent.friend.sendMessage(text)
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend> = reply(message)

    @JvmSynthetic
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend> = reply(text)


}