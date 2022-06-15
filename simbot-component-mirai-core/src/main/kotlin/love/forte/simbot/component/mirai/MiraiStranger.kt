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

package love.forte.simbot.component.mirai

import love.forte.simbot.Api4J
import love.forte.simbot.definition.Contact
import love.forte.simbot.definition.Stranger
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import net.mamoe.mirai.contact.Stranger as OriginalMiraiStranger


/**
 *
 * Mirai的陌生人对象实例。
 * @author ForteScarlet
 */
public interface MiraiStranger : Contact, Stranger, MiraiContact {
    
    override val bot: MiraiBot
    override val originalContact: OriginalMiraiStranger
    
    override val avatar: String
        get() = originalContact.avatarUrl
    
    override val username: String
        get() = originalContact.nick
    
    
    /**
     * 向此人发送消息。
     */
    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    /**
     * 向此人发送消息。
     */
    @JvmSynthetic
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    /**
     * 向此人发送消息。
     */
    @JvmSynthetic
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    /**
     * 向此人发送消息。
     */
    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    /**
     * 向此人发送消息。
     */
    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
    
    /**
     * 向此人发送消息。
     */
    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiStranger>
}
