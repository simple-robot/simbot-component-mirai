package love.forte.simbot.component.mirai

import love.forte.simbot.definition.Contact
import love.forte.simbot.definition.UserStatus


/**
 * @see net.mamoe.mirai.contact.Stranger
 */
public typealias NativeMiraiStranger = net.mamoe.mirai.contact.Stranger

/**
 *
 * Mirai的陌生人对象实例。
 * @author ForteScarlet
 */
public interface MiraiStranger : Contact, MiraiContact {

    override val bot: MiraiBot
    override val nativeContact: NativeMiraiStranger

    override val avatar: String
        get() = nativeContact.avatarUrl

    override val status: UserStatus
        get() = strangerStatus

    override val username: String
        get() = nativeContact.nick

}

private val strangerStatus = UserStatus.builder().normal().build()