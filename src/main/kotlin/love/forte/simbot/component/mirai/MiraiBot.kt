package love.forte.simbot.component.mirai

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.isActive
import love.forte.simbot.*
import love.forte.simbot.definition.Friend
import love.forte.simbot.definition.Group
import love.forte.simbot.definition.Guild
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.message.Image
import love.forte.simbot.resources.Resource
import java.util.stream.Stream
import kotlin.coroutines.CoroutineContext

public typealias MBot = net.mamoe.mirai.Bot
public typealias SBot = Bot

/**
 *
 * 一个针对于 [Mirai Bot][MBot] 的  [Simbot Bot][SBot] 实现, 允许直接获取源bot。
 *
 * @author ForteScarlet
 */
public interface MiraiBot : SBot {
    override val component: Component get() = MiraiComponent.component
    override val coroutineContext: CoroutineContext
    override val isStarted: Boolean
    override val status: UserStatus


    /**
     * 内部的源bot。
     */
    public val miraiBot: MBot


    /**
     * 当前Bot所属的 [BotManager]。
     */
    override val manager: BotManager<out MiraiBot>


    /**
     * Bot的 [id][net.mamoe.mirai.Bot.id].
     */
    override val id: ID

    /**
     * 当前bot携带的simbot事件调度器。
     */
    override val eventProcessor: EventProcessor

    override suspend fun join()
    override suspend fun start(): Boolean

    override suspend fun friends(grouping: Grouping, limiter: Limiter): Flow<Friend> {
        TODO("Not yet implemented")
    }

    @Api4J
    override fun getFriends(grouping: Grouping, limiter: Limiter): Stream<out Friend> {
        TODO("Not yet implemented")
    }

    @Api4J
    override fun getGroups(grouping: Grouping, limiter: Limiter): Stream<out Group> {
        TODO("Not yet implemented")
    }

    override suspend fun groups(grouping: Grouping, limiter: Limiter): Flow<Group> {
        TODO("Not yet implemented")
    }


    override val isActive: Boolean get() = miraiBot.isActive

    override val avatar: String get() = miraiBot.avatarUrl

    override val isCancelled: Boolean
        get() = miraiBot.coroutineContext[Job]?.isCancelled ?: false

    override val username: String
        get() = miraiBot.nick


    override suspend fun uploadImage(resource: Resource): Image {
        TODO("Not yet implemented")
    }


    @Api4J
    @Deprecated("Always empty.", ReplaceWith("Stream.empty()", "java.util.stream.Stream"))
    override fun getGuilds(grouping: Grouping, limiter: Limiter): Stream<out Guild> = Stream.empty()

    @Deprecated("Always empty.", ReplaceWith("emptyFlow()", "kotlinx.coroutines.flow.emptyFlow"))
    override suspend fun guilds(grouping: Grouping, limiter: Limiter): Flow<Guild> = emptyFlow()

}