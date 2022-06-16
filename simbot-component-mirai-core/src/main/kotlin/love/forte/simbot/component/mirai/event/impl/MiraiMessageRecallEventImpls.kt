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

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiFriend
import love.forte.simbot.component.mirai.event.MiraiFriendMessageRecallEvent
import love.forte.simbot.component.mirai.event.MiraiGroupMessageRecallEvent
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.component.mirai.internal.MiraiGroupImpl
import love.forte.simbot.component.mirai.internal.MiraiMemberImpl
import love.forte.simbot.component.mirai.internal.asSimbot
import love.forte.simbot.component.mirai.message.toSimbot
import love.forte.simbot.message.Messages
import love.forte.simbot.randomID
import net.mamoe.mirai.event.events.MessageRecallEvent


internal class MiraiFriendMessageRecallEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: MessageRecallEvent.FriendRecall,
) : MiraiFriendMessageRecallEvent() {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.now()
    override val friend: MiraiFriend = originalEvent.author.asSimbot(bot)
    
    override val messages: Messages? =
        bot.recallMessageCacheStrategy.getFriendMessageCache(bot, originalEvent)?.toSimbot()
}

internal class MiraiGroupMessageRecallEventImpl(
    override val bot: MiraiBotImpl,
    override val originalEvent: MessageRecallEvent.GroupRecall,
) : MiraiGroupMessageRecallEvent() {
    override val id: ID = randomID()
    override val timestamp: Timestamp = Timestamp.now()
    override val group: MiraiGroupImpl = originalEvent.group.asSimbot(bot)
    override val author: MiraiMemberImpl = originalEvent.author.asSimbot(bot, group)
    override val operator: MiraiMemberImpl? = originalEvent.operator?.asSimbot(bot, group)
    
    override val messages: Messages? =
        bot.recallMessageCacheStrategy.getGroupMessageCache(bot, originalEvent)?.toSimbot()
}


