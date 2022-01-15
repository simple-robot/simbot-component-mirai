import love.forte.simbot.component.mirai.MiraiBot;
import love.forte.simbot.component.mirai.MiraiBotManager;
import love.forte.simbot.core.event.CoreListenerManager;
import love.forte.simbot.core.event.CoreListenerManagerConfiguration;
import net.mamoe.mirai.Bot;

/**
 * @author ForteScarlet
 */
public class MyTest {
    public static void main(String[] args) {
        final MiraiBotManager manager = MiraiBotManager.newInstance(CoreListenerManager.newInstance(new CoreListenerManagerConfiguration()));

        final MiraiBot miraiBot = manager.register(1, "1");

        final Bot nativeBot = miraiBot.getNativeBot();

    }
}
