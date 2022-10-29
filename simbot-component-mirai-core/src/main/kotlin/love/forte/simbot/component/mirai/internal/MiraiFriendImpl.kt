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

package love.forte.simbot.component.mirai.internal

import love.forte.simbot.ExperimentalSimbotApi
import love.forte.simbot.ID
import love.forte.simbot.IntID
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.MiraiFriendCategory
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
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