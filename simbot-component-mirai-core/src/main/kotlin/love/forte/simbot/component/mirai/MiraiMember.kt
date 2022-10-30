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

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.simbot.Api4J
import love.forte.simbot.JavaDuration
import love.forte.simbot.LongID
import love.forte.simbot.Timestamp
import love.forte.simbot.action.DeleteSupport
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.definition.GroupMember
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.utils.item.Items
import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.PermissionDeniedException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.mamoe.mirai.contact.Member as OriginalMiraiMember
import net.mamoe.mirai.contact.NormalMember as OriginalMiraiNormalMember


/**
 * 一个由simbot包装为 [GroupMember] 的 [OriginalMiraiMember] 对象。
 *
 * ### [DeleteSupport]
 * 一个 mirai 的群成员是 [支持删除][DeleteSupport] 操作的, [delete] 行为相当于 [踢出][net.mamoe.mirai.contact.NormalMember.kick] 操作。
 *
 * 当 [originalContact] 的类型不是 [OriginalMiraiNormalMember] 的时候，[delete] 行为将会无效。
 *
 * @see OriginalMiraiMember
 * @author ForteScarlet
 */
public interface MiraiMember : GroupMember, MiraiContact, DeleteSupport {
    
    override val originalContact: OriginalMiraiMember
    
    override val bot: MiraiBot
    override val id: LongID
    
    /**
     * 获取此成员用户名
     */
    override val username: String get() = originalContact.nick
    
    /**
     * 用于判断当前 [MiraiMember] 中所代表的 [originalContact] 是否为一个匿名成员。
     * 可以通过 [isAnonymous] 来提前规避可能会因为是匿名而导致的异常，例如 [nickname] 的 setter。
     *
     * ```kotlin
     * // safely
     * if (!member.isAnonymous) {
     *    member.nickname = "new_nick"
     * }
     * ```
     *
     * @see isNotAnonymous
     * @return 如果 [MiraiMember.originalContact] 是匿名成员
     */
    public val isAnonymous: Boolean get() = originalContact is AnonymousMember
    
    /**
     * 当前群成员在此群中的昵称（或者为名片）。
     *
     * [nickname] 可修改，但是仅限于当 [originalContact] 类型为 [NormalMember][OriginalMiraiNormalMember]
     * 时才允许修改，修改时行为与 [OriginalMiraiNormalMember.nameCard] 的行为一致。
     * 当类型不匹配时，将会抛出 [UnsupportedOperationException]。
     *
     * @see OriginalMiraiMember.nameCard
     *
     */
    override var nickname: String
        get() = originalContact.nameCard
        set(value) {
            when (val member = originalContact) {
                is OriginalMiraiNormalMember -> {
                    member.nameCard = value
                }
                
                else -> throw UnsupportedOperationException("member $originalContact type is not NormalMember")
            }
        }
    
    
    /**
     * 当前群成员的 _群特殊头衔_。
     *
     *  [specialTitle] 可修改，但是仅限于当 [originalContact] 类型为 [NormalMember][OriginalMiraiNormalMember]
     * 时才允许修改，修改时行为与 [OriginalMiraiNormalMember.specialTitle] 的行为一致。
     * 当类型不匹配时，将会抛出 [UnsupportedOperationException]。
     *
     * @see OriginalMiraiMember.specialTitle
     */
    public var specialTitle: String
        get() = originalContact.specialTitle
        set(value) {
            when (val member = originalContact) {
                is OriginalMiraiNormalMember -> {
                    member.specialTitle = value
                }
                
                else -> throw UnsupportedOperationException("member $originalContact type is not NormalMember")
            }
        }
    
    
    /**
     * 被禁言的剩余时间（秒）。如果未被禁言、或者 [originalContact] 为匿名成员而无法得到时间，则得到 `0`。
     */
    public val muteTimeRemainingSeconds: Int
    
    /**
     * 被禁言的剩余时间。如果未被禁言、或者 [originalContact] 为匿名成员而无法得到时间，则得到 [Duration.ZERO]。
     */
    @get:JvmSynthetic
    public val muteTimeRemaining: Duration
        get() = if (muteTimeRemainingSeconds == 0) Duration.ZERO
        else muteTimeRemainingSeconds.seconds
    
    
    /**
     * 被禁言的剩余时间。如果未被禁言、或者 [originalContact] 为匿名成员而无法得到时间，则得到 [Duration.ZERO][java.time.Duration.ZERO]。
     */
    public val muteTimeRemainingDuration: JavaDuration
        get() = if (muteTimeRemainingSeconds == 0) JavaDuration.ZERO
        else JavaDuration.ofSeconds(muteTimeRemainingSeconds.toLong())
    
    /**
     * 判断当前成员是否处于禁言状态。
     */
    public val isMuted: Boolean get() = muteTimeRemainingSeconds > 0
    
    /**
     * 群成员入群时间。
     *
     * 当 [originalContact] 类型为 [NormalMember][OriginalMiraiNormalMember] 时，
     * 得到的结果为 [OriginalMiraiNormalMember.joinTimestamp]，否则将会得到 [Timestamp.NotSupport]。
     *
     * @see OriginalMiraiNormalMember.joinTimestamp
     */
    override val joinTime: Timestamp
    
    /**
     * 最后发言时间。
     *
     * 当 [originalContact] 类型为 [NormalMember][OriginalMiraiNormalMember] 时，
     * 得到的结果为 [OriginalMiraiNormalMember.lastSpeakTimestamp]，否则将会得到 `null`。
     *
     * @see OriginalMiraiNormalMember.lastSpeakTimestamp
     */
    public val lastSpeakTime: Timestamp?
    
    /**
     * 当前成员所述角色。
     */
    public val role: MemberRole get() = originalContact.simbotRole
    
    /**
     * 当前成员角色所属角色。通常内部只有一个元素: [role]。
     */
    override val roles: Items<MemberRole> get() = Items.items(role)
    
    /**
     * 获取此成员头像
     */
    override val avatar: String get() = originalContact.avatarUrl
    
    /**
     * 得到 [MiraiMemberActive]。
     * @see MiraiMemberActive
     */
    public val active: MiraiMemberActive
    
    /**
     * 得到此成员所属群。
     */
    @JvmBlocking(asProperty = true, suffix = "")
    @JvmAsync(asProperty = true)
    override suspend fun group(): MiraiGroup
    
    /**
     * 得到此成员所属群。
     */
    @JvmBlocking(asProperty = true, suffix = "")
    @JvmAsync(asProperty = true)
    override suspend fun organization(): MiraiGroup = group()
    
    /**
     * 向此群成员发送消息。
     */
    @JvmAsync
    @JvmBlocking
    override suspend fun send(text: String): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    
    /**
     * 向此群成员发送消息。
     */
    @JvmAsync
    @JvmBlocking
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<OriginalMiraiMember>
    
    
    /**
     * 向此群成员发送消息。
     */
    @JvmAsync
    @JvmBlocking
    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<OriginalMiraiMember> =
        send(message.messages)
    
    
    /**
     * 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @param message 踢出时提供的消息。可能无实际意义。
     * @param block 是否踢出后加入黑名单。
     * @throws PermissionDeniedException 无权限修改时. see [net.mamoe.mirai.contact.NormalMember.kick].
     * @return 是否为普通成员且踢出执行成功。
     */
    @JvmAsync
    @JvmBlocking
    public suspend fun kick(message: String, block: Boolean): Boolean
    
    /**
     * 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @param message 踢出时提供的消息。可能无实际意义。
     * @throws PermissionDeniedException 无权限修改时. see [net.mamoe.mirai.contact.NormalMember.kick].
     * @return 是否为普通成员且踢出执行成功。
     */
    @JvmAsync
    @JvmBlocking
    public suspend fun kick(message: String): Boolean = kick("", false)
    
    /**
     * 同 [kick], 如果当前群成员为普通群成员，则尝试踢出。否则将会返回 false。
     *
     * @see kick
     */
    @JvmSynthetic
    override suspend fun delete(): Boolean = kick("")
    
    
    /**
     * 尝试禁言此成员。
     *
     * @param duration 禁言时间。如果不提供或者提供值**等于0秒**（second == 0）则默认为最小时间：1分钟。
     * @return 当 [duration] 的秒值**小于0**（second < 0）时得到false，否则为true。
     */
    @JvmSynthetic
    override suspend fun mute(duration: Duration): Boolean
    
    /**
     * 尝试禁言此成员1分钟。
     */
    @Api4J
    override fun muteBlocking(): Boolean
    
    /**
     * 尝试禁言此成员。
     *
     * @param duration 禁言时间。如果不提供或者提供值**等于0秒**（second == 0）则默认为最小时间：1分钟。
     * @return 当 [duration] 的秒值**小于0**（second < 0）时得到false，否则为true。
     */
    @Api4J
    override fun muteBlocking(duration: JavaDuration): Boolean
    
    /**
     * 尝试禁言此成员。
     *
     * @param time 禁言时间。如果不提供值**等于0秒**（second == 0）则默认为最小时间：1分钟。
     * @param timeUnit 禁言时间单位。
     * @return 当 [time] 的秒值**小于0**（second < 0）时得到false，否则为true。
     */
    @JvmSynthetic
    override suspend fun mute(time: Long, timeUnit: TimeUnit): Boolean
    
    /**
     * 取消当前成员的禁言。
     *
     * @return 如果当前成员为匿名成员则得到false，否则为true。
     */
    @JvmSynthetic
    override suspend fun unmute(): Boolean {
        val normalMember = originalContact as? NormalMember ?: return false
        normalMember.unmute()
        return true
    }
    
    
    /**
     * 修改当前成员的管理员职位。
     *
     * Kotlin see also: [appoint], [dismiss]。
     *
     * @see NormalMember.modifyAdmin
     * @param operator 如果为 `true` 则为任命，否则为撤职。
     * @throws UnsupportedOperationException 如果当前成员为 [匿名成员][isAnonymous]
     * @throws PermissionDeniedException see [NormalMember.modifyAdmin]
     *
     */
    @JvmAsync
    @JvmBlocking
    public suspend fun modifyAdmin(operator: Boolean) {
        val member = originalContact as? NormalMember
            ?: throw UnsupportedOperationException("member $originalContact type is not NormalMember")
        member.modifyAdmin(operator)
    }
    
    
}


/**
 * [MiraiMember.isAnonymous] 取反。
 *
 * ```kotlin
 * // safely
 * if (member.isNotAnonymous) {
 *    member.nickname = "new_nick"
 * }
 * ```
 * @return 如果 [MiraiMember.originalContact] 不是匿名成员
 */
public inline val MiraiMember.isNotAnonymous: Boolean get() = !isAnonymous


/**
 * [MiraiMember.isMuted] 取反。
 */
public inline val MiraiMember.isNotMuted: Boolean get() = !isMuted

/**
 * 任命当前成员为管理员。
 * 同下：
 * ```kotlin
 * member.modifyAdmin(true)
 * ```
 * @see MiraiMember.modifyAdmin
 * @throws UnsupportedOperationException see [MiraiMember.modifyAdmin]
 * @throws PermissionDeniedException see [NormalMember.modifyAdmin]
 */
public suspend inline fun MiraiMember.appoint() {
    modifyAdmin(true)
}

/**
 * 将当前成员撤职（如果是管理员的话）。
 * 同下：
 * ```kotlin
 * member.modifyAdmin(false)
 * ```
 * @see MiraiMember.modifyAdmin
 * @throws UnsupportedOperationException see [MiraiMember.modifyAdmin]
 * @throws PermissionDeniedException see [NormalMember.modifyAdmin]
 */
public suspend inline fun MiraiMember.dismiss() {
    modifyAdmin(false)
}
