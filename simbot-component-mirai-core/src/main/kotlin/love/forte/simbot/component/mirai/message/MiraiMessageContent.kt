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

