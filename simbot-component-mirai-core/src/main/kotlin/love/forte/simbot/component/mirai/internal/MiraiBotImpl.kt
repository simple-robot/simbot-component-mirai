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

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.*
import love.forte.simbot.*
import love.forte.simbot.LoggerFactory
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.event.impl.*
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.component.mirai.util.*
import love.forte.simbot.definition.*
import love.forte.simbot.event.*
import love.forte.simbot.resources.*
import net.mamoe.mirai.utils.*
import org.slf4j.*
import java.util.stream.*
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


    @OptIn(Api4J::class)
    override fun getFriend(id: ID): MiraiFriend? =
        nativeBot.getFriend(id.tryToLongID().number)?.asSimbot(this)

    @OptIn(Api4J::class)
    override fun getGroup(id: ID): MiraiGroup? =
        nativeBot.getGroup(id.tryToLongID().number)?.asSimbot(this)


    override suspend fun friend(id: ID): MiraiFriend? = getFriend(id)
    override suspend fun group(id: ID): MiraiGroup? = getGroup(id)

    override fun sendOnlyImage(resource: Resource, flash: Boolean): MiraiSendOnlyImage {
        return MiraiSendOnlyImageImpl(resource, flash)
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
            nativeBot.login()
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
        LRUCacheMap<NativeMiraiFriend, MiraiFriendImpl>(nativeBot.friends.size.takeIf { it > 0 }?.let { it / 2 } ?: 16)
    private var groupCache =
        LRUCacheMap<NativeMiraiGroup, MiraiGroupImpl>(nativeBot.groups.size.takeIf { it > 0 }?.let { it / 2 } ?: 16)
    private var memberCache =
        LRUCacheMap<NativeMiraiMember, MiraiMemberImpl>(nativeBot.groups.sumOf { g -> g.members.size }.takeIf { it > 0 }
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
            is NativeMiraiFriendMessageEvent -> {
                // 临时处理，不接受friend为bot自己的消息
                // fix in 2.9.2
                if (this.sender.id != bot.id) {
                    doHandler(this, MiraiFriendMessageEvent) {
                        MiraiFriendMessageEventImpl(this@registerEvents, this)
                    }
                }
            }
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
                    MiraiBotGroupRoleChangeEvent
                ) { MiraiBotGroupRoleChangeEventImpl(this@registerEvents, this) }

            is NativeMiraiBotUnmuteEvent ->
                doHandler(this, MiraiBotUnmuteEvent) {
                    MiraiBotUnmuteEventImpl(
                        this@registerEvents,
                        this
                    )
                }
            //endregion

            //region Group settings event
            // is NativeMiraiGroupSettingChangeEvent<*> -> when (this) {
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
            // }
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
                doHandler(this, MiraiMemberRoleChangeEvent) {
                    MiraiMemberRoleChangeEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberSpecialTitleChangeEvent ->
                doHandler(this, MiraiMemberSpecialTitleChangeEvent) {
                    MiraiMemberSpecialTitleChangeEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberCardChangeEvent ->
                doHandler(this, MiraiMemberCardChangeEvent) {
                    MiraiMemberCardChangeEventImpl(this@registerEvents, this)
                }
            is NativeMiraiMemberJoinRequestEvent -> {
                doHandler(this, MiraiMemberJoinRequestEvent) {
                    MiraiMemberJoinRequestEventImpl(this@registerEvents, this)
                }
            }
            is NativeMiraiMemberLeaveEvent -> {
                doHandler(this, MiraiMemberLeaveEvent) {
                    MiraiMemberLeaveEventImpl(this@registerEvents, this)
                }
            }
            is NativeMiraiMemberJoinEvent -> {
                doHandler(this, MiraiMemberJoinEvent) {
                    MiraiMemberJoinEventImpl(this@registerEvents, this)
                }
            }
            //endregion

            //region message post send
            is NativeMiraiMessagePostSendEvent<*> -> when (this) {
                is NativeMiraiFriendMessagePostSendEvent ->
                    doHandler(this, MiraiFriendMessagePostSendEvent) {
                        MiraiFriendMessagePostSendEventImpl(this@registerEvents, this)
                    }

                is NativeMiraiGroupMessagePostSendEvent -> doHandler(this, MiraiGroupMessagePostSendEvent) {
                    MiraiGroupMessagePostSendEventImpl(this@registerEvents, this)
                }

                is NativeMiraiGroupTempMessagePostSendEvent -> doHandler(this, MiraiGroupTempMessagePostSendEvent) {
                    MiraiGroupTempMessagePostSendEventImpl(this@registerEvents, this)
                }

                is NativeMiraiStrangerMessagePostSendEvent -> TODO()

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
    crossinline handler: E.(bot: MiraiBotImpl) -> SE
) {
    launch {
        val b = this@doHandler
        if (eventProcessor.isProcessable(key)) {
            eventProcessor.push(event.handler(b))
        }
    }
}

