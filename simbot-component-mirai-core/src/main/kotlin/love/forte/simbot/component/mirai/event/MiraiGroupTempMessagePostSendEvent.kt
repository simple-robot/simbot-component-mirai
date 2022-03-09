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

package love.forte.simbot.component.mirai.event

import love.forte.simbot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.definition.*
import love.forte.simbot.event.*
import love.forte.simbot.message.*

/**
 * 群临时会话消息发送后的消息事件。此事件不会实现 [ContactMessageEvent], 取而代之的是使用 [GroupInfoContainer], [MemberInfoContainer], [MessageEvent].
 * 此消息本质上并非"群成员的临时消息"，而只是对bot的行为的后置处理。
 *
 * @author ForteScarlet
 */
public interface MiraiGroupTempMessagePostSendEvent :
    MiraiMessagePostSendEvent<OriginalMiraiMember, OriginalMiraiGroupTempMessagePostSendEvent>,
    GroupInfoContainer, MemberInfoContainer, MessageEvent {

    override val bot: MiraiBot
    override val id: ID
    override val timestamp: Timestamp
    override val messageContent: MiraiReceivedMessageContent
    override val originalEvent: OriginalMiraiGroupTempMessagePostSendEvent

    /**
     * 发送目标群成员所属群对象。
     */
    @OptIn(Api4J::class)
    override val group: MiraiGroup

    /**
     * 发送目标群成员对象。
     */
    @OptIn(Api4J::class)
    override val member: MiraiMember


    // Impl
    /**
     * 发送目标群成员所属群对象。
     */
    override suspend fun group(): MiraiGroup = group

    /**
     * 发送目标群成员对象。
     */
    override suspend fun member(): MemberInfo = member

    /**
     * 所有 `post send` 相关事件的源头均来自于bot自身。
     */
    @OptIn(Api4J::class)
    override val source: MiraiBot
        get() = bot

    /**
     * 所有 `post send` 相关事件的源头均来自于bot自身。
     */
    override suspend fun source(): MiraiBot = bot


    override val key: Event.Key<out MiraiGroupTempMessagePostSendEvent> get() = Key

    public companion object Key : BaseEventKey<MiraiGroupTempMessagePostSendEvent>(
        "mirai.group_temp_message_post_send_event", MiraiMessagePostSendEvent, MessageEvent
    ) {
        override fun safeCast(value: Any): MiraiGroupTempMessagePostSendEvent? = doSafeCast(value)
    }
}