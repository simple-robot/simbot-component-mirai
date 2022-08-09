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

package love.forte.simbot.component.mirai.message

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.ID
import love.forte.simbot.InternalSimbotApi
import love.forte.simbot.LongID
import love.forte.simbot.component.mirai.event.MiraiSimbotEvent
import love.forte.simbot.component.mirai.internal.InternalApi
import love.forte.simbot.component.mirai.message.MiraiReceivedNudge.SubjectType
import love.forte.simbot.event.EventProcessingContext
import love.forte.simbot.literal
import love.forte.simbot.message.Message
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.getMemberOrFail
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.action.Nudge.Companion.sendNudge
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup
import net.mamoe.mirai.contact.Member as OriginalMiraiMember
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger
import net.mamoe.mirai.event.events.FriendEvent as OriginalMiraiFriendEvent
import net.mamoe.mirai.event.events.GroupEvent as OriginalMiraiGroupEvent
import net.mamoe.mirai.event.events.GroupMemberEvent as OriginalMiraiGroupMemberEvent
import net.mamoe.mirai.event.events.GroupMessageEvent as OriginalMiraiGroupMessageEvent
import net.mamoe.mirai.event.events.StrangerEvent as OriginalMiraiStrangerEvent
import net.mamoe.mirai.message.data.Message as OriginalMiraiMessage


private suspend fun sendNudge(contact: Contact, target: Long?) {
    if (target != null) {
        if (target == contact.bot.id) {
            contact.sendNudge(contact.bot.nudge())
            return
        }
        
        when (contact) {
            is OriginalMiraiGroup -> {
                val nudge = contact.getMemberOrFail(target).nudge()
                contact.sendNudge(nudge)
            }
            
            is OriginalMiraiFriend -> contact.sendNudge(contact.nudge())
            is OriginalMiraiMember -> contact.sendNudge(contact.nudge())
            is OriginalMiraiStranger -> contact.sendNudge(contact.nudge())
        }
    } else {
        // 没有target
        val event = currentCoroutineContext()[EventProcessingContext]?.event
        if (event is MiraiSimbotEvent<*>) {
            when (val nativeEvent = event.originalEvent) {
                is OriginalMiraiGroupMemberEvent -> nativeEvent.member.sendNudge(nativeEvent.member.nudge())
                is OriginalMiraiGroupMessageEvent -> nativeEvent.group.sendNudge(nativeEvent.sender.nudge())
                is OriginalMiraiGroupEvent -> nativeEvent.group.sendNudge(nativeEvent.group.bot.nudge())
                is OriginalMiraiFriendEvent -> nativeEvent.friend.sendNudge(nativeEvent.friend.nudge())
                is OriginalMiraiStrangerEvent -> nativeEvent.stranger.sendNudge(nativeEvent.stranger.nudge())
            }
        } else {
            // 如果 contact不是群聊，戳此目标，否则戳bot自己。
            when (contact) {
                is OriginalMiraiFriend -> contact.sendNudge(contact.nudge())
                is OriginalMiraiMember -> contact.sendNudge(contact.nudge())
                is OriginalMiraiStranger -> contact.sendNudge(contact.nudge())
                // 发送nudge自己
                else -> contact.sendNudge(contact.bot.nudge())
            }
            
        }
    }
}


/**
 * 仅用于发送的 nudge 对象, 不会在接收中出现。
 *
 * 对于在消息中会接收到的 nudge, 参考 [MiraiReceivedNudge]。
 *
 * 如果发送目标不是群聊，那么[target]除非等于bot自己的id，否则将无效。如果发送目标是群聊，
 * 那么假如[target]不存在，则会尝试获取当前是否存在环境事件。如果处于事件当中, 则会戳对应的当前事件中的人，
 * 否则将会戳bot自己。
 *
 * Nudge 会瞬间发送，不会计入等待发送的消息列表中。
 *
 * @property target 发送目标
 */
@SerialName("mirai.nudge")
@Serializable
public data class MiraiNudge @JvmOverloads constructor(
    public val target: ID? = null,
) : MiraiSendOnlyComputableMessage<MiraiNudge> {
    override val key: Message.Key<MiraiNudge> get() = Key
    
    @OptIn(InternalApi::class)
    @JvmSynthetic
    override suspend fun originalMiraiMessage(contact: Contact, isDropAction: Boolean): OriginalMiraiMessage {
        if (!isDropAction) {
            sendNudge(contact, target?.literal?.toLong())
        }
        return EmptySingleMessage
    }
    
    override fun toString(): String = "MiraiNudge(target=$target)"
    
    public companion object Key : Message.Key<MiraiNudge> {
        override fun safeCast(value: Any): MiraiNudge? = doSafeCast(value)
    }
}


/**
 * Mirai事件中所接收到的戳一戳事件的信息。
 *
 * 与 [MiraiNudge] 类似，此消息会立即发送，不会计入等待发送的消息列表中。
 *
 * @see net.mamoe.mirai.event.events.NudgeEvent
 */
@SerialName("mirai.receivedNudge")
@Serializable
public data class MiraiReceivedNudge @InternalSimbotApi constructor(
    /**
     * 戳一戳发起人ID。
     *
     * 原类型为 [net.mamoe.mirai.contact.UserOrBot].
     */
    public val from: LongID, // UserOrBot
    
    /**
     * 戳一戳目标, 可能与 [from] 相同.
     *
     * 原类型为 [net.mamoe.mirai.contact.UserOrBot].
     */
    public val target: LongID, // UserOrBot
    
    /**
     * 主体类型。
     * @see SubjectType
     */
    public val subjectType: SubjectType,
    /**
     * 消息语境ID。
     * 原类型为 [net.mamoe.mirai.contact.Contact].
     */
    public val subject: LongID,
    
    public val action: String,
    public val suffix: String,
) : OriginalMiraiComputableSimbotMessage<MiraiReceivedNudge> {
    
    /**
     * [MiraiReceivedNudge] 中 [subjectType] 所使用的可能值。
     */
    @Serializable
    public enum class SubjectType {
        GROUP, STRANGER, FRIEND, MEMBER
    }
    
    /**
     * 立即发送戳一戳消息，并返回一个 [EmptySingleMessage].
     */
    @OptIn(InternalApi::class)
    @JvmSynthetic
    override suspend fun originalMiraiMessage(contact: Contact, isDropAction: Boolean): OriginalMiraiMessage {
        if (!isDropAction) {
            sendNudge(contact, target.number)
        }
        return EmptySingleMessage
    }
    
    override val key: Message.Key<MiraiReceivedNudge>
        get() = Key
    
    public companion object Key : Message.Key<MiraiReceivedNudge> {
        override fun safeCast(value: Any): MiraiReceivedNudge? = doSafeCast(value)
    }
}


/**
 * 将mirai的 [NudgeEvent] 转化为 [MiraiReceivedNudge] 消息对象。
 */
@OptIn(InternalSimbotApi::class)
public fun NudgeEvent.toMessage(): MiraiReceivedNudge {
    return MiraiReceivedNudge(
        from.id.ID,
        target.id.ID,
        subject.toSubjectType(),
        subject.id.ID,
        action,
        suffix
    )
}

private fun Contact.toSubjectType(): SubjectType {
    return when (this) {
        is OriginalMiraiGroup -> SubjectType.GROUP
        is OriginalMiraiStranger -> SubjectType.STRANGER
        is OriginalMiraiFriend -> SubjectType.FRIEND
        is OriginalMiraiMember -> SubjectType.MEMBER
        else -> throw NoSuchElementException("Subject type by contact $this")
    }
}