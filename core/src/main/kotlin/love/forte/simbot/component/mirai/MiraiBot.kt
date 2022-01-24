package love.forte.simbot.component.mirai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import love.forte.simbot.*
import love.forte.simbot.definition.Guild
import love.forte.simbot.definition.UserInfo
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.message.Image
import love.forte.simbot.resources.Resource
import net.mamoe.mirai.supervisorJob
import org.slf4j.Logger
import java.util.stream.Stream
import kotlin.coroutines.CoroutineContext


/**
 * 原生的MiraiBot [net.mamoe.mirai.Bot] 类型。
 */
public typealias NativeMiraiBot = net.mamoe.mirai.Bot

/**
 *
 * Mirai的Bot [NativeMiraiBot] 在 simbot中的整合类型。
 *
 * @see NativeMiraiBot
 * @see Bot
 * @author ForteScarlet
 */
public interface MiraiBot : Bot, UserInfo {

    /**
     * 得到这个Bot所代表的原生mirai bot。
     *
     * @see NativeMiraiBot
     */
    public val nativeBot: NativeMiraiBot

    /**
     * bot的id。
     *
     * 在mirai中，bot的账号都是 [Long] 类型的。
     */
    override val id: LongID

    /**
     * @see Bot.eventProcessor
     */
    override val eventProcessor: EventProcessor


    /**
     * @see Bot.logger
     */
    override val logger: Logger


    /**
     *  @see Bot.avatar
     */
    override val avatar: String get() = nativeBot.avatarUrl

    override val component: Component get() = ComponentMirai.component

    /** 直接使用 [nativeBot] 的协程作用域。 */
    override val coroutineContext: CoroutineContext get() = nativeBot.coroutineContext

    override val isActive: Boolean get() = nativeBot.supervisorJob.isActive
    override val isCancelled: Boolean get() = nativeBot.supervisorJob.isCancelled
    override val isStarted: Boolean get() = isCancelled || isActive

    override val manager: MiraiBotManager

    override val username: String get() = nativeBot.nick

    //region friend apis
    /**
     * 获取当前bot的好友信息，并可以通过 [limiter] 进行限流。
     *
     * 在mirai中，没有真正的分页API，因此使用 [limiter] 的效果等同于直接操作 flow 流。
     *
     */
    public suspend fun friends(limiter: Limiter = Limiter): Flow<MiraiFriend>


    /**
     * 获取指定的好友。在mirai中，好友的获取不是挂起的，因此可以安全的使用 [getFriend]
     */
    @OptIn(Api4J::class)
    override fun getFriend(id: ID): MiraiFriend?

    /**
     * 获取当前bot所有的好友信息。
     *
     * @see friends
     */
    @OptIn(Api4J::class)
    override fun getFriends(): Stream<out MiraiFriend>


    /**
     * 获取指定的好友。在mirai中，好友的获取不是挂起的，因此可以安全的使用 [getFriend]
     */
    override suspend fun friend(id: ID): MiraiFriend? = getFriend(id)


    /**
     * 获取当前bot的好友信息，并可以通过 [limiter] 进行限流。
     *
     * 在mirai中，没有真正的分页API，因此使用 [limiter] 的效果等同于直接操作 stream 流。
     * @see friends
     */
    @OptIn(Api4J::class)
    override fun getFriends(limiter: Limiter): Stream<out MiraiFriend> = getFriends().withLimiter(limiter)


    @Deprecated("Mirai好友没有分组信息", ReplaceWith("friends(limiter)"))
    override suspend fun friends(grouping: Grouping, limiter: Limiter): Flow<MiraiFriend> = friends(limiter)

    @OptIn(Api4J::class)
    @Deprecated("Mirai好友没有分组信息", ReplaceWith("getFriends(limiter)"))
    override fun getFriends(grouping: Grouping, limiter: Limiter): Stream<out MiraiFriend> = getFriends(limiter)


    //endregion


    //region group apis

    /**
     * 获取当前Bot中的群组列表，并可以通过 [limiter] 进行限流。
     *
     * 在mirai中，没有实际的限流或分页api，因此使用 [limiter] 等同于直接操作 flow 流。
     */
    public suspend fun groups(limiter: Limiter = Limiter): Flow<MiraiGroup>


    /**
     * 获取当前bot所有的群组。
     *
     * @see groups
     */
    @OptIn(Api4J::class)
    override fun getGroups(): Stream<out MiraiGroup>


    /**
     * 获取指定的群.
     * mirai的群组获取没有真正的挂起，因此可以安全的使用 [getGroup].
     */
    @OptIn(Api4J::class)
    override fun getGroup(id: ID): MiraiGroup?

    /**
     * 获取指定的群.
     * mirai的群组获取没有真正的挂起，因此可以安全的使用 [getGroup].
     * @see getGroup
     */
    override suspend fun group(id: ID): MiraiGroup? = getGroup(id)

    /**
     * 获取当前Bot中的群组列表，并可以通过 [limiter] 进行限流。
     *
     * 在mirai中，没有实际的限流或分页api，因此使用 [limiter] 等同于直接操作 stream 流。
     * @see groups
     */
    @OptIn(Api4J::class)
    override fun getGroups(limiter: Limiter): Stream<out MiraiGroup> = getGroups().withLimiter(limiter)


    @Deprecated("Mirai群组没有分组信息", ReplaceWith("getFriends(limiter)"))
    override suspend fun groups(grouping: Grouping, limiter: Limiter): Flow<MiraiGroup> = groups(limiter)

    @OptIn(Api4J::class)
    @Deprecated("Mirai群组没有分组信息", ReplaceWith("getFriends(limiter)"))
    override fun getGroups(grouping: Grouping, limiter: Limiter): Stream<out MiraiGroup> = getGroups(limiter)


    //endregion


    //region guild apis

    @Deprecated("Mirai组件不支持频道相关API", ReplaceWith("emptyFlow()", "kotlinx.coroutines.flow.emptyFlow"))
    override suspend fun guilds(grouping: Grouping, limiter: Limiter): Flow<Guild> = emptyFlow()

    @OptIn(Api4J::class)
    @Deprecated("Mirai组件不支持频道相关API", ReplaceWith("Stream.empty()", "java.util.stream.Stream"))
    override fun getGuilds(grouping: Grouping, limiter: Limiter): Stream<out Guild> = Stream.empty()

    @OptIn(Api4J::class)
    @Deprecated("Mirai组件不支持频道相关API", ReplaceWith("Stream.empty()", "java.util.stream.Stream"))
    override fun getGuilds(): Stream<out Guild> = Stream.empty()

    @OptIn(Api4J::class)
    @Deprecated("Mirai组件不支持频道相关API", ReplaceWith("Stream.empty()", "java.util.stream.Stream"))
    override fun getGuilds(limiter: Limiter): Stream<out Guild> = Stream.empty()

    @Deprecated("Mirai组件不支持频道相关API", ReplaceWith("Stream.empty()", "java.util.stream.Stream"))
    override suspend fun guild(id: ID): Guild? = null

    @OptIn(Api4J::class)
    @Deprecated("Mirai组件不支持频道相关API", ReplaceWith("Stream.empty()", "java.util.stream.Stream"))
    override fun getGuild(id: ID): Guild? = null

    //endregion

    /**
     * 通过 [resource] 上传并得到一个可以且仅可用于在mirai组件中进行 **发送** 的图片消息对象。
     *
     * 当使用 [love.forte.simbot.resources.IDResource] 的时候，会直接通过mirai的函数
     * [net.mamoe.mirai.message.data.Image] 直接通过此ID获取对应图片。
     * 此时的 [Image] 对象是可以序列化的。
     *
     * 如果通过 [love.forte.simbot.resources.StreamableResource] 来构建 [Image],
     * 那么得到的 [Image] 对象只是一个尚未初始化的伪[Image], 他会在发送消息的时候根据对应的 [NativeMiraiContact] 来进行上传并发送。
     *
     *
     */
    override suspend fun uploadImage(resource: Resource): Image<*> {
        return uploadImage(resource, false)
    }

    /**
     * 通过 [resource] 上传并得到一个可以且仅可用于在mirai组件中进行 **发送** 的图片消息对象。
     *
     * 当使用 [love.forte.simbot.resources.IDResource] 的时候，会直接通过mirai的函数
     * [net.mamoe.mirai.message.data.Image] 直接通过此ID获取对应图片。
     * 此时的 [Image] 对象是可以序列化的。
     *
     * 如果通过 [love.forte.simbot.resources.StreamableResource] 来构建 [Image],
     * 那么得到的 [Image] 对象只是一个尚未初始化的伪[Image], 他会在发送消息的时候根据对应的 [NativeMiraiContact] 来进行上传并发送。
     *
     * 可以通过 [flash] 对象来构建一个 *闪照* 图片对象。
     *
     */
    public suspend fun uploadImage(resource: Resource, flash: Boolean): Image<*>

    override suspend fun join() {
        nativeBot.join()
    }

    override suspend fun cancel(reason: Throwable?): Boolean {
        return if (isCancelled) false
        else true.also {
            nativeBot.close(reason)
        }
    }
}