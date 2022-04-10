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

package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.event.impl.*
import love.forte.simbot.component.mirai.message.MiraiImage
import love.forte.simbot.component.mirai.message.MiraiImageImpl
import love.forte.simbot.component.mirai.message.MiraiSendOnlyImage
import love.forte.simbot.component.mirai.util.LRUCacheMap
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.event.Event
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.event.pushIfProcessable
import love.forte.simbot.resources.Resource
import net.mamoe.mirai.utils.MiraiExperimentalApi
import org.slf4j.Logger
import java.util.stream.Stream
import net.mamoe.mirai.message.data.Image as miraiImageFunc


/**
 *
 * @author ForteScarlet
 */
internal class MiraiBotImpl(
    override val originalBot: OriginalMiraiBot,
    override val manager: MiraiBotManagerImpl,
    override val eventProcessor: EventProcessor,
    override val component: Component
) : MiraiBot {
    override val logger: Logger = LoggerFactory.getLogger("love.forte.simbot.mirai.bot.${originalBot.id}")
    override val id: LongID = originalBot.id.ID
    override val status: UserStatus get() = MiraiBotStatus

    override fun isMe(id: ID): Boolean {
        return when (id) {
            is LongID -> id.value == this.id.value
            is NumericalID<*> -> id.toLong() == this.id.value
            else -> id.literal == this.id.literal
        }
    }


    override suspend fun friends(limiter: Limiter): Flow<MiraiFriend> {
        return originalBot.friends.asFlow().map { it.asSimbot(this) }.withLimiter(limiter)
    }

    override fun getFriends(): Stream<out MiraiFriend> {
        return originalBot.friends.stream().map { it.asSimbot(this) }
    }


    override suspend fun groups(limiter: Limiter): Flow<MiraiGroup> {
        return originalBot.groups.asFlow().map { it.asSimbot(this) }.withLimiter(limiter)
    }

    override fun getGroups(): Stream<out MiraiGroup> {
        return originalBot.groups.stream().map { it.asSimbot(this) }.withLimiter(limiter())
    }


    @OptIn(Api4J::class)
    override fun getFriend(id: ID): MiraiFriend? =
        originalBot.getFriend(id.tryToLongID().number)?.asSimbot(this)

    @OptIn(Api4J::class)
    override fun getGroup(id: ID): MiraiGroup? =
        originalBot.getGroup(id.tryToLongID().number)?.asSimbot(this)


    override suspend fun friend(id: ID): MiraiFriend? = getFriend(id)
    override suspend fun group(id: ID): MiraiGroup? = getGroup(id)

    override fun sendOnlyImage(resource: Resource, flash: Boolean): MiraiSendOnlyImage {
        return MiraiSendOnlyImage.of(resource, flash)
    }

    override fun idImage(
        id: ID,
        flash: Boolean,
        builderAction: net.mamoe.mirai.message.data.Image.Builder.() -> Unit
    ): MiraiImage {
        val img = miraiImageFunc(id.literal, builderAction)
        return MiraiImageImpl(img, flash)
    }


    private val loginLock = Mutex()

    @Volatile
    private var eventRegistered = false

    override suspend fun start(): Boolean = loginLock.withLock {
        // 注册事件。
        if (!eventRegistered) {
            registerEvents()
            eventRegistered = true
        }
        try {
            originalBot.login()
        } catch (e: Throwable) {
            // close cause
            e.initCause(null)
            val cause = e.cause
            throw IllegalStateException("Bot login failed. cause: $cause", e)
        }
        eventProcessor.pushIfProcessable(MiraiBotStartedEvent) {
            MiraiBotStartedEventImpl(this)
        }
        true
    }

    override fun toString(): String {
        return "MiraiBot(id=$id, isActive=$isActive, eventProcessor=$eventProcessor, manager=$manager)"
    }


    private var friendCache =
        LRUCacheMap<OriginalMiraiFriend, MiraiFriendImpl>(originalBot.friends.size.takeIf { it > 0 }?.let { it / 2 }
            ?: 16)
    private var groupCache =
        LRUCacheMap<OriginalMiraiGroup, MiraiGroupImpl>(originalBot.groups.size.takeIf { it > 0 }?.let { it / 2 } ?: 16)
    private var memberCache =
        LRUCacheMap<OriginalMiraiMember, MiraiMemberImpl>(originalBot.groups.sumOf { g -> g.members.size }
            .takeIf { it > 0 }
            ?.let { it / 2 } ?: 16)


    @Suppress("UNUSED_PARAMETER")
    internal inline fun <K, V> computeCache(cache: LRUCacheMap<K, V>, key: K, ifMiss: () -> V): V {
        // 暂时不启用缓存
        return ifMiss()

        // return cache[key] ?: synchronized(cache) {
        //     cache[key] ?: run {
        //         val newValue = ifMiss()
        //         cache[key] = newValue
        //         newValue
        //     }
        // }
    }

    // 无效的Map

    internal inline fun computeFriend(friend: OriginalMiraiFriend, ifMiss: () -> MiraiFriendImpl): MiraiFriendImpl {
        return computeCache(friendCache, friend, ifMiss)
    }

    internal inline fun computeGroup(group: OriginalMiraiGroup, ifMiss: () -> MiraiGroupImpl): MiraiGroupImpl {
        return computeCache(groupCache, group, ifMiss)
    }

    internal inline fun computeMember(member: OriginalMiraiMember, ifMiss: () -> MiraiMemberImpl): MiraiMemberImpl {
        return computeCache(memberCache, member, ifMiss)
    }

}

private val MiraiBotStatus = UserStatus.builder().bot().fakeUser().build()


@OptIn(MiraiExperimentalApi::class)
private fun MiraiBotImpl.registerEvents() {

    // 仅作为一个 listener 注册。
    originalBot.eventChannel.subscribeAlways<OriginalMiraiEvent> {
        when (this) {
            //region Message events
            // friend message
            is OriginalMiraiFriendMessageEvent -> {
                // 临时处理，不接受friend为bot自己的消息
                // fix in 2.9.2
                if (this.sender.id != bot.id) {
                    doHandler(this, MiraiFriendMessageEvent) {
                        MiraiFriendMessageEventImpl(this@registerEvents, this)
                    }
                }
            }
            // stranger message
            is OriginalMiraiStrangerMessageEvent ->
                doHandler(this, MiraiStrangerMessageEvent) { MiraiStrangerMessageEventImpl(this@registerEvents, this) }
            // group message
            is OriginalMiraiGroupMessageEvent ->
                doHandler(this, MiraiGroupMessageEvent) { MiraiGroupMessageEventImpl(this@registerEvents, this) }
            // group temp message
            is OriginalMiraiGroupTempMessageEvent ->
                doHandler(this, MiraiMemberMessageEvent) { MiraiMemberMessageEventImpl(this@registerEvents, this) }
            //endregion

            //region Friend events
            is OriginalMiraiFriendRequestEvent ->
                doHandler(this, MiraiFriendRequestEvent) { MiraiFriendRequestEventImpl(this@registerEvents, this) }
            is OriginalMiraiFriendInputStatusChangedEvent ->
                doHandler(
                    this,
                    MiraiFriendInputStatusChangedEvent
                ) { MiraiFriendInputStatusChangedEventImpl(this@registerEvents, this) }
            is OriginalMiraiFriendNickChangedEvent ->
                doHandler(this, MiraiFriendNickChangedEvent) {
                    MiraiFriendNickChangedEventImpl(
                        this@registerEvents,
                        this
                    )
                }
            is OriginalMiraiFriendAvatarChangedEvent ->
                doHandler(this, MiraiFriendAvatarChangedEvent) {
                    MiraiFriendAvatarChangedEventImpl(
                        this@registerEvents,
                        this
                    )
                }
            is OriginalMiraiFriendDecreaseEvent ->
                doHandler(this, MiraiFriendDecreaseEvent) { MiraiFriendDecreaseEventImpl(this@registerEvents, this) }
            is OriginalMiraiFriendIncreaseEvent ->
                doHandler(this, MiraiFriendIncreaseEvent) { MiraiFriendIncreaseEventImpl(this@registerEvents, this) }
            is OriginalMiraiFriendRemarkChangeEvent ->
                doHandler(this, MiraiFriendRemarkChangeEvent) {
                    MiraiFriendRemarkChangeEventImpl(
                        this@registerEvents,
                        this
                    )
                }
            //endregion

            //region Group bot events
            is OriginalMiraiBotInvitedJoinGroupRequestEvent ->
                doHandler(
                    this,
                    MiraiBotInvitedJoinGroupRequestEvent
                ) { MiraiBotInvitedJoinGroupRequestEventImpl(this@registerEvents, this) }

            is OriginalMiraiBotLeaveEvent ->
                doHandler(this, MiraiBotLeaveEvent) { MiraiBotLeaveEventImpl(this@registerEvents, this) }

            is OriginalMiraiBotJoinGroupEvent ->
                doHandler(this, MiraiBotJoinGroupEvent) { MiraiBotJoinGroupEventImpl(this@registerEvents, this) }

            is OriginalMiraiBotMuteEvent ->
                doHandler(this, MiraiBotMuteEvent) { MiraiBotMuteEventImpl(this@registerEvents, this) }

            is OriginalMiraiBotGroupPermissionChangeEvent ->
                doHandler(
                    this,
                    MiraiBotGroupRoleChangeEvent
                ) { MiraiBotGroupRoleChangeEventImpl(this@registerEvents, this) }

            is OriginalMiraiBotUnmuteEvent ->
                doHandler(this, MiraiBotUnmuteEvent) {
                    MiraiBotUnmuteEventImpl(
                        this@registerEvents,
                        this
                    )
                }
            //endregion

            //region Group settings event
            // is NativeMiraiGroupSettingChangeEvent<*> -> when (this) {
            is OriginalMiraiGroupNameChangeEvent ->
                doHandler(this, MiraiGroupNameChangeEvent) {
                    MiraiGroupNameChangeEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiGroupEntranceAnnouncementChangeEvent ->
                doHandler(this, MiraiGroupEntranceAnnouncementChangeEvent) {
                    MiraiGroupEntranceAnnouncementChangeEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiGroupMuteAllEvent ->
                doHandler(this, MiraiGroupMuteAllEvent) {
                    MiraiGroupMuteAllEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiGroupAllowAnonymousChatEvent ->
                doHandler(this, MiraiGroupAllowAnonymousChatEvent) {
                    MiraiGroupAllowAnonymousChatEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiGroupAllowConfessTalkEvent ->
                doHandler(this, MiraiGroupAllowConfessTalkEvent) {
                    MiraiGroupAllowConfessTalkEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiGroupAllowMemberInviteEvent ->
                doHandler(this, MiraiGroupAllowMemberInviteEvent) {
                    MiraiGroupAllowMemberInviteEventImpl(this@registerEvents, this)
                }
            // }
            //endregion

            //region Group member events
            is OriginalMiraiGroupTalkativeChangeEvent ->
                doHandler(this, MiraiGroupTalkativeChangeEvent) {
                    MiraiGroupTalkativeChangeEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiMemberHonorChangeEvent ->
                doHandler(this, MiraiMemberHonorChangeEvent) {
                    MiraiMemberHonorChangeEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiMemberUnmuteEvent ->
                doHandler(this, MiraiMemberUnmuteEvent) {
                    MiraiMemberUnmuteEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiMemberMuteEvent ->
                doHandler(this, MiraiMemberMuteEvent) {
                    MiraiMemberMuteEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiMemberPermissionChangeEvent ->
                doHandler(this, MiraiMemberRoleChangeEvent) {
                    MiraiMemberRoleChangeEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiMemberSpecialTitleChangeEvent ->
                doHandler(this, MiraiMemberSpecialTitleChangeEvent) {
                    MiraiMemberSpecialTitleChangeEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiMemberCardChangeEvent ->
                doHandler(this, MiraiMemberCardChangeEvent) {
                    MiraiMemberCardChangeEventImpl(this@registerEvents, this)
                }
            is OriginalMiraiMemberJoinRequestEvent -> {
                doHandler(this, MiraiMemberJoinRequestEvent) {
                    MiraiMemberJoinRequestEventImpl(this@registerEvents, this)
                }
            }
            is OriginalMiraiMemberLeaveEvent -> {
                doHandler(this, MiraiMemberLeaveEvent) {
                    MiraiMemberLeaveEventImpl(this@registerEvents, this)
                }
            }
            is OriginalMiraiMemberJoinEvent -> {
                doHandler(this, MiraiMemberJoinEvent) {
                    MiraiMemberJoinEventImpl(this@registerEvents, this)
                }
            }
            //endregion

            //region message post send
            is OriginalMiraiMessagePostSendEvent<*> -> when (this) {
                is OriginalMiraiFriendMessagePostSendEvent ->
                    doHandler(this, MiraiFriendMessagePostSendEvent) {
                        MiraiFriendMessagePostSendEventImpl(this@registerEvents, this)
                    }

                is OriginalMiraiGroupMessagePostSendEvent ->
                    doHandler(this, MiraiGroupMessagePostSendEvent) {
                        MiraiGroupMessagePostSendEventImpl(this@registerEvents, this)
                    }

                is OriginalMiraiGroupTempMessagePostSendEvent ->
                    doHandler(this, MiraiGroupTempMessagePostSendEvent) {
                        MiraiGroupTempMessagePostSendEventImpl(this@registerEvents, this)
                    }

                is OriginalMiraiStrangerMessagePostSendEvent ->
                    doHandler(this, MiraiStrangerMessagePostSendEvent) {
                        MiraiStrangerMessagePostSendEventImpl(this@registerEvents, this)
                    }


            }
            //endregion


            else -> {
                @OptIn(DiscreetSimbotApi::class)
                eventProcessor.pushIfProcessable(UnsupportedMiraiEvent) {
                    UnsupportedMiraiEvent(this@registerEvents, this)
                }
            }
        }

        // this.intercept()
    }

    // listener

}

private suspend inline fun <reified E : OriginalMiraiEvent, reified SE : MiraiSimbotEvent<E>>
        MiraiBotImpl.doHandler(
    event: E,
    key: Event.Key<SE>,
    crossinline handler: E.(bot: MiraiBotImpl) -> SE
) {
    launch {
        val b = this@doHandler
        if (eventProcessor.isProcessable(key)) {
            eventProcessor.push(event.handler(b))
        }
    }
}

