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
import love.forte.simbot.action.DeleteSupport
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.definition.GroupMember
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.utils.runInBlocking
import net.mamoe.mirai.contact.PermissionDeniedException
import java.util.stream.Stream
import kotlin.time.Duration


/**
 * @see net.mamoe.mirai.contact.Member
 */
public typealias NativeMiraiMember = net.mamoe.mirai.contact.Member

/**
 * @see net.mamoe.mirai.contact.NormalMember
 */
public typealias NativeNormalMiraiMember = net.mamoe.mirai.contact.NormalMember

/**
 * 一个由simbot包装为 [GroupMember] 的 [NativeMiraiMember] 对象。
 *
 * ### [DeleteSupport]
 * 一个 mirai 的群成员是 [支持删除][DeleteSupport] 操作的. [delete] 行为相当于 [踢出][net.mamoe.mirai.contact.NormalMember.kick] 操作。
 *
 * 当 [nativeContact] 的类型不是 [NativeNormalMiraiMember] 的时候，[delete] 行为将会无效。
 *
 * @see NativeMiraiMember
 * @author ForteScarlet
 */
public interface MiraiMember : GroupMember, MiraiContact, ReplySupport, DeleteSupport {

    override val nativeContact: NativeMiraiMember

    override val bot: MiraiBot
    override val id: LongID

    @OptIn(Api4J::class)
    override val group: MiraiGroup

    @JvmSynthetic
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember>

    @JvmSynthetic
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember>

    @JvmSynthetic
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember>

    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember>


    //// Impl


    //region send support
    @JvmSynthetic
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiMember> =
        send(message.messages)

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember> = runInBlocking { send(text) }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember> =
        runInBlocking { send(message) }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiMember> =
        runInBlocking { send(message) }
    //endregion


    //region reply support
    @JvmSynthetic
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
    //endregion


    /**
     * 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @param message 踢出时提供的消息。可能无实际意义。
     * @throws PermissionDeniedException 无权限修改时. see [net.mamoe.mirai.contact.NormalMember.kick].
     * @return 是否为普通成员且踢出执行成功。
     */
    public suspend fun kick(message: String, block: Boolean): Boolean {
        val contact = nativeContact
        if (contact is NativeNormalMiraiMember) {
            contact.kick(message, block)
            return true
        }
        return false
    }

    /**
     * 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @param message 踢出时提供的消息。可能无实际意义。
     * @throws PermissionDeniedException 无权限修改时. see [net.mamoe.mirai.contact.NormalMember.kick].
     * @return 是否为普通成员且踢出执行成功。
     */
    public suspend fun kick(message: String): Boolean = kick("", false)

    /**
     * 行为同 [kick], 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @see kick
     */
    override suspend fun delete(): Boolean = kick("")

    /**
     * 行为同 [kick], 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @see kick
     */
    @Api4J
    override fun deleteBlocking(): Boolean = runInBlocking { kick("") }


    @JvmSynthetic
    override suspend fun group(): MiraiGroup = group

    @JvmSynthetic
    override suspend fun organization(): MiraiGroup = group

    @OptIn(Api4J::class)
    override val organization: MiraiGroup
        get() = group


    @JvmSynthetic
    override suspend fun mute(duration: Duration): Boolean {
        nativeContact.mute(duration.inWholeSeconds.toInt())
        return true
    }

    @JvmSynthetic
    override suspend fun unmute(): Boolean {
        nativeContact.mute(0)
        return true
    }

    @OptIn(Api4J::class)
    override val roles: Stream<MemberRole>

    @JvmSynthetic
    override suspend fun roles(): Flow<MemberRole>

    //// Impl

    override val joinTime: Timestamp get() = Timestamp.NotSupport
    override val nickname: String get() = nativeContact.nameCard
    override val avatar: String get() = nativeContact.avatarUrl
    override val username: String get() = nativeContact.nick
}