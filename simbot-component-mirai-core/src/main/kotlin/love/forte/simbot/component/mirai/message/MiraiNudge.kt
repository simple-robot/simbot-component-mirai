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

import kotlinx.coroutines.*
import kotlinx.serialization.*
import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.*
import love.forte.simbot.event.*
import love.forte.simbot.message.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.action.Nudge.Companion.sendNudge

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
) : MiraiSendOnlyComputableMessage<MiraiNudge> {
    override val key: Message.Key<MiraiNudge> get() = Key

    @OptIn(InternalApi::class)
    @JvmSynthetic
    override suspend fun originalMiraiMessage(contact: Contact): OriginalMiraiMessage {
        if (target != null) {
            if (target.number == contact.bot.id) {
                contact.sendNudge(contact.bot.nudge())
                return EmptySingleMessage
            }

            when (contact) {
                is OriginalMiraiGroup -> {
                    val nudge = contact.getMemberOrFail(target.number).nudge()
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
        override fun safeCast(value: Any): MiraiNudge? = doSafeCast(value)
    }
}


@SerialName("mirai.nudgeMessage")
@Serializable
public class MiraiNudgeMessage {
    // TODO

}