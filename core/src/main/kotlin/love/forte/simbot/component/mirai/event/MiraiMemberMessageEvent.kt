package love.forte.simbot.component.mirai.event

import love.forte.simbot.Api4J
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.Event
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.event.events.GroupTempMessageEvent


/**
 * @see GroupTempMessageEvent
 */
public typealias NativeMiraiGroupTempMessageEvent = GroupTempMessageEvent


/**
 * mirai群临时会话事件。
 *
 * @author ForteScarlet
 */
public interface MiraiMemberMessageEvent
    : MiraiSimbotContactMessageEvent<GroupTempMessageEvent>,
    ContactMessageEvent {

    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val user: MiraiMember

    override val key: Event.Key<MiraiMemberMessageEvent> get() = Key
    override val metadata: Metadata

    public interface Metadata : MiraiSimbotEvent.Metadata<GroupTempMessageEvent>

    public companion object Key : BaseEventKey<MiraiMemberMessageEvent>(
        "mirai.group_temp_message",
        MiraiSimbotContactMessageEvent, ContactMessageEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberMessageEvent? = doSafeCast(value)
    }

}
