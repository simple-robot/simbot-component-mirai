package love.forte.simbot.component.mirai

import com.google.auto.service.AutoService
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import love.forte.simbot.*
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.message.Message
import net.mamoe.mirai.message.MessageSerializers


/**
 * Mirai在simbot下的组件.
 *
 */
public object ComponentMirai {
    @JvmField
    public val COMPONENT_ID: CharSequenceID = ComponentMiraiApi.COMPONENT_ID.ID
    internal lateinit var componentValue: Component

    public val component: Component
        get() {
            return if (!::componentValue.isInitialized) {
                return Components[COMPONENT_ID]
            } else componentValue
        }
}


/**
 * Mirai组件的 [Component] 注册器。
 */
@AutoService(ComponentInformationRegistrar::class)
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

    override val messageSerializersModule: SerializersModule =
        MessageSerializers.serializersModule +
                SerializersModule {
                    polymorphic(Message.Element::class) {
                        subclass(SimbotNativeMiraiMessage.serializer())

                        polymorphic(MiraiImage::class) {
                            subclass(MiraiImageImpl.serializer())
                        }
                        subclass(MiraiImageImpl.serializer())

                        polymorphic(MiraiAudio::class) {
                            subclass(MiraiAudioImpl.serializer())
                        }
                        subclass(MiraiAudioImpl.serializer())
                    }
                }

    override fun setComponent(component: Component) {
        ComponentMirai.componentValue = component
    }
}