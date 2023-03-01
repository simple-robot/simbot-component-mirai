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
