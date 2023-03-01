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

import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.component.mirai.bot.MiraiGroupBot
import love.forte.simbot.definition.*
import net.mamoe.mirai.contact.AvatarSpec
import net.mamoe.mirai.contact.Contact as OriginalMiraiContact
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup


/**
 * [MiraiBot] 容器类型。
 */
public interface MiraiBotContainer : BotContainer {
    override val bot: MiraiBot
}


/**
 * 包含了mirai原生联系人 [OriginalMiraiContact] 的容器类型.
 *
 * @see MiraiContact
 * @see MiraiChatroom
 *
 */
public interface MiraiContactContainer {
    public val originalContact: OriginalMiraiContact
}

/**
 * mirai的原生 [OriginalMiraiContact] 类型在simbot中的基本类型。
 *
 * 是 [MiraiContact] 与 [MiraiChatroom] 父类型。通常使用目标类型在这两者中都可能的情况下。
 *
 * @see MiraiContact
 * @see MiraiChatroom
 */
public interface MiraiContactObjective : MiraiBotContainer, MiraiContactContainer

/**
 * [Contact] 对应Mirai的 [联系人][OriginalMiraiContact] 类型。
 *
 * @author ForteScarlet
 *
 * @see MiraiFriend
 * @see MiraiMember
 * @see MiraiStranger
 */
public interface MiraiContact : Contact, MiraiContactObjective {
    override val id: LongID
    override val bot: MiraiBot
    override val originalContact: OriginalMiraiContact

    /**
     * 获取头像链接。
     */
    override val avatar: String
        get() = originalContact.avatarUrl

    /**
     * 获取头像链接。
     * @param spec 头像规格，为mirai原生类型 [AvatarSpec]。
     * @see net.mamoe.mirai.contact.ContactOrBot.avatarUrl
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("getAvatar")
    public fun avatar(spec: AvatarSpec): String = originalContact.avatarUrl(spec)
}

/**
 * [ChatRoom] 对应Mirai的 [联系人][OriginalMiraiContact] 类型。
 *
 * @see MiraiGroup
 */
public interface MiraiChatroom : ChatRoom, MiraiContactObjective {
    override val id: LongID
    override val bot: MiraiGroupBot
    override val originalContact: OriginalMiraiGroup

    /**
     * 获取群头像链接
     */
    override val icon: String
        get() = originalContact.avatarUrl


    /**
     * 获取头像链接。
     * @param spec 头像规格，为mirai原生类型 [AvatarSpec]。
     * @see net.mamoe.mirai.contact.ContactOrBot.avatarUrl
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("getIcon")
    public fun icon(spec: AvatarSpec): String = originalContact.avatarUrl(spec)

}
