package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.Limiter
import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.definition.Role
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
    override val nativeGroup: NativeMiraiGroup,
) : MiraiGroup {

    override val id: LongID = nativeGroup.id.ID


    override suspend fun send(message: Message): MessageReceipt {
        val receipt = nativeGroup.sendMessage(message.toNativeMiraiMessage(nativeGroup))
        return SimbotMiraiMessageReceipt(receipt)
    }

    @Api4J
    override fun getRoles(groupingId: ID?, limiter: Limiter): Stream<out Role> {
        TODO("Not yet implemented")
    }

    override suspend fun roles(groupingId: ID?, limiter: Limiter): Flow<Role> {
        TODO("Not yet implemented")
    }


    @OptIn(Api4J::class)
    override val owner: MiraiMemberImpl = nativeGroup.owner.asSimbotMember(bot)
    override val ownerId: LongID = nativeGroup.owner.id.ID
    override suspend fun owner(): MiraiMember = owner

    @Api4J
    override fun getMembers(groupingId: ID?, limiter: Limiter): Stream<MiraiMemberImpl> {
        return nativeGroup.members.stream().map { it.asSimbotMember(bot) }
    }

    override suspend fun members(groupingId: ID?, limiter: Limiter): Flow<MiraiMemberImpl> {
        return nativeGroup.members.asFlow().map { it.asSimbotMember(bot) }
    }

    override suspend fun mute(duration: Duration): Boolean {
        val seconds = duration.inWholeSeconds
        if (seconds < 0) return false
        nativeGroup.settings.isMuteAll = true
        if (seconds > 0) {
            bot.launch {
                kotlin.runCatching {
                    delay(duration.inWholeMilliseconds)
                    nativeGroup.settings.isMuteAll = false
                }
            }
        }

        return true
    }
}