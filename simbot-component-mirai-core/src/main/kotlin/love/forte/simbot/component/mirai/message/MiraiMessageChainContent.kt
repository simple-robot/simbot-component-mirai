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

package love.forte.simbot.component.mirai.message

import love.forte.simbot.ID
import love.forte.simbot.component.mirai.SerialID
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
    MiraiMessageChainContainer {

    /**
     * 当前消息的ID。
     *
     * 当 [originalMessageChain] 中的 [MessageSource] 为null的时候会使用 [randomID] 作为消息ID.
     */
    override val messageId: ID by lazy(LazyThreadSafetyMode.PUBLICATION) { messageSourceOrNull?.SerialID ?: randomID() }


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
