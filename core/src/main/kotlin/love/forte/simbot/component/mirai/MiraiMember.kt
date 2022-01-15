package love.forte.simbot.component.mirai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import love.forte.simbot.Api4J
import love.forte.simbot.LongID
import love.forte.simbot.Timestamp
import love.forte.simbot.definition.GroupMember
import kotlin.time.Duration


/**
 * @see net.mamoe.mirai.contact.Member
 */
public typealias NativeMiraiMember = net.mamoe.mirai.contact.Member

/**
 * 一个由simbot包装为 [GroupMember] 的 [NativeMiraiMember] 对象。
 *
 * @see NativeMiraiMember
 * @author ForteScarlet
 */
public interface MiraiMember : GroupMember, MiraiContact {

    override val nativeContact: NativeMiraiMember

    override val bot: MiraiBot
    override val id: LongID

    @OptIn(Api4J::class)
    override val group: MiraiGroup

    //// Impl

    override suspend fun group(): MiraiGroup = group
    override suspend fun organization(): MiraiGroup = group

    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group


    override suspend fun mute(duration: Duration): Boolean {
        nativeContact.mute(duration.inWholeSeconds.toInt())
        return true
    }

    override suspend fun unmute(): Boolean {
        nativeContact.mute(0)
        return true
    }

    @OptIn(Api4J::class)
    override val roles: List<MemberRole>

    override suspend fun roles(): Flow<MemberRole> = roles.asFlow()


    override val joinTime: Timestamp get() = Timestamp.NotSupport
    override val nickname: String get() = nativeContact.nameCard
    override val avatar: String get() = nativeContact.avatarUrl
    override val username: String get() = nativeContact.nick
}