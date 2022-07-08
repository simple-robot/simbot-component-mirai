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

package love.forte.simbot.component.mirai.bot

import love.forte.simbot.Api4J
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.bot.*
import love.forte.simbot.component.mirai.*
import love.forte.simbot.component.mirai.message.MiraiImage
import love.forte.simbot.component.mirai.message.MiraiSendOnlyImage
import love.forte.simbot.definition.FriendsContainer
import love.forte.simbot.definition.GroupBot
import love.forte.simbot.definition.Guild
import love.forte.simbot.definition.UserInfo
import love.forte.simbot.event.EventProcessor
import love.forte.simbot.message.Image
import love.forte.simbot.resources.Resource
import love.forte.simbot.utils.item.Items
import love.forte.simbot.utils.item.Items.Companion.emptyItems
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.supervisorJob
import org.slf4j.Logger
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
public interface MiraiBot : Bot, UserInfo, FriendsContainer {
    
    /**
     * 得到自己。
     */
    override val bot: MiraiBot
        get() = this
    
    /**
     * 得到这个Bot所代表的原生mirai bot。
     *
     * @see OriginalMiraiBot
     */
    public val originalBot: OriginalMiraiBot
    
    /**
     * bot的id。
     *
     * 在mirai中，bot的账号都是 [Long] 类型的。
     */
    override val id: LongID
    
    /**
     * @see Bot.eventProcessor
     */
    override val eventProcessor: EventProcessor
    
    
    /**
     * @see Bot.logger
     */
    override val logger: Logger
    
    
    /**
     *  @see Bot.avatar
     */
    override val avatar: String get() = originalBot.avatarUrl
    
    /** 直接使用 [originalBot] 的协程作用域。 */
    override val coroutineContext: CoroutineContext get() = originalBot.coroutineContext
    
    override val isActive: Boolean get() = originalBot.supervisorJob.isActive
    override val isCancelled: Boolean get() = originalBot.supervisorJob.isCancelled
    override val isStarted: Boolean get() = isCancelled || isActive
    
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
    @OptIn(Api4J::class)
    override fun getFriend(id: ID): MiraiFriend?
    
    
    /**
     * 获取指定的好友。在mirai中，好友的获取不是挂起的，因此可以安全的使用 [getFriend]
     */
    @JvmSynthetic
    override suspend fun friend(id: ID): MiraiFriend? = getFriend(id)
    
    // endregion
    
    // region contacts api
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
    @JvmSynthetic
    public suspend fun stranger(id: ID): MiraiStranger? = getStranger(id)
    
    
    /**
     * 根据唯一标识获取一个陌生人。
     *
     * @see OriginalMiraiBot.getStranger
     */
    public fun getStranger(id: ID): MiraiStranger?
    // endregion
    
    // region contacts api
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
    @JvmSynthetic
    override suspend fun contact(id: ID): MiraiContact? = getContact(id)
    
    /**
     * 尝试获取一个联系人。
     *
     * 会优先尝试获取一个 [好友][MiraiFriend]，当没找到的时候会尝试寻找一个符合条件的 [陌生人][MiraiStranger]。
     * 找不到符合条件的目标时返回 `null`。
     *
     * 不会寻找 [群成员][MiraiMember].
     *
     */
    @OptIn(Api4J::class)
    override fun getContact(id: ID): MiraiContact?
    // endregion
    
    
    // region group apis
    
    /**
     * 获取当前Bot中的群组序列。
     *
     * 在mirai中，没有实际的限流或分页api，本质上得到的就是列表。
     */
    override val groups: Items<MiraiGroup>
    
    /**
     * 获取指定的群.
     * mirai的群组获取没有真正的挂起，因此可以安全的使用 [getGroup].
     */
    @OptIn(Api4J::class)
    override fun getGroup(id: ID): MiraiGroup?
    
    /**
     * 获取指定的群.
     * mirai的群组获取没有真正的挂起，因此可以安全的使用 [getGroup].
     * @see getGroup
     */
    @JvmSynthetic
    override suspend fun group(id: ID): MiraiGroup? = getGroup(id)
    // endregion
    
    
    // region guild apis
    
    @Deprecated(
        "Channel related APIs are not supported",
        ReplaceWith("emptyItems()", "love.forte.simbot.utils.item.Items.Companion.emptyItems")
    )
    override val guilds: Items<Guild>
        get() = emptyItems()
    
    @Deprecated("Channel related APIs are not supported", ReplaceWith("null"))
    @JvmSynthetic
    override suspend fun guild(id: ID): Guild? = null
    
    @OptIn(Api4J::class)
    @Deprecated("Channel related APIs are not supported", ReplaceWith("null"))
    override fun getGuild(id: ID): Guild? = null
    
    // endregion
    
    // region image api
    /**
     * 通过 [resource] 构建得到一个可以且仅可用于在mirai组件中进行 **发送** 的图片消息对象。
     *
     * 如果通过 [love.forte.simbot.resources.Resource] 来构建 [Image],
     * 那么得到的 [Image] 对象只是一个尚未初始化的伪[Image], 他会在发送消息的时候根据对应的 [net.mamoe.mirai.contact.Contact] 来进行上传并发送。
     */
    public fun sendOnlyImage(resource: Resource, flash: Boolean): MiraiSendOnlyImage
    
    /**
     * @see sendOnlyImage
     */
    @JvmSynthetic
    @Deprecated("Just use sendOnlyImage(resource, flash)", ReplaceWith("sendOnlyImage(resource, flash)"))
    public suspend fun uploadImage(resource: Resource, flash: Boolean): MiraiSendOnlyImage =
        sendOnlyImage(resource, flash)
    
    /**
     * @see sendOnlyImage
     */
    @Deprecated("Just use sendOnlyImage(resource, flash)", ReplaceWith("sendOnlyImage(resource, flash)"))
    public fun uploadImageBlocking(resource: Resource, flash: Boolean): MiraiSendOnlyImage =
        sendOnlyImage(resource, flash)
    
    
    //// id image
    
    /**
     * 尝试通过一个 [ID] 解析得到一个图片对象。
     * 当使用 [ID]的时候， 会直接通过mirai的函数
     * [net.mamoe.mirai.message.data.Image] 直接通过此ID获取对应图片。
     * 此时的 [Image] 对象是可以序列化的。
     */
    public fun idImage(
        id: ID,
        flash: Boolean,
        builderAction: net.mamoe.mirai.message.data.Image.Builder.() -> Unit = {},
    ): MiraiImage
    
    
    /**
     * @see idImage
     */
    @JvmSynthetic
    override suspend fun resolveImage(id: ID): MiraiImage = idImage(id, false)
    
    /**
     * @see idImage
     */
    public fun resolveImage(
        id: ID,
        flash: Boolean,
        builderAction: net.mamoe.mirai.message.data.Image.Builder.() -> Unit = {},
    ): MiraiImage = idImage(id, flash, builderAction)
    
    /**
     * @see idImage
     */
    @OptIn(Api4J::class)
    override fun resolveImageBlocking(id: ID): MiraiImage = idImage(id, false)
    
    
    /**
     * @see idImage
     */
    public fun resolveImageBlocking(
        id: ID,
        flash: Boolean,
        builderAction: net.mamoe.mirai.message.data.Image.Builder.() -> Unit,
    ): MiraiImage = idImage(id, flash, builderAction)
    // endregion
    
    
    @JvmSynthetic
    override suspend fun join() {
        originalBot.join()
    }
    
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
    @JvmSynthetic
    override suspend fun asMember(): MiraiMember
    
    /**
     * 当前bot在指定群中所扮演的成员实例。
     *
     */
    @OptIn(Api4J::class)
    override fun toMember(): MiraiMember
}

