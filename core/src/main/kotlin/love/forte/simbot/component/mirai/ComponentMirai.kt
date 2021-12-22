package love.forte.simbot.component.mirai

import kotlinx.serialization.modules.SerializersModule
import love.forte.simbot.*


/**
 * Mirai在simbot下的组件.
 *
 */
public object ComponentMirai {
    @JvmField public val COMPONENT_ID: CharSequenceID = ComponentMiraiApi.COMPONENT_ID.ID

    @Suppress("ObjectPropertyName")
    internal lateinit var _component: Component

    public val component: Component
        get() {
            return if (!::_component.isInitialized) {
                return Components[COMPONENT_ID]
            } else _component
        }
}


/**
 * Mirai组件的 [Component] 注册器。
 */
public class MiraiComponentRegistrar : ComponentInformationRegistrar {
    override fun informations(): ComponentInformationRegistrar.Result {
        return ComponentInformationRegistrar.Result.ok(listOf(MiraiComponentInformation()))
    }
}

private class MiraiComponentInformation : ComponentInformation {
    override val id: ID
        get() = ComponentMirai.COMPONENT_ID

    override val name: String
        get() = id.toString()

    override fun configAttributes(attributes: MutableAttributeMap) {
        // attributes for component
    }

    override val messageSerializersModule: SerializersModule?
        get() = super.messageSerializersModule // TODO messages

    override fun setComponent(component: Component) {
        ComponentMirai._component = component
    }
}