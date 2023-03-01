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

package love.forte.simbot.component.mirai


/**
 * 可得到部分自定义配置属性的 [MiraiRecallMessageCacheStrategy] 实现。
 *
 * @author ForteScarlet
 */
public abstract class CustomPropertiesMiraiRecallMessageCacheStrategy : MiraiRecallMessageCacheStrategy {
    /**
     * 等待初始化的属性表。[properties] 在默认情况下不应该在初始化阶段使用，且应当在当前类被实例化后迫切地进行初始化。
     */
    public lateinit var properties: Map<String, String>
    
    
}
