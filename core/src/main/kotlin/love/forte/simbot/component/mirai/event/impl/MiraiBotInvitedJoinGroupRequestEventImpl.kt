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