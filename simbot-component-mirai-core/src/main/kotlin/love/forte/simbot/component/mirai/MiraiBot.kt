/*
 *  Copyright (c) 2022-2022 ForteScarlet <ForteScarlet@163.com>
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
 *
 */

package love.forte.simbot.component.mirai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import love.forte.simbot.*
import love.forte.simbot.Bot
import love.forte.simbot.component.mirai.message.MiraiImage
import love.forte.simbot.component.mirai.message.MiraiSendOnlyImage
import love.forte.simbot.definition.GroupMemberBot
import love.forte.simbot.definition.Guild
import love.forte.simbot.definition.UserInfo
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.message.Image
import love.forte.simbot.resources.Resource
import net.mamoe.mirai.supervisorJob
import org.slf4j.Logger
import java.util.stream.Stream
import kotlin.coroutines.CoroutineContext
import net.mamoe.mirai.Bot as OriginalMiraiBot


/**
 *
 * Mirai的Bot [OriginalMiraiBot] 在 simbot中的整合类型。
 *
 * 当 [MiraiBot] 被关闭的时候（或者说 [originalBot] 被关闭的时候）会将自身移出所属的 [BotManager][manager].
 * 这一行为是由 [MiraiBotManager] 所决定的。
 *
 * @see OriginalMiraiBot
 * @see Bot
 * @author ForteScarlet
 */
public interface MiraiBot : Bot, UserInfo {
    
    /**
     * 得到自己。
     */
    override val bot: MiraiBot
        get() = this
    
    /**
     * 得到这个Bot所代表的原生mirai bot。
     *
     * @see OriginalMiraiBot
     */
    public val originalBot: OriginalMiraiBot
    
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
    override val avatar: String get() = originalBot.avatarUrl
    
    /** 直接使用 [originalBot] 的协程作用域。 */
    override val coroutineContext: CoroutineContext get() = originalBot.coroutineContext
    
    override val isActive: Boolean get() = originalBot.supervisorJob.isActive
    override val isCancelled: Boolean get() = originalBot.supervisorJob.isCancelled
    override val isStarted: Boolean get() = isCancelled || isActive
    
    override val manager: MiraiBotManager
    
    /**
     * 得到用户名。
     *
     * @see OriginalMiraiBot.nick
     */
    override val username: String get() = originalBot.nick
    
    // region friend apis
    /**
     * 获取当前bot的好友信息，并可以通过 [limiter] 进行限流。
     *
     * 在mirai中，没有真正的分页API，因此使用 [limiter] 的效果等同于直接操作 flow 流。
     *
     */
    @JvmSynthetic
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
    @JvmSynthetic
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
    @JvmSynthetic
    override suspend fun friends(grouping: Grouping, limiter: Limiter): Flow<MiraiFriend> = friends(limiter)
    
    @OptIn(Api4J::class)
    @Deprecated("Mirai好友没有分组信息", ReplaceWith("getFriends(limiter)"))
    override fun getFriends(grouping: Grouping, limiter: Limiter): Stream<out MiraiFriend> = getFriends(limiter)
    
    
    // endregion
    
    
    // region group apis
    
    /**
     * 获取当前Bot中的群组列表，并可以通过 [limiter] 进行限流。
     *
     * 在mirai中，没有实际的限流或分页api，因此使用 [limiter] 等同于直接操作 flow 流。
     */
    @JvmSynthetic
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
    @JvmSynthetic
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
    @JvmSynthetic
    override suspend fun groups(grouping: Grouping, limiter: Limiter): Flow<MiraiGroup> = groups(limiter)
    
    @OptIn(Api4J::class)
    @Deprecated("Mirai群组没有分组信息", ReplaceWith("getFriends(limiter)"))
    override fun getGroups(grouping: Grouping, limiter: Limiter): Stream<out MiraiGroup> = getGroups(limiter)
    
    
    // endregion
    
    
    // region guild apis
    
    @Deprecated("Mirai组件不支持频道相关API", ReplaceWith("emptyFlow()", "kotlinx.coroutines.flow.emptyFlow"))
    @JvmSynthetic
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
    @JvmSynthetic
    override suspend fun guild(id: ID): Guild? = null
    
    @OptIn(Api4J::class)
    @Deprecated("Mirai组件不支持频道相关API", ReplaceWith("Stream.empty()", "java.util.stream.Stream"))
    override fun getGuild(id: ID): Guild? = null
    
    // endregion
    
    // region image api
    /**
     * 通过 [resource] 构建得到一个可以且仅可用于在mirai组件中进行 **发送** 的图片消息对象。
     *
     * 如果通过 [love.forte.simbot.resources.Resource] 来构建 [Image],
     * 那么得到的 [Image] 对象只是一个尚未初始化的伪[Image], 他会在发送消息的时候根据对应的 [net.mamoe.mirai.contact.Contact] 来进行上传并发送。
     */
    public fun sendOnlyImage(resource: Resource, flash: Boolean): MiraiSendOnlyImage
    
    
    /**
     * @see sendOnlyImage
     */
    @JvmSynthetic
    override suspend fun uploadImage(resource: Resource): MiraiSendOnlyImage = sendOnlyImage(resource, false)
    
    /**
     * @see sendOnlyImage
     */
    @JvmSynthetic
    public suspend fun uploadImage(resource: Resource, flash: Boolean): MiraiSendOnlyImage =
        sendOnlyImage(resource, flash)
    
    /**
     * @see sendOnlyImage
     */
    @OptIn(Api4J::class)
    override fun uploadImageBlocking(resource: Resource): MiraiSendOnlyImage = sendOnlyImage(resource, false)
    
    /**
     * @see sendOnlyImage
     */
    public fun uploadImageBlocking(resource: Resource, flash: Boolean): MiraiSendOnlyImage =
        sendOnlyImage(resource, flash)
    
    
    //// id image
    
    /**
     * 尝试通过一个 [ID] 解析得到一个图片对象。
     * 当使用 [ID]的时候， 会直接通过mirai的函数
     * [net.mamoe.mirai.message.data.Image] 直接通过此ID获取对应图片。
     * 此时的 [Image] 对象是可以序列化的。
     */
    public fun idImage(
        id: ID,
        flash: Boolean,
        builderAction: net.mamoe.mirai.message.data.Image.Builder.() -> Unit = {},
    ): MiraiImage
    
    
    /**
     * @see idImage
     */
    @JvmSynthetic
    override suspend fun resolveImage(id: ID): MiraiImage = idImage(id, false)
    
    /**
     * @see idImage
     */
    public fun resolveImage(
        id: ID,
        flash: Boolean,
        builderAction: net.mamoe.mirai.message.data.Image.Builder.() -> Unit = {},
    ): MiraiImage = idImage(id, flash, builderAction)
    
    /**
     * @see idImage
     */
    @OptIn(Api4J::class)
    override fun resolveImageBlocking(id: ID): MiraiImage = idImage(id, false)
    
    
    /**
     * @see idImage
     */
    @Api4J
    public fun resolveImageBlocking(
        id: ID,
        flash: Boolean,
        builderAction: net.mamoe.mirai.message.data.Image.Builder.() -> Unit,
    ): MiraiImage = idImage(id, flash, builderAction)
    // endregion
    
    
    @JvmSynthetic
    override suspend fun join() {
        originalBot.join()
    }
    
    @JvmSynthetic
    override suspend fun cancel(reason: Throwable?): Boolean {
        return if (isCancelled) false
        else true.also {
            originalBot.close(reason)
        }
    }
}


/**
 * mirai组件中针对于 [GroupMemberBot] 的实现。
 *
 * @see GroupMemberBot
 *
 */
@Suppress("UnnecessaryOptInAnnotation")
public interface MiraiGroupMemberBot : MiraiBot, GroupMemberBot {
    
    /**
     * 得到自己。
     */
    override val bot: MiraiBot
    
    
    /**
     * 得到用户头像。
     *  @see MiraiBot.avatar
     */
    override val avatar: String
        get() = originalBot.avatarUrl
    
    
    /**
     * 得到用户名。
     * @see MiraiBot.username
     */
    override val username: String
        get() = super.username
}

