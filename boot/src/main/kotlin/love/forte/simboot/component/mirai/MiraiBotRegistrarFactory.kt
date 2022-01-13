package love.forte.simboot.component.mirai

import love.forte.simboot.factory.BotRegistrarFactory
import love.forte.simbot.BotRegistrar
import love.forte.simbot.component.mirai.miraiBotManager
import love.forte.simbot.event.EventProcessor
import javax.inject.Named

/**
 * Mirai bot注册器工厂。
 * @author ForteScarlet
 */
@Named("miraiBotRegistrarFactory")
public class MiraiBotRegistrarFactory : BotRegistrarFactory {
    override fun invoke(processor: EventProcessor): BotRegistrar {
        return miraiBotManager(processor)
    }
}