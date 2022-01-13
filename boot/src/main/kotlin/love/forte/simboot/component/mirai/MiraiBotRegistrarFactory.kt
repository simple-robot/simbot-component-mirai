package love.forte.simboot.component.mirai

import love.forte.simboot.factory.BotRegistrarFactory
import love.forte.simbot.BotRegistrar
import love.forte.simbot.component.mirai.miraiBotManager
import love.forte.simbot.event.EventProcessor

/**
 * Mirai bot注册器工厂。
 * @author ForteScarlet
 */
public class MiraiBotRegistrarFactory : BotRegistrarFactory {
    override fun invoke(processor: EventProcessor): BotRegistrar {
        return miraiBotManager(processor)
    }
}