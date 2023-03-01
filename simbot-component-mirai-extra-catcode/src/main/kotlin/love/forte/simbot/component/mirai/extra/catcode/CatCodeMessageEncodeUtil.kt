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

@file:JvmName("CatCodeMessageUtil")
@file:JvmMultifileClass

package love.forte.simbot.component.mirai.extra.catcode

import catcode.CatCodeUtil
import catcode.Neko
import net.mamoe.mirai.message.data.*

/**
 * 将一个 [SingleMessage] 转化为携带cat字符串。
 * 普通文本会被转化为 [CAT:text,text=xxx]
 */
public fun SingleMessage.toNeko(): Neko {
    return when (this) {
        // at all
        AtAll -> AtAllCatCodeSerializer.encode(this)
        // at
        is At -> AtCatCodeSerializer.encode(this)
        // 普通文本, 转义
        is PlainText -> TextCatCodeSerializer.encode(this)
        // face
        is Face -> FaceCatCodeSerializer.encode(this)

        is Dice -> DiceCatCodeSerializer.encode(this)

        // market face
        is MarketFace -> MarketFaceCatCodeSerializer.encode(this)

        // vip face
        is VipFace -> VipFaceCatCodeSerializer.encode(this)

        is PokeMessage -> PokeCatCodeSerializer.encode(this)
        is Image, is FlashImage -> ImageCatCodeSerializer.encode(this)
        is Audio -> AudioCatCodeSerializer.encode(this)
        is FileMessage -> FileCatCodeSerializer.encode(this)


        // 引用回复
        is QuoteReply -> QuoteCatCodeSerializer.encode(this)

        // 转发消息
        is ForwardMessage -> {
            CatCodeUtil.getNekoBuilder("forward", true)
                .key("title").value(title)
                .key("brief").value(brief)
                .key("source").value(source)
                .key("summary").value(summary)
                .build()
        }

        is MusicShare -> MusicShareCatCodeSerializer.encode(this)

        // 富文本，xml或json
        is RichMessage -> RichCatCodeSerializer.encode(this)

        // mirai不支持的消息
        is UnsupportedMessage -> UnsupportedCatCodeSerializer.encode(this)

        // else.
        else -> {
            CatCodeUtil.getNekoBuilder("other", true)
                .key("code").value(this.toString()).build()
        }
    }

}
