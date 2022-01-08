package love.forte.simbot.component.mirai

import love.forte.simbot.Bot
import love.forte.simbot.Grouping
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.definition.Friend
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageReceipt


public typealias NativeMiraiFriend = net.mamoe.mirai.contact.Friend


/**
 *
 * 在simbot中 [NativeMiraiFriend] 的表现形式。
 *
 * @author ForteScarlet
 */
public interface MiraiFriend : Friend {
    override val bot: MiraiBot
    override val id: LongID

    /**
     * mirai原生的好友对象。
     */
    public val nativeFriend: NativeMiraiFriend


    override val avatar: String get() = nativeFriend.avatarUrl

    /**
     * 无法得到好友的分组信息。
     */
    override val grouping: Grouping get() = Grouping.EMPTY
    override val username: String get() = nativeFriend.nick
    override val remark: String? get() = nativeFriend.remark.takeIf { it.isNotEmpty() }
}