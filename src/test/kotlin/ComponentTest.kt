import love.forte.simbot.Components
import love.forte.simbot.component.mirai.MiraiComponent
import kotlin.test.Test

/**
 *
 * @author ForteScarlet
 */
class ComponentTest {

    @Test
    fun getComp() {
        Components.all.forEach {
            println(it)
        }
    }

}