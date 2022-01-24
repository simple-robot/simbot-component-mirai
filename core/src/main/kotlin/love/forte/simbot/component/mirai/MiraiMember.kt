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

import kotlinx.coroutines.flow.Flow
import love.forte.simbot.Api4J
import love.forte.simbot.LongID
import love.forte.simbot.Timestamp
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.definition.GroupMember
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.utils.runInBlocking
import java.util.stream.Stream
import kotlin.time.Duration


/**
 * @see net.mamoe.mirai.contact.Member
 */
public typealias NativeMiraiMember = net.mamoe.mirai.contact.Member

/**
 * 一个由simbot包装为 [GroupMember] 的 [NativeMiraiMember] 对象。
 *
 * @see NativeMiraiMember
 * @author ForteScarlet
 */
public interface MiraiMember : GroupMember, MiraiContact, ReplySupport {

    override val nativeContact: NativeMiraiMember

    override val bot: MiraiBot
    override val id: LongID

    @OptIn(Api4J::class)
    override val group: MiraiGroup

    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember>
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember>
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember>
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember>


    //// Impl


    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiMember> =
        send(message.messages)

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember> =
        runInBlocking { send(text) }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember> =
        runInBlocking { send(message) }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiMember>  =
        runInBlocking { send(message) }


    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiMember> =
        reply(message.messages)

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember> =
        runInBlocking { reply(text) }

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember> =
        runInBlocking { reply(message) }

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiMember> =
        runInBlocking { reply(message) }


    override suspend fun group(): MiraiGroup = group
    override suspend fun organization(): MiraiGroup = group

    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group


    override suspend fun mute(duration: Duration): Boolean {
        nativeContact.mute(duration.inWholeSeconds.toInt())
        return true
    }

    override suspend fun unmute(): Boolean {
        nativeContact.mute(0)
        return true
    }

    @OptIn(Api4J::class)
    override val roles: Stream<MemberRole>
    override suspend fun roles(): Flow<MemberRole>

    //// Impl

    override val joinTime: Timestamp get() = Timestamp.NotSupport
    override val nickname: String get() = nativeContact.nameCard
    override val avatar: String get() = nativeContact.avatarUrl
    override val username: String get() = nativeContact.nick
}