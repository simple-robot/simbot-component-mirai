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
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.announcement.MiraiAnnouncements
import love.forte.simbot.component.mirai.internal.MiraiGroupBotImpl.Companion.getGroupBot
import love.forte.simbot.component.mirai.internal.announcement.MiraiAnnouncementsImpl
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.message.Message
import love.forte.simbot.utils.item.Items
import love.forte.simbot.utils.item.Items.Companion.asItems
import love.forte.simbot.utils.item.map
import net.mamoe.mirai.contact.GroupSettings
import net.mamoe.mirai.contact.active.GroupActive
import net.mamoe.mirai.contact.getMember
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup


/**
 *
 * @author ForteScarlet
 */
internal class MiraiGroupImpl(
    internal val baseBot: MiraiBotImpl,
    override val originalContact: OriginalMiraiGroup,
    initOwner: MiraiMemberImpl? = null,
) : MiraiGroup {
    private lateinit var memberBot: MiraiGroupBotImpl

    override val bot: MiraiGroupBotImpl
        get() {
            if (::memberBot.isInitialized) {
                return memberBot
            }

            // 不关心线程安全
            return getGroupBot(baseBot).also {
                memberBot = it
            }
        }

    override val id: LongID = originalContact.id.ID

    override val active: MiraiGroupActive
        get() = MiraiGroupActiveImpl(originalContact.active)

    override val settings: MiraiGroupSettings
        get() = MiraiGroupSettingsImpl(originalContact.settings)

    override val announcements: MiraiAnnouncements
        get() = MiraiAnnouncementsImpl(this, originalContact.announcements)

    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        val receipt = originalContact.sendMessage(message.toOriginalMiraiMessage(originalContact))
        return SimbotMiraiMessageReceiptImpl(receipt)
    }

    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiGroup> {
        return SimbotMiraiMessageReceiptImpl(originalContact.sendMessage(text))
    }

    override suspend fun member(id: ID): MiraiMember? {
        return originalContact.getMember(id.tryToLong())?.asSimbot(baseBot, this)
    }

    private val _owner = initOwner ?: originalContact.owner.asSimbot(baseBot, this)

    override suspend fun owner(): MiraiMember = _owner

    override val ownerId: LongID get() = _owner.id

    override val members: Items<MiraiMember>
        get() = originalContact.members.asItems().map { it.asSimbot(baseBot) }


    override val roles: Items<MemberRole>
        get() = MemberRole.values().asList().asItems()

    override suspend fun mute(duration: Duration): Boolean {
        baseBot.groupMute(originalContact, duration.inWholeMilliseconds)
        return true
    }

    override suspend fun mute(time: Long, timeUnit: TimeUnit): Boolean {
        baseBot.groupMute(originalContact, timeUnit.toMillis(time))
        return true
    }

    @Api4J
    override fun muteBlocking(duration: JavaDuration): Boolean {
        baseBot.groupMute(originalContact, duration.toMillis())
        return true
    }

    @Api4J
    override fun muteBlocking(): Boolean {
        baseBot.groupMute(originalContact, 0)
        return true
    }

    override suspend fun unmute(): Boolean {
        return baseBot.groupUnmute(originalContact)
    }

}

internal fun OriginalMiraiGroup.asSimbot(bot: MiraiBotImpl): MiraiGroupImpl =
    bot.computeGroup(this) { MiraiGroupImpl(bot, this) }

internal fun OriginalMiraiGroup.asSimbot(bot: MiraiBotImpl, initOwner: MiraiMemberImpl): MiraiGroupImpl =
    bot.computeGroup(this) { MiraiGroupImpl(bot, this, initOwner) }

internal class MiraiGroupActiveImpl(override val originalGroupActive: GroupActive) : MiraiGroupActive {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MiraiGroupActiveImpl

        if (originalGroupActive != other.originalGroupActive) return false

        return true
    }

    override fun hashCode(): Int {
        return originalGroupActive.hashCode()
    }

    override fun toString(): String {
        return "MiraiGroupActiveImpl(originalGroupActive=$originalGroupActive)"
    }


}


internal class MiraiGroupSettingsImpl(override val originalGroupSettings: GroupSettings) : MiraiGroupSettings {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MiraiGroupSettingsImpl

        if (originalGroupSettings != other.originalGroupSettings) return false

        return true
    }

    override fun hashCode(): Int {
        return originalGroupSettings.hashCode()
    }

    override fun toString(): String {
        return "MiraiGroupSettingsImpl(originalGroupSettings=$originalGroupSettings)"
    }


}


