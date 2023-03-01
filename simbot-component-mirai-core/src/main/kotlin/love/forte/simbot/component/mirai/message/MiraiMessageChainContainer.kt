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

import love.forte.simbot.component.mirai.event.MiraiReceivedMessageContent
import love.forte.simbot.definition.Container
import love.forte.simbot.message.MessageContent
import net.mamoe.mirai.message.data.MessageChain

/**
 * 代表为一个能够得到 [mirai原生消息链][MessageChain] 的容器。
 *
 * 此容器的常见实现者有由mirai组件中的 [MessageContent] 类型对象实现，
 * 例如 [MiraiReceivedMessageContent] 或 [MiraiMessageChainContent]。
 *
 *
 * @author ForteScarlet
 */
public interface MiraiMessageChainContainer : Container {
    
    /**
     * 得到当前容器中存在的原始的mirai消息链。
     */
    public val originalMessageChain: MessageChain
    
    
}
