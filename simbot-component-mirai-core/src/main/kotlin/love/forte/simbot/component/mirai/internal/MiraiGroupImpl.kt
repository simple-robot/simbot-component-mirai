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

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.message.*
import net.mamoe.mirai.contact.*
import java.util.stream.*
import kotlin.time.*


/**
 *
 * @author ForteScarlet
 */
internal class MiraiGroupImpl(
    override val bot: MiraiBotImpl,
    override val nativeContact: NativeMiraiGroup,
    private val initOwner: MiraiMemberImpl? = null
) : MiraiGroup {

    override val id: LongID = nativeContact.id.ID


    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiGroup> {
        val receipt = nativeContact.sendMessage(message.toNativeMiraiMessage(nativeContact))
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiGroup> {
        return SimbotMiraiMessageReceiptImpl(nativeContact.sendMessage(text))
    }

    override suspend fun member(id: ID): MiraiMember? {
        return nativeContact.getMember(id.tryToLongID().number)?.asSimbot(bot, this)
    }

    @OptIn(Api4J::class)
    override val owner: MiraiMemberImpl
        get() = initOwner ?: nativeContact.owner.asSimbot(bot, this)


    override val ownerId: LongID get() = owner.id

    override fun getMembers(groupingId: ID?, limiter: Limiter): Stream<MiraiMemberImpl> {
        return nativeContact.members.stream().map { it.asSimbot(bot) }.withLimiter(limiter)
    }

    override suspend fun members(groupingId: ID?, limiter: Limiter): Flow<MiraiMemberImpl> {
        return nativeContact.members.asFlow().map { it.asSimbot(bot) }.withLimiter(limiter)
    }

    override suspend fun mute(duration: Duration): Boolean {
        val seconds = duration.inWholeSeconds
        if (seconds < 0) return false
        nativeContact.settings.isMuteAll = true
        if (seconds > 0) {
            bot.launch {
                kotlin.runCatching {
                    delay(duration.inWholeMilliseconds)
                    nativeContact.settings.isMuteAll = false
                }
            }
        }

        return true
    }
}


internal fun NativeMiraiGroup.asSimbot(bot: MiraiBotImpl): MiraiGroupImpl =
    bot.computeGroup(this) { MiraiGroupImpl(bot, this) }

internal fun NativeMiraiGroup.asSimbot(bot: MiraiBotImpl, initOwner: MiraiMemberImpl): MiraiGroupImpl =
    bot.computeGroup(this) { MiraiGroupImpl(bot, this, initOwner) }