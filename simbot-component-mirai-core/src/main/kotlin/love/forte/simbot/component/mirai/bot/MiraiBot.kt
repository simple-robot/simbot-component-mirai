/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  This file is part of simbot-component-mirai.
 *
 *  simbot-component-mirai is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  simbot-component-mirai is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with simbot-component-mirai. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package love.forte.simbot.component.mirai.bot

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.simbot.*
import love.forte.simbot.bot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.MiraiImage
import love.forte.simbot.component.mirai.message.MiraiSendOnlyImage
import love.forte.simbot.definition.FriendsContainer
import love.forte.simbot.definition.GroupBot
import love.forte.simbot.definition.Guild
import love.forte.simbot.definition.SocialRelationsContainer.Companion.COUNT_NOT_SUPPORTED
import love.forte.simbot.definition.UserInfo
import love.forte.simbot.message.Image
import love.forte.simbot.resources.Resource
import love.forte.simbot.utils.item.Items
import love.forte.simbot.utils.item.Items.Companion.emptyItems
import net.mamoe.mirai.contact.AvatarSpec
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.friendgroup.FriendGroups
import net.mamoe.mirai.supervisorJob
import kotlin.coroutines.CoroutineContext
import net.mamoe.mirai.Bot as OriginalMiraiBot


/**
 *
 * Mirai的Bot [OriginalMiraiBot] 在 simbot中的整合类型。
 *
 * 当 [MiraiBot] 被关闭的时候（或者说 [originalBot] 被关闭的时候）会将自身移出所属的 [BotManager][manager].
 * 这一行为是由 [MiraiBotManager] 所决定的。
 *
 * 在 mirai 中, bot存在"好友"概念，因此 [MiraiBot] 实现 [FriendsContainer], 提供好友相关api。
 *
 * @see OriginalMiraiBot
 * @see Bot
 * @author ForteScarlet
 */
public interface MiraiBot : Bot, UserInfo, FriendsContainer, MiraiUserProfileQueryable {

    /**
     * 得到自己。
     */
    override val bot: MiraiBot
        get() = this

    /**
     * 得到这个Bot所代表的[原生mirai bot][OriginalMiraiBot]。
     *
     * @see OriginalMiraiBot
     */
    public val originalBot: OriginalMiraiBot

    /**
     * 好友分组
     * @see OriginalMiraiBot.friendGroups
     */
    public val friendCategories: MiraiFriendCategories

    /**
     * bot的id。
     *
     * 在mirai中，bot的账号都是 [Long] 类型的。
     */
    override val id: LongID


    /**
     * 获取当前bot的头像链接。规格默认为 [AvatarSpec.LARGEST]
     *  @see OriginalMiraiBot.avatarUrl
     */
    override val avatar: String get() = originalBot.avatarUrl

    /**
     * 获取头像链接。
     * @param spec 头像规格，为mirai原生类型 [AvatarSpec]。
     * @see net.mamoe.mirai.contact.ContactOrBot.avatarUrl
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("getAvatar")
    public fun avatar(spec: AvatarSpec): String = originalBot.avatarUrl(spec)

    /** 直接使用 [originalBot] 的协程作用域。 */
    override val coroutineContext: CoroutineContext get() = originalBot.coroutineContext

    override val isActive: Boolean get() = originalBot.supervisorJob.isActive
    override val isCancelled: Boolean get() = originalBot.supervisorJob.isCancelled
    override val isStarted: Boolean get() = isCancelled || isActive

    /**
     * 当前bot所属的bot管理器。
     *
     * @see MiraiBotManager
     */
    override val manager: MiraiBotManager

    /**
     * 得到用户名。
     *
     * @see OriginalMiraiBot.nick
     */
    override val username: String get() = originalBot.nick

    // region friends api
    /**
     * 获取当前bot的好友信息。
     *
     * 在mirai中，没有真正的分页API，本质上得到的就是列表。
     *
     */
    override val friends: Items<MiraiFriend>


    /**
     * 获取指定的好友。在mirai中，好友的获取不是挂起的，因此可以安全的使用 [getFriend]
     */
    @JvmBlocking(baseName = "getFriend", suffix = "")
    @JvmAsync(baseName = "getFriend")
    override suspend fun friend(id: ID): MiraiFriend?

    /**
     * 获取当前bot中所有好友的数量
     */
    @JvmSynthetic
    override suspend fun friendCount(): Int
    // endregion

    // region strangers api
    /**
     * 陌生人数据序列。
     *
     * 此序列中，会获取 [陌生人列表][strangers], 元素类型为 [MiraiStranger]。
     * 序列元素来自于 [原生Mirai Bot][OriginalMiraiBot] 中的 [strangers][OriginalMiraiBot.strangers].
     *
     * @see OriginalMiraiBot.strangers
     */
    public val strangers: Items<MiraiStranger>


    /**
     * 根据唯一标识获取一个陌生人。
     *
     * @see OriginalMiraiBot.getStranger
     */
    @JvmBlocking(baseName = "getStranger", suffix = "")
    @JvmAsync(baseName = "getStranger")
    public suspend fun stranger(id: ID): MiraiStranger?

    /**
     * 获取当前bot所有陌生人的数量。
     */
    public val strangerCount: Int

    // endregion

    // region contacts api

    /**
     * mirai 支持联系人 [contact][MiraiContact] 操作。
     * 在 mirai 组件中，联系人相当于好友 [friend][MiraiFriend]
     * 与陌生人 [stranger][MiraiStranger] 的汇总。
     */
    override val isContactsSupported: Boolean
        get() = true

    /**
     * 联系人数据序列。
     *
     * 此序列中，会先获取 [好友列表][friends], 元素类型为 [MiraiFriend] ,
     * 当好友信息迭代结束后，后续会获取 [陌生人列表][strangers], 元素类型为 [MiraiStranger].
     *
     * 此序列中不会出现 [群成员][MiraiMember] 类型.
     *
     */
    override val contacts: Items<MiraiContact>

    /**
     * 尝试获取一个联系人。
     *
     * 会优先尝试获取一个 [好友][MiraiFriend]，当没找到的时候会尝试寻找一个符合条件的 [陌生人][MiraiStranger]。
     * 找不到符合条件的目标时返回 `null`。
     *
     * 不会寻找 [群成员][MiraiMember].
     *
     */
    @JvmBlocking(baseName = "getContact", suffix = "")
    @JvmAsync(baseName = "getContact")
    override suspend fun contact(id: ID): MiraiContact?

    /**
     * 获取当前bot的所有联系人的数量
     */
    @JvmSynthetic
    override suspend fun contactCount(): Int
    // endregion


    // region group apis

    /**
     * mirai 组件中支持群 [group][MiraiGroup] 操作。
     */
    override val isGroupsSupported: Boolean
        get() = true

    /**
     * 获取当前Bot中的群组序列。
     *
     * 在mirai中，没有实际的限流或分页api，本质上得到的就是列表。
     */
    override val groups: Items<MiraiGroup>

    /**
     * 获取指定的群.
     * mirai的群组获取没有真正的挂起，因此可以安全的使用 [getGroup].
     * @see getGroup
     */
    @JvmBlocking(baseName = "getGroup", suffix = "")
    @JvmAsync(baseName = "getGroup")
    override suspend fun group(id: ID): MiraiGroup?

    /**
     * 获取当前bot中所有群的数量
     */
    @JvmSynthetic
    override suspend fun groupCount(): Int
    // endregion


    // region guild apis

    /**
     * mirai 组件中没有频道 `guild` 。
     */
    override val isGuildsSupported: Boolean
        get() = false

    /**
     * mirai中不存在'频道（guild）'概念。
     *
     */
    @Deprecated(
        "Channel related APIs are not supported",
        ReplaceWith("emptyItems()", "love.forte.simbot.utils.item.Items.Companion.emptyItems")
    )
    override val guilds: Items<Guild>
        get() = emptyItems()


    /**
     * mirai中不存在'频道（guild）'概念。
     *
     */
    @Deprecated("Channel related APIs are not supported", ReplaceWith("0"))
    @JvmSynthetic
    override suspend fun guildCount(): Int = COUNT_NOT_SUPPORTED


    /**
     * mirai中不存在'频道（guild）'概念。
     *
     */
    @Deprecated("Channel related APIs are not supported", ReplaceWith("null"))
    @JvmSynthetic
    override suspend fun guild(id: ID): Guild? = null
    // endregion

    // region image api
    /**
     * 通过 [resource] 构建得到一个可以且仅可用于在mirai组件中进行 **发送** 的图片消息对象。
     *
     * 如果通过 [love.forte.simbot.resources.Resource] 来构建 [Image],
     * 那么得到的 [Image] 对象只是一个尚未初始化的伪[Image], 他会在发送消息的时候根据对应的 [net.mamoe.mirai.contact.Contact] 来进行上传并发送。
     */
    public fun sendOnlyImage(resource: Resource, flash: Boolean): MiraiSendOnlyImage

    //// id image

    /**
     * 尝试通过一个 [ID] 解析得到一个图片对象。
     * 当使用 [ID]的时候， 会直接通过mirai的函数
     * [net.mamoe.mirai.message.data.Image] 直接通过此ID获取对应图片。
     * 此时的 [Image] 对象是可以序列化的。
     *
     * @param id 图片的ID
     * @param flash 是否标记为闪照
     * @param builderAction mirai原生图片类型的构建器函数
     */
    public fun idImage(
        id: ID,
        flash: Boolean,
        builderAction: net.mamoe.mirai.message.data.Image.Builder.() -> Unit = {},
    ): MiraiImage


    /**
     * 通过id构造一个 [MiraiImage].
     * @see idImage
     */
    @love.forte.simbot.component.mirai.JST
    override suspend fun resolveImage(id: ID): MiraiImage = idImage(id, false)

    /**
     * 通过id构造一个 [MiraiImage].
     * @see idImage
     */
    public fun resolveImage(
        id: ID,
        flash: Boolean,
        builderAction: net.mamoe.mirai.message.data.Image.Builder.() -> Unit = {},
    ): MiraiImage = idImage(id, flash, builderAction)
    // endregion

    /**
     * 挂起直到当前bot结束其任务。
     */
    @JvmSynthetic
    override suspend fun join() {
        originalBot.join()
    }

    /**
     * 关闭当前bot
     */
    @JvmSynthetic
    override suspend fun cancel(reason: Throwable?): Boolean {
        return if (isCancelled) false
        else true.also {
            originalBot.close(reason)
        }
    }
}


/**
 * mirai组件中针对于 [GroupBot] 的实现。
 *
 * @see GroupBot
 *
 */
@Suppress("UnnecessaryOptInAnnotation")
public interface MiraiGroupBot : MiraiBot, GroupBot {

    /**
     * 此bot在群中的[原生mirai群成员][NormalMember]实例。
     */
    public val originalBotMember: NormalMember

    /**
     * 当前bot在指定群中所扮演的成员实例。
     *
     */
    @JvmBlocking(baseName = "toMember", suffix = "")
    @JvmAsync(baseName = "toMember")
    override suspend fun asMember(): MiraiMember
}


/**
 * bot中的所有好友信息列表（管理器）
 *
 * @see OriginalMiraiBot.friendGroups
 * @see MiraiFriendCategory
 * @see MiraiBot.friendCategories
 */
public interface MiraiFriendCategories : Iterable<MiraiFriendCategory> {

    /**
     * Mirai原生的好友分组列表对象。
     *
     * @see FriendGroups
     */
    public val originalFriendGroups: FriendGroups

    /**
     * 得到ID为 `0` 的默认好友分组。
     *
     * @see FriendGroups
     */
    public val default: MiraiFriendCategory

    /**
     * 新建一个好友分组.
     *
     * @see FriendGroups.create
     */
    @love.forte.simbot.component.mirai.JST
    public suspend fun create(name: String): MiraiFriendCategory

    /**
     * 获取指定 ID 的好友分组, 不存在时返回 `null`
     *
     * @see FriendGroups.get
     * @throws NumberFormatException 当 [id] 内容无法转化为 [Int] 类型时
     */
    public operator fun get(id: ID): MiraiFriendCategory? {
        val idNumber = when (id) {
            is IntID -> id.number
            is NumericalID<*> -> id.toInt()
            else -> id.literal.toInt()
        }

        return get(idNumber)
    }

    /**
     * 获取指定 ID 的好友分组, 不存在时返回 `null`
     *
     * @see FriendGroups.get
     */
    public operator fun get(id: Int): MiraiFriendCategory?

    /**
     * 获取当前分组下所有的分组。
     *
     * 通过 [FriendGroups.asCollection] 的结果转化而来，但是 [iterator]
     * 的返回结果是不可变的一次性产物，与 [FriendGroups.asCollection] 不同。
     */
    override fun iterator(): Iterator<MiraiFriendCategory>
}
