/*
 *  Copyright (c) 2022-2023 ForteScarlet.
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
