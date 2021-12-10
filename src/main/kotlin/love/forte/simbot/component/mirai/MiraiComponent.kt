package love.forte.simbot.component.mirai

import love.forte.simbot.*
import love.forte.simbot.component.mirai.MiraiComponent.COMPONENT_ID

/**
 * Simbot下mirai组件的部分常量信息。
 */
public object MiraiComponent {
    public val COMPONENT_ID: CharSequenceID = "simbot.mirai".ID

    @Suppress("ObjectPropertyName")
    internal lateinit var _component: Component
    public val component: Component
        get() = if (!::_component.isInitialized) Components[COMPONENT_ID]
        else _component
}


/**
 * 组件信息注册器
 */
public class MiraiComponentInformationRegistrar : ComponentInformationRegistrar {
    override fun informations(): ComponentInformationRegistrar.Result {
        return ComponentInformationRegistrar.Result.ok(listOf(MiraiComponentInformation))
    }
}


private object MiraiComponentInformation : ComponentInformation {
    override val id: ID = COMPONENT_ID
    override val name: String = id.toString()

    override fun configAttributes(attributes: MutableAttributeMap) {
        // attr?


    }

    override fun setComponent(component: Component) {
        MiraiComponent._component = component
    }
}