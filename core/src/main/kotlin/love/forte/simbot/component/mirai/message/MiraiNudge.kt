package love.forte.simbot.component.mirai.message

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.Component
import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.InternalApi
import love.forte.simbot.event.EventProcessingContext
import love.forte.simbot.message.Message
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.getMemberOrFail
import net.mamoe.mirai.event.events.StrangerEvent
import net.mamoe.mirai.message.action.Nudge.Companion.sendNudge
import kotlin.reflect.KClass

/**
 * 仅用于发送的 nudge 对象, 不会在接收中出现。
 *
 * 如果发送目标不是群聊，那么[target]除非等于bot自己的id，否则将无效。如果发送目标是群聊，
 * 那么假如[target]不存在，则会尝试获取当前是否存在环境事件。如果处于事件当中, 则会戳对应的当前事件中的人，
 * 否则将会戳bot自己。
 *
 * Nudge 会瞬间发送，不会计入消息列表中。
 *
 * @property target 发送目标
 */
@SerialName("mirai.nudge")
@Serializable
public data class MiraiNudge constructor(
    public val target: LongID? = null,
) : MiraiSendOnlyComputableSimbotMessage<MiraiNudge> {
    override val key: Message.Key<MiraiNudge> get() = Key

    @OptIn(InternalApi::class)
    override suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage {
        if (target != null) {
            if (target.number == contact.bot.id) {
                contact.sendNudge(contact.bot.nudge())
                return EmptySingleMessage
            }

            when (contact) {
                is NativeMiraiGroup -> {
                    val nudge = contact.getMemberOrFail(target.number).nudge()
                    contact.sendNudge(nudge)
                }
                is NativeMiraiFriend -> contact.sendNudge(contact.nudge())
                is NativeMiraiMember -> contact.sendNudge(contact.nudge())
                is NativeMiraiStranger -> contact.sendNudge(contact.nudge())
            }
        } else {
            // 没有target
            val event = currentCoroutineContext()[EventProcessingContext]?.event
            if (event is MiraiSimbotEvent<*>) {
                when (val nativeEvent = event.metadata.nativeEvent) {
                    is NativeMiraiGroupMemberEvent -> nativeEvent.member.sendNudge(nativeEvent.member.nudge())
                    is NativeMiraiGroupMessageEvent -> nativeEvent.group.sendNudge(nativeEvent.sender.nudge())
                    is NativeMiraiGroupEvent -> nativeEvent.group.sendNudge(nativeEvent.group.bot.nudge())
                    is NativeMiraiFriendEvent -> nativeEvent.friend.sendNudge(nativeEvent.friend.nudge())
                    is StrangerEvent -> nativeEvent.stranger.sendNudge(nativeEvent.stranger.nudge())
                }
            } else {
                // 发送nudge自己
                contact.sendNudge(contact.bot.nudge())
            }
        }
        return EmptySingleMessage
    }

    override fun toString(): String = "MiraiNudge(target=$target)"

    public companion object Key : Message.Key<MiraiNudge> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<MiraiNudge>
            get() = MiraiNudge::class
    }
}


@SerialName("mirai.nudgeMessage")
@Serializable
public class MiraiNudgeMessage {
    // TODO

}