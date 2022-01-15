package love.forte.simbot.component.mirai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import love.forte.simbot.*
import love.forte.simbot.definition.Group
import java.util.stream.Stream
import kotlin.time.Duration


public typealias NativeMiraiGroup = net.mamoe.mirai.contact.Group

/**
 *
 * TODO 注释
 * @author ForteScarlet
 */
public interface MiraiGroup : Group, MiraiChatroom {
    override val nativeContact: NativeMiraiGroup

    override val bot: MiraiBot
    override val id: LongID

    @OptIn(Api4J::class)
    override val owner: MiraiMember
    override val ownerId: LongID
    // Impl

    /**
     * Mirai中，一个群内可能出现的权限是固定的。
     *
     * @see MemberRole
     */
    @OptIn(Api4J::class)
    override fun getRoles(groupingId: ID?, limiter: Limiter): Stream<MemberRole> = Stream.of(*MemberRole.values())

    /**
     * Mirai中，一个群内可能出现的权限是固定的。
     *
     * @see MemberRole
     */
    override suspend fun roles(groupingId: ID?, limiter: Limiter): Flow<MemberRole> = MemberRole.values().asFlow()

    override val icon: String get() = nativeContact.avatarUrl
    override val name: String get() = nativeContact.name
    override val createTime: Timestamp get() = Timestamp.NotSupport
    override val currentMember: Int get() = nativeContact.members.size
    override val description: String get() = ""
    override val maximumMember: Int get() = -1


    override suspend fun owner(): MiraiMember = owner

    @OptIn(Api4J::class)
    override fun getMembers(groupingId: ID?, limiter: Limiter): Stream<out MiraiMember>

    @OptIn(Api4J::class)
    override fun getMembers(groupingId: ID?): Stream<out MiraiMember> = getMembers(groupingId, Limiter)

    @OptIn(Api4J::class)
    override fun getMembers(limiter: Limiter): Stream<out MiraiMember> = getMembers(null, limiter)

    @OptIn(Api4J::class)
    override fun getMembers(): Stream<out MiraiMember> = getMembers(null, Limiter)


    override suspend fun members(groupingId: ID?, limiter: Limiter): Flow<MiraiMember>
    override suspend fun mute(duration: Duration): Boolean

    override suspend fun unmute(): Boolean {
        val settings = nativeContact.settings
        val muteAll = settings.isMuteAll
        return if (muteAll) {
            nativeContact.settings.isMuteAll = false
            true
        } else false
    }

    override suspend fun previous(): Group? = null
}

