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

import catcode.CatCodeUtil;
import catcode.Neko;
import love.forte.simbot.Identifies;
import love.forte.simbot.component.mirai.extra.catcode.CatCodeMessageUtil;
import love.forte.simbot.message.At;
import love.forte.simbot.message.Message;
import love.forte.simbot.message.MessageList;
import org.junit.jupiter.api.Test;

/**
 * @author ForteScarlet
 */
public class CatDecoder4Test {

    @Test
    public void decodeCatCodeTest() {
        final String cat =  "[CAT:at,code=123]";
        final Message message = CatCodeMessageUtil.catCodeToMessage(cat);
        assert message instanceof MessageList;

        final Message.Element<?> first = ((MessageList) message).get(0);
        assert first.equals(new At(Identifies.ID(123)));

    }
    @Test
    public void decodeNekoTest() {
        final Neko cat = CatCodeUtil.INSTANCE.getNekoTemplate().at(123);
        final Message.Element<?> message = CatCodeMessageUtil.toMessage(cat);
        assert message instanceof At;
        assert message.equals(new At(Identifies.ID(123)));
    }


}
