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


