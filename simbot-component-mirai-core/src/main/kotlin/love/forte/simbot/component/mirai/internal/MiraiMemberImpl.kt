/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
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

import love.forte.simbot.*
import love.forte.simbot.action.SendSupport
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.utils.runInBlocking
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.active.MemberActive
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import net.mamoe.mirai.contact.Member as OriginalMiraiMember


/**
 *
 * @author ForteScarlet
 */
internal class MiraiMemberImpl(
    override val bot: MiraiBotImpl,
    override val originalContact: OriginalMiraiMember,
    initGroup: MiraiGroupImpl? = null,
) : MiraiMember, SendSupport {
    override val id: LongID = originalContact.id.ID

    override suspend fun queryProfile(): MiraiUserProfile {
        return originalContact.queryProfile().asSimbot()
    }

    override val joinTime: Timestamp = when (val member = originalContact) {
        is NormalMember -> Timestamp.bySecond(member.joinTimestamp.toLong())
        else -> Timestamp.NotSupport
    }
    
    override val lastSpeakTime: Timestamp? = when (val member = originalContact) {
        is NormalMember -> Timestamp.bySecond(member.joinTimestamp.toLong())
        else -> null
    }
    
    override val muteTimeRemainingSeconds: Int = when (val member = originalContact) {
        is NormalMember -> member.muteTimeRemaining
        else -> 0
    }
    
    private var _active: MiraiMemberActive? = null
    override val active: MiraiMemberActive
        // Don't care about concurrency
        get() = _active ?: MiraiMemberActiveImpl(originalContact.active).also { _active = it }
    
    private val _group: MiraiGroupImpl = initGroup ?: originalContact.group.asSimbot(bot)
    
    override suspend fun group(): MiraiGroup = _group
    
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember> {
        val receipt = originalContact.sendMessage(message.toOriginalMiraiMessage(originalContact))
        return SimbotMiraiMessageReceiptImpl(receipt)
    }
    
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember> {
        return SimbotMiraiMessageReceiptImpl(originalContact.sendMessage(text))
    }
    
    override suspend fun kick(message: String, block: Boolean): Boolean {
        val contact = originalContact
        if (contact is NormalMember) {
            contact.kick(message, block)
            return true
        }
        return false
    }
    
    private suspend fun mute0(second: Int): Boolean {
        val s = if (second == 0) 1 else second
        return if (s > 0) {
            originalContact.mute(s)
            true
        } else {
            false
        }
    }
    
    override suspend fun mute(duration: Duration): Boolean {
        return mute0(duration.inWholeSeconds.toInt())
    }
    
    override suspend fun mute(time: Long, timeUnit: TimeUnit): Boolean {
        return mute0(timeUnit.toSeconds(time).toInt())
    }
    
    @Api4J
    override fun muteBlocking(): Boolean {
        return runInBlocking { mute0(60) }
    }
    
    @Api4J
    override fun muteBlocking(duration: JavaDuration): Boolean {
        return runInBlocking { mute0(duration.seconds.toInt()) }
    }
    
}

internal class MiraiMemberActiveImpl(override val originalMemberActive: MemberActive) : MiraiMemberActive {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as MiraiMemberActiveImpl
        
        if (originalMemberActive != other.originalMemberActive) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        return originalMemberActive.hashCode()
    }
}

internal fun OriginalMiraiMember.asSimbot(bot: MiraiBotImpl): MiraiMemberImpl =
    bot.computeMember(this) { MiraiMemberImpl(bot, this) }

internal fun OriginalMiraiMember.asSimbot(bot: MiraiBotImpl, initGroup: MiraiGroupImpl): MiraiMemberImpl =
    bot.computeMember(this) { MiraiMemberImpl(bot, this, initGroup) }
