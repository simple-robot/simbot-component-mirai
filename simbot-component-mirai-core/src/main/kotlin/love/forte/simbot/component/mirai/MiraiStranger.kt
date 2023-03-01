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

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.definition.Contact
import love.forte.simbot.definition.Stranger
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger


/**
 *
 * Mirai的陌生人对象实例。
 * @author ForteScarlet
 */
@JvmAsync
@JvmBlocking
public interface MiraiStranger : Contact, Stranger, MiraiContact, MiraiUserProfileQueryable {
    
    override val bot: MiraiBot
    override val originalContact: OriginalMiraiStranger
    
    override val avatar: String
        get() = originalContact.avatarUrl
    
    override val username: String
        get() = originalContact.nick
    
    
    /**
     * 向此人发送消息。
     */
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    /**
     * 向此人发送消息。
     */
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    /**
     * 向此人发送消息。
     */
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
}
