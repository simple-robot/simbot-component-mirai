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
import love.forte.simbot.Grouping
import love.forte.simbot.LongID
import love.forte.simbot.definition.Friend
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.utils.runInBlocking

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
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend>
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend>

    //// Impl

    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiFriend> =
        send(message.messages)

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiFriend> = runInBlocking { send(text) }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiFriend> =
        runInBlocking { send(message) }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiFriend> =
        runInBlocking { send(message) }


    override val avatar: String get() = nativeContact.avatarUrl

    /**
     * 无法得到好友的分组信息。
     */
    override val grouping: Grouping get() = Grouping.EMPTY
    override val username: String get() = nativeContact.nick
    override val remark: String? get() = nativeContact.remark.takeIf { it.isNotEmpty() }
}

