package love.forte.simbot.component.mirai

import love.forte.simbot.LongID
import love.forte.simbot.Timestamp
import love.forte.simbot.definition.Member
import kotlin.time.Duration


public typealias NativeMiraiMember = net.mamoe.mirai.contact.Member

/**
 * TODO 注释
 * @author ForteScarlet
 */
public interface MiraiMember : Member {

    public val nativeMember: NativeMiraiMember

    override val bot: MiraiBot
    override val id: LongID

    override suspend fun mute(duration: Duration): Boolean {
        nativeMember.mute(duration.inWholeSeconds.toInt())
        return true
    }

    override suspend fun unmute(): Boolean {
        nativeMember.mute(0)
        return true
    }

    override val joinTime: Timestamp get() = Timestamp.NotSupport
    override val nickname: String get() = nativeMember.nameCard
    override val avatar: String get() = nativeMember.avatarUrl
    override val username: String get() = nativeMember.nick
}