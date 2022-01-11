package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import love.forte.simbot.*
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.NativeMiraiBot
import love.forte.simbot.component.mirai.message.MiraiSendOnlyImageImpl
import love.forte.simbot.component.mirai.message.asSimbot
import love.forte.simbot.definition.Guild
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.message.Image
import love.forte.simbot.resources.IDResource
import love.forte.simbot.resources.Resource
import love.forte.simbot.resources.StreamableResource
import net.mamoe.mirai.message.data.flash
import org.slf4j.Logger
import java.util.stream.Stream
import net.mamoe.mirai.message.data.Image as miraiImageFunc


/**
 *
 * @author ForteScarlet
 */
internal class MiraiBotImpl(
    override val nativeBot: NativeMiraiBot,
    override val manager: MiraiBotManagerImpl,
    override val eventProcessor: EventProcessor
) : MiraiBot {
    override val logger: Logger = LoggerFactory.getLogger("love.forte.simbot.mirai.bot.${nativeBot.id}")
    override val id: LongID = nativeBot.id.ID
    override val status: UserStatus get() = MiraiBotStatus

    override suspend fun friends(grouping: Grouping, limiter: Limiter): Flow<MiraiFriendImpl> {
        return nativeBot.friends.asFlow().map { MiraiFriendImpl(this, it) }
    }

    @Api4J
    override fun getFriends(grouping: Grouping, limiter: Limiter): Stream<MiraiFriendImpl> {
        return nativeBot.friends.stream().map { MiraiFriendImpl(this, it) }
    }

    @Api4J
    override fun getGroups(grouping: Grouping, limiter: Limiter): Stream<MiraiGroupImpl> {
        return nativeBot.groups.stream().map { MiraiGroupImpl(this, it) }
    }

    override suspend fun groups(grouping: Grouping, limiter: Limiter): Flow<MiraiGroupImpl> {
        return nativeBot.groups.asFlow().map { MiraiGroupImpl(this, it) }
    }

    @Api4J
    override fun getGuilds(grouping: Grouping, limiter: Limiter): Stream<out Guild> = Stream.empty()
    override suspend fun guilds(grouping: Grouping, limiter: Limiter): Flow<Guild> = emptyFlow()


    override suspend fun uploadImage(resource: Resource, flash: Boolean): Image<*> {
        return when (resource) {
            is IDResource -> {
                val image = miraiImageFunc(resource.id.toString())
                if (flash) image.flash().asSimbot() else image.asSimbot()
            }
            is StreamableResource -> MiraiSendOnlyImageImpl(resource, flash)
        }
    }

    // inner class MiraiContactCache {
    //     init {
    //         TODO("Impl")
    //     }
    //     internal val friendCache = ConcurrentHashMap<LongID, SoftReference<NativeMiraiFriend>>()
    //     internal val groupCache = ConcurrentHashMap<LongID, SoftReference<NativeMiraiGroup>>()
    //     internal val memberCache = ConcurrentHashMap<LongID, SoftReference<NativeMiraiMember>>()
    //
    //
    // }
}

private val MiraiBotStatus = UserStatus.builder().bot().fakeUser().build()



