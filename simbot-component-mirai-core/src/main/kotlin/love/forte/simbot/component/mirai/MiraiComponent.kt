/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
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

@file:JvmName("MiraiComponents")

package love.forte.simbot.component.mirai

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import love.forte.simbot.*
import love.forte.simbot.application.ApplicationBuilder
import love.forte.simbot.component.mirai.MiraiComponent.Factory
import love.forte.simbot.component.mirai.bot.MiraiBotManager
import love.forte.simbot.component.mirai.message.*
import love.forte.simbot.message.Message
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.utils.MiraiExperimentalApi


/**
 * simbot对应的mirai组件。
 *
 * ## 实例化
 *
 * [MiraiComponent] 允许直接通过构造函数构建，但是这种行为不被建议。
 * 除非你清楚这么做的意义与后果，否则你始终应当通过 [Factory] 向 [ApplicationBuilder]
 * 中注册，而非直接通过构造函数。
 *
 *
 * @see Factory
 */
public class MiraiComponent : Component {

    /**
     * 代表组件的唯一标识。
     */
    override val id: String get() = ID_VALUE

    /**
     * 得到 [MiraiComponent] 所使用的消息序列化信息。
     */
    override val componentSerializersModule: SerializersModule
        get() = messageSerializersModule

    override fun toString(): String = TO_STRING_VALUE

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other !is MiraiComponent -> false
            else -> id == other.id
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    /**
     * 用于构建 [MiraiComponent] 的 [ComponentFactory] 实现。
     *
     * 当处于 [ApplicationBuilder] 中时，你可以通过扩展函数 [useMiraiComponent] 来注册当前组件,
     * 可以通过 [miraiBots] 来注册 [MiraiBotManager], 或者通过 [useMirai] 来注册二者。
     *
     * @see useMirai
     * @see useMiraiComponent
     * @see miraiBots
     */
    public companion object Factory : ComponentFactory<MiraiComponent, MiraiComponentConfiguration> {
        /**
         * [MiraiComponent] 的组件标识ID。
         */
        @Suppress("MemberVisibilityCanBePrivate")
        public const val ID_VALUE: String = "simbot.mirai"

        internal const val TO_STRING_VALUE: String = "MiraiComponent(id=$ID_VALUE)"

        /**
         * [Factory.ID_VALUE] 的ID实例。
         */
        @JvmField
        @Deprecated("Unused")
        public val componentID: ID = ID_VALUE.ID

        /**
         * 当前组件工厂的注册标识。
         */
        override val key: Attribute<MiraiComponent> = attribute(ID_VALUE)

        /**
         * 根据配置函数构建 [MiraiComponent].
         *
         */
        override suspend fun create(configurator: MiraiComponentConfiguration.() -> Unit): MiraiComponent {
            MiraiComponentConfiguration.configurator()
            return MiraiComponent()
        }

        /**
         * 当前组件中所提供的所有额外消息类型的序列化模块。
         *
         * [componentSpecialMessageSerializersModule] **只**包含组件内的消息序列化模块，而不包含mirai提供的消息序列化模块。
         * 如果想要保证序列化不会出现问题，你可能需要使用 [messageSerializersModule].
         *
         * @see messageSerializersModule
         */
        @Suppress("MemberVisibilityCanBePrivate")
        @JvmStatic
        public val componentSpecialMessageSerializersModule: SerializersModule = SerializersModule {
            polymorphic(Message.Element::class) {
                subclass(SimbotOriginalMiraiMessage.serializer())

                //region image
                polymorphic(MiraiImage::class) {
                    subclass(MiraiImageImpl.serializer())
                }
                subclass(MiraiImageImpl.serializer())
                //endregion

                //region audio
                polymorphic(MiraiAudio::class) {
                    subclass(MiraiAudioImpl.serializer())
                }
                subclass(MiraiAudioImpl.serializer())
                //endregion

                //region forward message
                polymorphic(MiraiForwardMessage::class) {
                    subclass(MiraiForwardMessageImpl.serializer())
                }
                subclass(MiraiForwardMessageImpl.serializer())

                polymorphic(MiraiForwardMessage.Node::class) {
                    subclass(MiraiForwardMessageNodeImpl.serializer())
                }
                //endregion

                @OptIn(MiraiExperimentalApi::class) subclass(MiraiShare.serializer())
                subclass(MiraiQuoteReply.serializer())
                subclass(MiraiMusicShare.serializer())
                subclass(MiraiNudge.serializer())
                subclass(MiraiReceivedNudge.serializer())
            }
        }

        /**
         * 得到 [MiraiComponent] 所使用的消息序列化信息。
         *
         * [messageSerializersModule] 会集成mirai中所提供的消息序列化模块 [MessageSerializers.serializersModule]
         * 与当前组件中所提供的额外的消息序列化模块 [componentSpecialMessageSerializersModule].
         *
         * 如果你需要使用能支持组件消息实例的进行序列化的序列化模块，[messageSerializersModule] 是更好的选择。
         *
         * [messageSerializersModule] 同时也是在 [MiraiComponent] 中对外提供的序列化模块。
         *
         */
        @JvmStatic
        public val messageSerializersModule: SerializersModule =
            MessageSerializers.serializersModule + componentSpecialMessageSerializersModule
    }
}


/**
 * [MiraiComponent] 注册的时候所使用的配置类。
 *
 * 目前的 [MiraiComponent] 暂无可配置内容，因此 [MiraiComponentConfiguration] 没有任何可配置属性。
 *
 */
public object MiraiComponentConfiguration


/**
 * 支持进行自动加载的组件配置工厂。
 *
 * @see ComponentAutoRegistrarFactory
 */
public class MiraiComponentAutoRegistrarFactory :
    ComponentAutoRegistrarFactory<MiraiComponent, MiraiComponentConfiguration> {
    override val registrar: Factory
        get() = MiraiComponent
}

