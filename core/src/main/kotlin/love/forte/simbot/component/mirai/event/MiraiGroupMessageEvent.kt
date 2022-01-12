package love.forte.simbot.component.mirai.event

import love.forte.simbot.Api4J
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiGroup
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.Event
import love.forte.simbot.event.FriendMessageEvent
import love.forte.simbot.event.GroupMessageEvent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.message.data.MessageSource.Key.recall


/**
 * @see net.mamoe.mirai.event.events.FriendMessageEvent
 */
public typealias NativeMiraiGroupMessageEvent = net.mamoe.mirai.event.events.GroupMessageEvent

/**
 * 群消息事件。
 *
 * Mirai [NativeMiraiFriendMessageEvent] 事件对应的 [FriendMessageEvent] 事件类型。
 *
 * @see NativeMiraiFriendMessageEvent
 * @author ForteScarlet
 */
public interface MiraiGroupMessageEvent :
    MiraiSimbotChatroomMessageEvent<NativeMiraiGroupMessageEvent>,
    GroupMessageEvent {

    override val bot: MiraiBot
    override val metadata: Metadata
    @OptIn(Api4J::class)
    override val author: MiraiMember
    @OptIn(Api4J::class)
    override val group: MiraiGroup
    override val messageContent: MiraiReceivedMessageContent

    /**
     * 删除，或者说撤回这个消息。
     *
     * 当无权限时会抛出异常。
     *
     * @see net.mamoe.mirai.message.data.MessageSource.recall
     */
    override suspend fun delete(): Boolean

    //// Impl

    override val key: Event.Key<MiraiGroupMessageEvent> get() = Key

    /**
     * 对于群消息，其可见性是 [公开的][Event.VisibleScope.PUBLIC].
     * */
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PUBLIC


    override suspend fun author(): MiraiMember = author
    override suspend fun group(): MiraiGroup = group
    override suspend fun organization(): MiraiGroup = group
    override suspend fun source(): MiraiGroup = group
    @OptIn(Api4J::class)
    override val source: MiraiGroup get() = group

    @OptIn(Api4J::class)
    override val organization: MiraiGroup get() = group


    /**
     * [MiraiGroupMessageEvent] 的元数据类型。
     */
    public interface Metadata : MiraiSimbotEvent.Metadata<NativeMiraiGroupMessageEvent>

    public companion object Key :
        BaseEventKey<MiraiGroupMessageEvent>(
            "mirai.group_message",
            MiraiSimbotChatroomMessageEvent, GroupMessageEvent
        ) {
        override fun safeCast(value: Any): MiraiGroupMessageEvent? = doSafeCast(value)
    }

}