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

package love.forte.simbot.component.mirai.event.impl

import love.forte.simbot.ID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.event.*
import love.forte.simbot.component.mirai.internal.MiraiBotImpl
import love.forte.simbot.definition.GroupInfo

/**
 *
 * @author ForteScarlet
 */
internal class MiraiBotInvitedJoinGroupRequestEventImpl(
    override val bot: MiraiBotImpl,
    nativeEvent: NativeMiraiBotInvitedJoinGroupRequestEvent
) : MiraiBotInvitedJoinGroupRequestEvent {

    override val timestamp: Timestamp = Timestamp.now()
    override val group: GroupInfo = InvitedJoinGroupInfo(nativeEvent.groupId, nativeEvent.groupName)

    override val inviter: InvitorUserInfo = InvitorUserInfo(
        nativeEvent.invitor,
        nativeEvent.invitorId,
        nativeEvent.invitorNick
    )
    override val metadata: MiraiSimbotEvent.Metadata<NativeMiraiBotInvitedJoinGroupRequestEvent> =
        nativeEvent.toSimpleMetadata(nativeEvent.eventId.ID)
}

private data class InvitedJoinGroupInfo(private val groupId: Long, private val groupName: String) : GroupInfo {
    override val createTime: Timestamp get() = Timestamp.NotSupport
    override val currentMember: Int get() = -1
    override val description: String get() = ""
    override val icon: String
        get() = "https://p.qlogo.cn/gh/$groupId/$groupId/640"
    override val maximumMember: Int get() = -1
    override val name: String get() = groupName
    override val ownerId: ID get() = emptyID

    companion object {
        private val emptyID = "".ID
    }
}