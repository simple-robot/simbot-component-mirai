/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
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
