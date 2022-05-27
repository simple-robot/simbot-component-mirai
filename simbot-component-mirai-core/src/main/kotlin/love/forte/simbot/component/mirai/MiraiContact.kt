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

package love.forte.simbot.component.mirai

import love.forte.simbot.LongID
import love.forte.simbot.definition.*
import net.mamoe.mirai.contact.Contact as OriginalMiraiContact
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup


/**
 * [MiraiBot] 容器类型。
 */
public interface MiraiBotContainer : BotContainer {
    override val bot: MiraiBot
}


/**
 * 包含了mirai原生联系人[OriginalMiraiContact] 的容器类型.
 */
public interface MiraiContactContainer {
    public val originalContact: OriginalMiraiContact
}


/**
 * [Contact] 对应Mirai的 [联系人][OriginalMiraiContact] 类型。
 *
 * @author ForteScarlet
 *
 * @see MiraiFriend
 * @see MiraiMember
 */
public interface MiraiContact : Contact, MiraiBotContainer, MiraiContactContainer {
    override val bot: MiraiBot
    override val originalContact: OriginalMiraiContact
    override val id: LongID

}

/**
 * [ChatRoom] 对应Mirai的 [联系人][OriginalMiraiContact] 类型。
 *
 * @see MiraiGroup
 */
public interface MiraiChatroom : ChatRoom, MiraiBotContainer, MiraiContactContainer {
    override val bot: MiraiGroupMemberBot
    override val originalContact: OriginalMiraiGroup
    override val id: LongID


}