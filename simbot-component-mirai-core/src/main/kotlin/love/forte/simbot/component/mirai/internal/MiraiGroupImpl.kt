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

package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import love.forte.simbot.*
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceiptImpl
import love.forte.simbot.component.mirai.internal.MiraiGroupMemberBotImpl.Companion.toMemberBot
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import net.mamoe.mirai.contact.getMember
import java.util.stream.Stream
import kotlin.time.Duration
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup


/**
 *
 * @author ForteScarlet
 */
internal class MiraiGroupImpl(
    private val baseBot: MiraiBotImpl,
    override val originalContact: OriginalMiraiGroup,
    private val initOwner: MiraiMemberImpl? = null
) : MiraiGroup {
    private lateinit var memberBot: MiraiGroupMemberBotImpl
    
    override val bot: MiraiGroupMemberBotImpl
        get() {
            if (::memberBot.isInitialized) {
                return memberBot
            }
    
            // 不关心线程安全
            return baseBot.toMemberBot(originalContact.botAsMember.asSimbot(baseBot, this)).also {
                memberBot = it
            }
        }

    override val id: LongID = originalContact.id.ID


    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val receipt = originalContact.sendMessage(message.toOriginalMiraiMessage(originalContact))
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        return SimbotMiraiMessageReceiptImpl(originalContact.sendMessage(text))
    }

    override suspend fun member(id: ID): MiraiMember? {
        return originalContact.getMember(id.tryToLongID().number)?.asSimbot(baseBot, this)
    }

    @OptIn(Api4J::class)
    override val owner: MiraiMemberImpl
        get() = initOwner ?: originalContact.owner.asSimbot(baseBot, this)


    override val ownerId: LongID get() = owner.id

    override fun getMembers(groupingId: ID?, limiter: Limiter): Stream<MiraiMemberImpl> {
        return originalContact.members.stream().map { it.asSimbot(baseBot) }.withLimiter(limiter)
    }

    override suspend fun members(groupingId: ID?, limiter: Limiter): Flow<MiraiMemberImpl> {
        return originalContact.members.asFlow().map { it.asSimbot(baseBot) }.withLimiter(limiter)
    }

    override suspend fun mute(duration: Duration): Boolean {
        val seconds = duration.inWholeSeconds
        if (seconds < 0) return false
        originalContact.settings.isMuteAll = true
        if (seconds > 0) {
            bot.launch {
                // TODO cancel if unmute
                kotlin.runCatching {
                    delay(duration.inWholeMilliseconds)
                    originalContact.settings.isMuteAll = false
                }
            }
        }

        return true
    }
}


internal fun OriginalMiraiGroup.asSimbot(bot: MiraiBotImpl): MiraiGroupImpl =
    bot.computeGroup(this) { MiraiGroupImpl(bot, this) }

internal fun OriginalMiraiGroup.asSimbot(bot: MiraiBotImpl, initOwner: MiraiMemberImpl): MiraiGroupImpl =
    bot.computeGroup(this) { MiraiGroupImpl(bot, this, initOwner) }