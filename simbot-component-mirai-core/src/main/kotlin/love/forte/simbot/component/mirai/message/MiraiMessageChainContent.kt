/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  This file is part of simbot-component-mirai.
 *
 *  simbot-component-mirai is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  simbot-component-mirai is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with simbot-component-mirai. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package love.forte.simbot.component.mirai.message

import love.forte.simbot.ID
import love.forte.simbot.component.mirai.ID
import love.forte.simbot.component.mirai.fullSerialID
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.Messages
import love.forte.simbot.message.toMessages
import love.forte.simbot.randomID
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.sourceOrNull

/**
 *
 * 直接将一个 [MessageChain] 作为一个简单的 [MessageContent] 对象实现。
 *
 * @author ForteScarlet
 */
public open class MiraiMessageChainContent(
    final override val originalMessageChain: MessageChain,
    public val messageSourceOrNull: MessageSource? = originalMessageChain.sourceOrNull,
) : MessageContent(),
    MiraiMessageContent,
    MiraiMessageChainContainer {

    /**
     * 当前消息的ID。
     * 当 [originalMessageChain] 中的 [MessageSource] 存在时, 使用 [MessageSource.ID] 计算ID，
     * 当 [originalMessageChain] 中的 [MessageSource] 不存在时会使用 [randomID] 作为消息ID.
     */
    override val messageId: ID by lazy(LazyThreadSafetyMode.PUBLICATION) { messageSourceOrNull?.ID ?: randomID() }


    /**
     * 当前消息的完整ID。
     * 当 [originalMessageChain] 中的 [MessageSource] 存在时, 使用 [MessageSource.ID] 计算ID，
     * 当 [originalMessageChain] 中的 [MessageSource] 不存在时与 [messageId] 一致。
     */
    override val fullMessageId: ID by lazy(LazyThreadSafetyMode.PUBLICATION) { messageSourceOrNull?.fullSerialID ?: messageId }

    /**
     * 消息链。
     *
     * 消息链中不追加source(如果存在的话)。
     */
    override val messages: Messages by lazy(
        LazyThreadSafetyMode.PUBLICATION,
        originalMessageChain.filter { it !is MessageSource }.map(SingleMessage::asSimbotMessage)::toMessages
    )
}
