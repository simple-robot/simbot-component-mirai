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
 *
 */

package love.forte.simbot.component.mirai

import kotlinx.serialization.modules.*
import love.forte.simbot.*
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.message.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.*


/**
 * simbot对应的mirai组件。
 */
public class MiraiComponent : Component {
    /**
     * 代表组件的唯一标识。
     */
    override val id: ID get() = ComponentID

    /**
     * 得到 [MiraiComponent] 所使用的消息序列化信息。
     */
    override val componentSerializersModule: SerializersModule
        get() = messageSerializersModule

    public companion object Registrar : ComponentRegistrar<MiraiComponent, MiraiComponentConfiguration> {
        /**
         * [MiraiComponent] 的组件标识ID。
         */
        @Suppress("MemberVisibilityCanBePrivate")
        public const val ID: String = "simbot.mirai"
        internal val ComponentID: ID = ID.ID

        override val key: Attribute<MiraiComponent> = attribute(ID)

        override fun register(block: MiraiComponentConfiguration.() -> Unit): MiraiComponent {
            // nothing now
            // val config = MiraiComponentConfiguration().also(block)

            return MiraiComponent()
        }

        /**
         * 得到 [MiraiComponent] 所使用的消息序列化信息。
         */
        @JvmStatic
        public val messageSerializersModule: SerializersModule =
            MessageSerializers.serializersModule +
                    SerializersModule {
                        polymorphic(Message.Element::class) {
                            subclass(SimbotOriginalMiraiMessage.serializer())

                            ////

                            polymorphic(MiraiImage::class) {
                                subclass(MiraiImageImpl.serializer())
                            }
                            subclass(MiraiImageImpl.serializer())

                            ////

                            polymorphic(MiraiAudio::class) {
                                subclass(MiraiAudioImpl.serializer())
                            }
                            subclass(MiraiAudioImpl.serializer())

                            ////
                            @OptIn(MiraiExperimentalApi::class)
                            subclass(MiraiShare.serializer())
                            subclass(MiraiQuoteReply.serializer())
                            subclass(MiraiMusicShare.serializer())
                            subclass(MiraiNudge.serializer())
                        }
                    }
    }
}


/**
 * [MiraiComponent] 注册的时候所使用的配置类。
 */
public class MiraiComponentConfiguration {

}


/**
 * 支持进行自动加载的组件配置工厂。
 */
public class MiraiComponentRegistrarFactory : ComponentRegistrarFactory<MiraiComponent, MiraiComponentConfiguration> {
    override val registrar: ComponentRegistrar<MiraiComponent, MiraiComponentConfiguration>
        get() = MiraiComponent
}




/**
 * Mirai在simbot下的组件信息.
 *
 */
@Deprecated("Use 'MiraiComponent'")
public object ComponentMirai {
    /**
     * Mirai组件的ID标识。
     */
    @JvmField
    public val COMPONENT_ID: CharSequenceID = MiraiComponent.ComponentID.toCharSequenceID()

    /**
     * Mirai组件的 [组件][Component] 实例。当simbot核心加载后初始化。
     */
    @JvmStatic
    public val component: Component
        get() {
            throw UnsupportedOperationException("See love.forte.simbot.component.mirai.MiraiComponent")
        }
}

//region manager获取扩展
/**
 * 通过 [OriginBotManager] 获取所有的 [MiraiBotManager]
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun miraiComponents(): List<MiraiBotManager> = OriginBotManager.filterIsInstance<MiraiBotManager>()


/**
 * 获取其中为 [MiraiBotManager] 的管理器。
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Iterable<BotManager<*>>.filterIsMiraiBotManagers(): List<MiraiBotManager> =
    filterIsInstance<MiraiBotManager>()


/**
 * 过滤获取其中为 [MiraiBotManager] 的管理器。
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun Sequence<BotManager<*>>.filterIsMiraiBotManagers(): Sequence<MiraiBotManager> =
    filterIsInstance<MiraiBotManager>()
//endregion
