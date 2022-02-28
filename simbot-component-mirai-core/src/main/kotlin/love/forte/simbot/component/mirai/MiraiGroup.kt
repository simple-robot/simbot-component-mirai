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

import kotlinx.coroutines.flow.*
import love.forte.simbot.*
import love.forte.simbot.definition.*
import love.forte.simbot.message.*
import love.forte.simbot.utils.*
import java.util.stream.*
import kotlin.time.*


public typealias NativeMiraiGroup = net.mamoe.mirai.contact.Group

/**
 *
 * TODO 注释
 * @author ForteScarlet
 */
public interface MiraiGroup : Group, MiraiChatroom {
    override val nativeContact: NativeMiraiGroup

    override val bot: MiraiBot
    override val id: LongID

    @OptIn(Api4J::class)
    override val owner: MiraiMember
    override val ownerId: LongID

    @JvmSynthetic
    override suspend fun members(groupingId: ID?, limiter: Limiter): Flow<MiraiMember>

    @OptIn(Api4J::class)
    override fun getMembers(groupingId: ID?, limiter: Limiter): Stream<out MiraiMember>

    @JvmSynthetic
    override suspend fun mute(duration: Duration): Boolean

    @JvmSynthetic
    override suspend fun member(id: ID): MiraiMember?

    @JvmSynthetic
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiGroup>
    @JvmSynthetic
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiGroup>

    // Impl


    @JvmSynthetic
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiGroup>
            = send(message.messages)

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiGroup>
            = runInBlocking { send(text) }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiGroup>
            = runInBlocking { send(message) }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiGroup>
            = runInBlocking { send(message) }


    /**
     * Mirai中，一个群内可能出现的权限是固定的。
     *
     * @see MemberRole
     */
    @OptIn(Api4J::class)
    override fun getRoles(groupingId: ID?, limiter: Limiter): Stream<MemberRole> = Stream.of(*MemberRole.values())

    /**
     * Mirai中，一个群内可能出现的权限是固定的。
     *
     * @see MemberRole
     */
    @JvmSynthetic
    override suspend fun roles(groupingId: ID?, limiter: Limiter): Flow<MemberRole> = MemberRole.values().asFlow()

    override val icon: String get() = nativeContact.avatarUrl
    override val name: String get() = nativeContact.name
    override val createTime: Timestamp get() = Timestamp.NotSupport
    override val currentMember: Int get() = nativeContact.members.size
    override val description: String get() = ""
    override val maximumMember: Int get() = -1


    @JvmSynthetic
    override suspend fun owner(): MiraiMember = owner


    @OptIn(Api4J::class)
    override fun getMembers(groupingId: ID?): Stream<out MiraiMember> = getMembers(groupingId, Limiter)

    @OptIn(Api4J::class)
    override fun getMembers(limiter: Limiter): Stream<out MiraiMember> = getMembers(null, limiter)

    @OptIn(Api4J::class)
    override fun getMembers(): Stream<out MiraiMember> = getMembers(null, Limiter)

    override fun getMember(id: ID): MiraiMember? = runInBlocking { member(id) }

    @JvmSynthetic
    override suspend fun unmute(): Boolean {
        val settings = nativeContact.settings
        val muteAll = settings.isMuteAll
        return if (muteAll) {
            nativeContact.settings.isMuteAll = false
            true
        } else false
    }

    @JvmSynthetic
    override suspend fun previous(): Group? = null
}

