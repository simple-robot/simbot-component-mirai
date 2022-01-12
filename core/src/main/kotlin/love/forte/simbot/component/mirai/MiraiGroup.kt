package love.forte.simbot.component.mirai

import kotlinx.coroutines.flow.Flow
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
public interface MiraiGroup : Group, MiraiOrganization {
    override val nativeContact: NativeMiraiGroup

    override val bot: MiraiBot
    override val id: LongID

    @OptIn(Api4J::class)
    override val owner: MiraiMember
    override val ownerId: LongID
    // Impl

    override val icon: String get() = nativeContact.avatarUrl
    override val name: String get() = nativeContact.name
    override val createTime: Timestamp get() = Timestamp.NotSupport
    override val currentMember: Int get() = nativeContact.members.size
    override val description: String get() = ""
    override val maximumMember: Int get() = -1


    override suspend fun owner(): MiraiMember = owner
    @OptIn(Api4J::class)
    override fun getMembers(groupingId: ID?, limiter: Limiter): Stream<out MiraiMember>
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

