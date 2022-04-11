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
 */

package love.forte.simbot.component.mirai

import love.forte.simbot.*
import love.forte.simbot.action.*
import love.forte.simbot.definition.*
import love.forte.simbot.message.*
import love.forte.simbot.utils.*
import net.mamoe.mirai.contact.Friend as OriginalMiraiFriend



/**
 *
 * 在simbot中 [OriginalMiraiFriend] 的表现形式。
 *
 * ### [DeleteSupport]
 *
 * mirai好友支持 [删除操作][DeleteSupport]. [delete] 相当于删除好友，等同于 [net.mamoe.mirai.contact.Friend1.delete].
 *
 * @author ForteScarlet
 */
public interface MiraiFriend : Friend, MiraiContact, DeleteSupport {
    override val bot: MiraiBot
    override val id: LongID

    /**
     * mirai原生的好友对象。
     */
    override val originalContact: OriginalMiraiFriend
    @JvmSynthetic
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend>
    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend>

    //// Impl

    //region send support
    @JvmSynthetic
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend> =
        send(message.messages)

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<OriginalMiraiFriend> = runInBlocking { send(text) }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiFriend> =
        runInBlocking { send(message) }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiFriend> =
        runInBlocking { send(message) }
    //endregion

    /**
     * 行为同 [net.mamoe.mirai.contact.Friend1.delete], 删除当前好友。
     *
     * @see net.mamoe.mirai.contact.Friend1.delete
     * @return true.
     */
    override suspend fun delete(): Boolean {
        originalContact.delete()
        return true
    }

    /**
     * 行为同 [net.mamoe.mirai.contact.Friend1.delete], 删除当前好友。
     *
     * @see net.mamoe.mirai.contact.Friend1.delete
     * @see delete
     * @return true.
     */
    @Api4J
    override fun deleteBlocking(): Boolean = runInBlocking { delete() }


    override val avatar: String get() = originalContact.avatarUrl

    /**
     * 无法得到好友的分组信息。
     */
    override val grouping: Grouping get() = Grouping.EMPTY
    override val username: String get() = originalContact.nick
    override val remark: String? get() = originalContact.remark.takeIf { it.isNotEmpty() }
}

