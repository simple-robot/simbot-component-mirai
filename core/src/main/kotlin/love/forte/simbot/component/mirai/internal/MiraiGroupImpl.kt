package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.toNativeMiraiMessage
import love.forte.simbot.message.Message
import net.mamoe.mirai.contact.getMember
import java.util.stream.Stream
import kotlin.time.Duration


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