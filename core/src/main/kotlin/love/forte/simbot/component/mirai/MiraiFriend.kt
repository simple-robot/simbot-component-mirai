package love.forte.simbot.component.mirai

import love.forte.simbot.Grouping
import love.forte.simbot.LongID
import love.forte.simbot.definition.Friend

/**
 * Mirai原生类型。
 *
 * @see net.mamoe.mirai.contact.Friend
 */
public typealias NativeMiraiFriend = net.mamoe.mirai.contact.Friend


/**
 *
 * 在simbot中 [NativeMiraiFriend] 的表现形式。
 *
 * @author ForteScarlet
 */
public interface MiraiFriend : Friend, MiraiContact {
    override val bot: MiraiBot
    override val id: LongID

    /**
     * mirai原生的好友对象。
     */
    override val nativeContact: NativeMiraiFriend


    override val avatar: String get() = nativeContact.avatarUrl

    /**
     * 无法得到好友的分组信息。
     */
    override val grouping: Grouping get() = Grouping.EMPTY
    override val username: String get() = nativeContact.nick
    override val remark: String? get() = nativeContact.remark.takeIf { it.isNotEmpty() }
}

