package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import love.forte.simbot.*
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.NativeMiraiGroup
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageReceipt
import java.util.stream.Stream
import kotlin.time.Duration


/**
 *
 * @author ForteScarlet
 */
internal class MiraiGroupImpl(
    override val bot: MiraiBotImpl,
    override val nativeContact: NativeMiraiGroup,
) : MiraiGroup {

    override val id: LongID = nativeContact.id.ID


    override suspend fun send(message: Message): MessageReceipt {
        val receipt = nativeContact.sendMessage(message.toNativeMiraiMessage(nativeContact))
        return SimbotMiraiMessageReceipt(receipt)
    }


    @OptIn(Api4J::class)
    override val owner: MiraiMemberImpl = nativeContact.owner.asSimbotMember(bot)
    override val ownerId: LongID = nativeContact.owner.id.ID

    override fun getMembers(groupingId: ID?, limiter: Limiter): Stream<MiraiMemberImpl> {
        return nativeContact.members.stream().map { it.asSimbotMember(bot) }.withLimiter(limiter)
    }

    override suspend fun members(groupingId: ID?, limiter: Limiter): Flow<MiraiMemberImpl> {
        return nativeContact.members.asFlow().map { it.asSimbotMember(bot) }.withLimiter(limiter)
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


internal fun NativeMiraiGroup.asSimbot(bot: MiraiBotImpl): MiraiGroupImpl = MiraiGroupImpl(bot, this)