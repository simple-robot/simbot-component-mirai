package love.forte.simbot.component.mirai.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.event.impl.*
import love.forte.simbot.component.mirai.message.MiraiSendOnlyImageImpl
import love.forte.simbot.component.mirai.message.asSimbot
import love.forte.simbot.component.mirai.util.LRUCacheMap
import love.forte.simbot.definition.UserStatus
import love.forte.simbot.event.Event
import love.forte.simbot.event.EventProcessingResult
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.event.pushIfProcessable
import love.forte.simbot.message.Image
import love.forte.simbot.resources.IDResource
import love.forte.simbot.resources.Resource
import love.forte.simbot.resources.StreamableResource
import net.mamoe.mirai.message.data.flash
import net.mamoe.mirai.utils.MiraiExperimentalApi
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

    override suspend fun friends(limiter: Limiter): Flow<MiraiFriend> {
        return nativeBot.friends.asFlow().map { it.asSimbot(this) }.withLimiter(limiter)
    }

    override fun getFriends(): Stream<out MiraiFriend> {
        return nativeBot.friends.stream().map { it.asSimbot(this) }
    }


    override suspend fun groups(limiter: Limiter): Flow<MiraiGroup> {
        return nativeBot.groups.asFlow().map { it.asSimbot(this) }.withLimiter(limiter)
    }

    override fun getGroups(): Stream<out MiraiGroup> {
        return nativeBot.groups.stream().map { it.asSimbot(this) }.withLimiter(limiter())
    }

    override suspend fun uploadImage(resource: Resource, flash: Boolean): Image<*> {
        return when (resource) {
            is IDResource -> {
                val image = miraiImageFunc(resource.id.toString())
                if (flash) image.flash().asSimbot() else image.asSimbot()
            }
            is StreamableResource -> MiraiSendOnlyImageImpl(resource, flash)
        }
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
        nativeBot.login()
        true
    }

    override fun toString(): String {
        return "MiraiBot(id=$id, isActive=$isActive, eventProcessor=$eventProcessor, manager=$manager)"
    }


    internal var friendCache =
        LRUCacheMap<NativeMiraiFriend, MiraiFriendImpl>(nativeBot.friends.size.takeIf { it > 0 }?.let { it / 2 } ?: 16)
        private set
    internal var groupCache =
        LRUCacheMap<NativeMiraiGroup, MiraiGroupImpl>(nativeBot.groups.size.takeIf { it > 0 }?.let { it / 2 } ?: 16)
        private set
    internal var memberCache =
        LRUCacheMap<NativeMiraiMember, MiraiMemberImpl>(nativeBot.groups.sumOf { g -> g.members.size }.takeIf { it > 0 }
            ?.let { it / 2 } ?: 16)
        private set


    internal inline fun <K, V> computeCache(cache: LRUCacheMap<K, V>, key: K, ifMiss: () -> V): V {
        return cache[key] ?: synchronized(cache) {
            cache[key] ?: run {
                val newValue = ifMiss()
                cache[key] = newValue
                newValue
            }
        }
    }

    // 无效的Map

    internal inline fun computeFriend(friend: NativeMiraiFriend, ifMiss: () -> MiraiFriendImpl): MiraiFriendImpl {
        return computeCache(friendCache, friend, ifMiss)
    }

    internal inline fun computeGroup(group: NativeMiraiGroup, ifMiss: () -> MiraiGroupImpl): MiraiGroupImpl {
        return computeCache(groupCache, group, ifMiss)
    }

    internal inline fun computeMember(member: NativeMiraiMember, ifMiss: () -> MiraiMemberImpl): MiraiMemberImpl {
        return computeCache(memberCache, member, ifMiss)
    }

}

private val MiraiBotStatus = UserStatus.builder().bot().fakeUser().build()


@OptIn(MiraiExperimentalApi::class)
private fun MiraiBotImpl.registerEvents() {

    // 仅作为一个 listener 注册。
    nativeBot.eventChannel.subscribeAlways<NativeMiraiEvent> {
        when (this) {
            //region Message events
            // friend message
            is NativeMiraiFriendMessageEvent ->
                doHandler(this, MiraiFriendMessageEvent) { MiraiFriendMessageEventImpl(this@registerEvents, this) }
            // stranger message
            is NativeMiraiStrangerMessageEvent ->
                doHandler(this, MiraiStrangerMessageEvent) { MiraiStrangerMessageEventImpl(this@registerEvents, this) }
            // group message
            is NativeMiraiGroupMessageEvent ->
                doHandler(this, MiraiGroupMessageEvent) { MiraiGroupMessageEventImpl(this@registerEvents, this) }
            // group temp message
            is NativeMiraiGroupTempMessageEvent ->
                doHandler(this, MiraiMemberMessageEvent) { MiraiMemberMessageEventImpl(this@registerEvents, this) }
            //endregion

            //region Friend events
            is NativeMiraiFriendRequestEvent ->
                doHandler(this, MiraiFriendRequestEvent) { MiraiFriendRequestEventImpl(this@registerEvents, this) }
            is NativeMiraiFriendInputStatusChangedEvent ->
                doHandler(
                    this,
                    MiraiFriendInputStatusChangedEvent
                ) { MiraiFriendInputStatusChangedEventImpl(this@registerEvents, this) }
            is NativeMiraiFriendNickChangedEvent ->
                doHandler(this, MiraiFriendNickChangedEvent) {
                    MiraiFriendNickChangedEventImpl(
                        this@registerEvents,
                        this
                    )
                }
            is NativeMiraiFriendAvatarChangedEvent ->
                doHandler(this, MiraiFriendAvatarChangedEvent) {
                    MiraiFriendAvatarChangedEventImpl(
                        this@registerEvents,
                        this
                    )
                }
            is NativeMiraiFriendDecreaseEvent ->
                doHandler(this, MiraiFriendDecreaseEvent) { MiraiFriendDecreaseEventImpl(this@registerEvents, this) }
            is NativeMiraiFriendIncreaseEvent ->
                doHandler(this, MiraiFriendIncreaseEvent) { MiraiFriendIncreaseEventImpl(this@registerEvents, this) }
            is NativeMiraiFriendRemarkChangeEvent ->
                doHandler(this, MiraiFriendRemarkChangeEvent) {
                    MiraiFriendRemarkChangeEventImpl(
                        this@registerEvents,
                        this
                    )
                }
            //endregion

            //region Group bot events
            is NativeMiraiBotInvitedJoinGroupRequestEvent ->
                doHandler(
                    this,
                    MiraiBotInvitedJoinGroupRequestEvent
                ) { MiraiBotInvitedJoinGroupRequestEventImpl(this@registerEvents, this) }

            is NativeMiraiBotLeaveEvent ->
                doHandler(this, MiraiBotLeaveEvent) { MiraiBotLeaveEventImpl(this@registerEvents, this) }

            is NativeMiraiBotJoinGroupEvent ->
                doHandler(this, MiraiBotJoinGroupEvent) { MiraiBotJoinGroupEventImpl(this@registerEvents, this) }

            is NativeMiraiBotMuteEvent ->
                doHandler(this, MiraiBotMuteEvent) { MiraiBotMuteEventImpl(this@registerEvents, this) }

            is NativeMiraiBotGroupPermissionChangeEvent ->
                doHandler(
                    this,
                    MiraiBotGroupPermissionChangeEvent
                ) { MiraiBotGroupPermissionChangeEventImpl(this@registerEvents, this) }

            is NativeMiraiBotUnmuteEvent ->
                doHandler(this, MiraiBotUnmuteEvent) {
                    MiraiBotUnmuteEventImpl(
                        this@registerEvents,
                        this
                    )
                }
            //endregion

            //region Group settings event
            is NativeMiraiGroupSettingChangeEvent<*> -> when (this) {
                is NativeMiraiGroupNameChangeEvent ->
                    doHandler(this, MiraiGroupNameChangeEvent) {
                        MiraiGroupNameChangeEventImpl(this@registerEvents, this)
                    }
                is NativeMiraiGroupEntranceAnnouncementChangeEvent ->
                    doHandler(this, MiraiGroupEntranceAnnouncementChangeEvent) {
                        MiraiGroupEntranceAnnouncementChangeEventImpl(this@registerEvents, this)
                    }
                is NativeMiraiGroupMuteAllEvent ->
                    doHandler(this, MiraiGroupMuteAllEvent) {
                        MiraiGroupMuteAllEventImpl(this@registerEvents, this)
                    }
                is NativeMiraiGroupAllowAnonymousChatEvent ->
                    doHandler(this, MiraiGroupAllowAnonymousChatEvent) {
                        MiraiGroupAllowAnonymousChatEventImpl(this@registerEvents, this)
                    }
                is NativeMiraiGroupAllowConfessTalkEvent ->
                    doHandler(this, MiraiGroupAllowConfessTalkEvent) {
                        MiraiGroupAllowConfessTalkEventImpl(this@registerEvents, this)
                    }
                is NativeMiraiGroupAllowMemberInviteEvent ->
                    doHandler(this, MiraiGroupAllowMemberInviteEvent) {
                        MiraiGroupAllowMemberInviteEventImpl(this@registerEvents, this)
                    }
            }
            //endregion

            //region Group member events
            is NativeMiraiGroupTalkativeChangeEvent ->
                doHandler(this, MiraiGroupTalkativeChangeEvent) {
                    MiraiGroupTalkativeChangeEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberHonorChangeEvent ->
                doHandler(this, MiraiMemberHonorChangeEvent) {
                    MiraiMemberHonorChangeEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberUnmuteEvent ->
                doHandler(this, MiraiMemberUnmuteEvent) {
                    MiraiMemberUnmuteEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberMuteEvent ->
                doHandler(this, MiraiMemberMuteEvent) {
                    MiraiMemberMuteEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberPermissionChangeEvent ->
                doHandler(this, MiraiMemberPermissionChangeEvent) {
                    MiraiMemberPermissionChangeEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberSpecialTitleChangeEvent ->
                doHandler(this, MiraiMemberSpecialTitleChangeEvent) {
                    MiraiMemberSpecialTitleChangeEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberCardChangeEvent ->
                doHandler(this, MiraiMemberCardChangeEvent) {
                    MiraiMemberCardChangeEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberJoinRequestEvent ->
                doHandler(this, MiraiMemberJoinRequestEvent) {
                    MiraiMemberJoinRequestEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberLeaveEvent ->
                doHandler(this, MiraiMemberLeaveEvent) {
                    MiraiMemberLeaveEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberJoinEvent ->
                doHandler(this, MiraiMemberJoinEvent) {
                    MiraiMemberJoinEventImpl(this@registerEvents, this)
                }
            //endregion


            else -> {
                @OptIn(SimbotDiscreetApi::class)
                eventProcessor.pushIfProcessable(UnsupportedMiraiEvent) {
                    UnsupportedMiraiEvent(this@registerEvents, this)
                }
            }
        }

        // this.intercept()
    }

    // listener

}

private suspend inline fun <reified E : NativeMiraiEvent, reified SE : MiraiSimbotEvent<E>>
        MiraiBotImpl.doHandler(
    event: E,
    key: Event.Key<SE>,
    noinline handler: suspend E.(bot: MiraiBotImpl) -> SE
): EventProcessingResult? {
    return eventProcessor.pushIfProcessable(key) {
        event.handler(this)
    }
}
