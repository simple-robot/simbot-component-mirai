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

package love.forte.simbot.component.mirai.internal

import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.ID
import love.forte.simbot.IntID
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend


/**
 *
 * @author ForteScarlet
 */
internal class MiraiFriendImpl(
    override val bot: MiraiBotImpl,
    override val originalContact: OriginalMiraiFriend,
) : MiraiFriend {
    
    override val id = originalContact.id.ID
    
    @Volatile
    private var _category: MiraiFriendCategory? = null
    
    override val category: MiraiFriendCategory
        get() = _category ?: synchronized(this) {
            _category ?: MiraiFriendCategoryImpl(bot, originalContact.friendGroup).also { _category = it }
        }

    override suspend fun queryProfile(): MiraiUserProfile {
        return originalContact.queryProfile().asSimbot()
    }

    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend> {
        val receipt = originalContact.sendMessage(message.toOriginalMiraiMessage(originalContact))
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend> {
        return SimbotMiraiMessageReceiptImpl(originalContact.sendMessage(text))
    }
}

internal fun OriginalMiraiFriend.asSimbot(bot: MiraiBotImpl): MiraiFriendImpl =
    bot.computeFriend(this) { MiraiFriendImpl(bot, this) }


internal class MiraiFriendCategoryImpl(
    val bot: MiraiBotImpl,
    override val originalFriendGroup: FriendGroup,
) : MiraiFriendCategory {
    override val id: IntID = originalFriendGroup.id.ID
    
    @ExperimentalSimbotApi
    override val friends: Collection<MiraiFriend> by lazy {
        originalFriendGroup.friends.map { MiraiFriendImpl(bot, it) }
    }
}
