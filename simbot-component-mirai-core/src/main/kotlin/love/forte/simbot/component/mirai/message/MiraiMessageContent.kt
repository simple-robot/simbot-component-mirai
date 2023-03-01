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
import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.component.mirai.event.MiraiReceivedNudgeMessageContent
import love.forte.simbot.message.MessageContent
import love.forte.simbot.randomID
import net.mamoe.mirai.message.data.MessageSource


/**
 * 由mirai组件中与 [MessageContent] 相关的类实现，提供统一约束。
 *
 * @see MiraiMessageChainContent
 * @see MiraiReceivedMessageContent
 * @see MiraiReceivedNudgeMessageContent
 * @author ForteScarlet
 */
public interface MiraiMessageContent {

    /**
     * 消息ID, 同 [MessageContent.messageId], 代表当前消息中的ID, 通常由 [MessageSource.ID][love.forte.simbot.component.mirai.ID] 计算而得,
     * 可能存在一定的内容丢失。
     *
     * 如果当前消息本体中无法得到 [MessageSource](即无法通过[MessageSource.ID][love.forte.simbot.component.mirai.ID] 计算), 则其消息格式可能有所不同，
     * 例如通过 [randomID] 代替。
     *
     */
    public val messageId: ID

    /**
     * 完整的消息ID, 如果当前消息本体中可能存在 [MessageSource],
     * 则通常为 [MessageSource.fullSerialID][love.forte.simbot.component.mirai.fullSerialID] 计算而得,
     * 否则与 [messageId] 结果一致。
     */
    public val fullMessageId: ID

}

