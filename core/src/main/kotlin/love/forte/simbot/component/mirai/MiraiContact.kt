package love.forte.simbot.component.mirai

import love.forte.simbot.LongID
import love.forte.simbot.definition.BotContainer
import love.forte.simbot.definition.Contact
import love.forte.simbot.definition.Organization

/**
 * Mirai的原生类型
 *
 * @see net.mamoe.mirai.contact.Contact
 */
public typealias NativeMiraiContact = net.mamoe.mirai.contact.Contact


/**
 * [MiraiBot] 容器类型。
 */
public interface MiraiBotContainer : BotContainer {
    override val bot: MiraiBot
}


/**
 * 包含了mirai原生联系人[NativeMiraiContact] 的容器类型.
 */
public interface MiraiContactContainer {
    public val nativeContact: NativeMiraiContact
}


/**
 * [Contact] 对应Mirai的 [联系人][NativeMiraiContact] 类型。
 *
 * @author ForteScarlet
 *
 * @see MiraiFriend
 * @see MiraiMember
 */
public interface MiraiContact : Contact, MiraiBotContainer, MiraiContactContainer {
    override val bot: MiraiBot
    override val nativeContact: NativeMiraiContact
    override val id: LongID

}

/**
 * [Organization] 对应Mirai的 [联系人][NativeMiraiContact] 类型。
 *
 * @see MiraiGroup
 */
public interface MiraiOrganization : Organization, MiraiBotContainer, MiraiContactContainer {
    override val bot: MiraiBot
    override val nativeContact: NativeMiraiGroup
    override val id: LongID

}