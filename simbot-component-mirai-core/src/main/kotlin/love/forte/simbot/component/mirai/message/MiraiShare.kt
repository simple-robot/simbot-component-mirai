/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU Affero通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
 *
 *
 */

package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.message.Message
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.message.data.RichMessage
import net.mamoe.mirai.message.data.ServiceMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * mirai的(链接)分享模板实例。
 *
 * 此类型不会在接收消息中存在，仅用于发送。直接通过 [RichMessage.share] 构建真实的消息对象，
 * 请注意参考此函数的相关限制。
 *
 * 可以通过 [originalMiraiMessage] 获取构建后的 [ServiceMessage] 对象实例。
 *
 * @see RichMessage
 * @see RichMessage.share
 * @author ForteScarlet
 */
@MiraiExperimentalApi
@SerialName("mirai.rich.share")
@Serializable
public data class MiraiShare @JvmOverloads constructor(
    private val url: String,
    private val title: String? = null,
    private val content: String? = null,
    private val coverUrl: String? = null
) : MiraiSendOnlySimbotMessage<MiraiShare>,
    OriginalMiraiDirectlySimbotMessage<ServiceMessage, MiraiShare> {

    override val originalMiraiMessage: ServiceMessage = RichMessage.share(
        url, title, content, coverUrl
    )

    override val key: Message.Key<MiraiShare>
        get() = Key

    public companion object Key : Message.Key<MiraiShare> {
        override fun safeCast(value: Any): MiraiShare? = doSafeCast(value)
    }
}


