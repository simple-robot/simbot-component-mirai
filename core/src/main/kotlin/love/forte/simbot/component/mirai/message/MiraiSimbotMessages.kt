package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.Component
import love.forte.simbot.component.mirai.ComponentMirai
import love.forte.simbot.message.Message
import net.mamoe.mirai.contact.Contact
import kotlin.reflect.KClass


/**
 * 所有在mirai组件中使用的Message所使用的消息标记。
 *
 * 如果对于一个消息实现希望使其可以进行发送，
 * 应当实现 [MiraiNativeComputableSimbotMessage] 接口并提供 [MiraiNativeComputableSimbotMessage.nativeMiraiMessage]
 * 计算逻辑。
 *
 * @see MiraiSendOnlySimbotMessage
 * @see MiraiNativeComputableSimbotMessage
 */
public sealed interface MiraiSimbotMessage<E : MiraiSimbotMessage<E>> : Message.Element<E>


/**
 * 标记性质的接口。
 * 标记一个类型为 **仅** 用于发送使用的 [Message.Element]. 这类消息将 **不保证** 能够序列化。
 *
 * @see MiraiShare
 * @see MiraiMusicShare
 * @see MiraiSendOnlyImage
 * @see MiraiSimbotMessage
 * @see SimpleMiraiSendOnlyComputableSimbotMessage
 */
public interface MiraiSendOnlySimbotMessage<E : MiraiSendOnlySimbotMessage<E>> : MiraiSimbotMessage<E>


/**
 *
 * mirai与simbot相互转化的用于发送的 [Message.Element]. 可计算的 message 通常可用于发送与接收。当作为接收消息使用的时候，
 * 必须保证其可以进行序列化。
 *
 * 对于不需要计算的消息以及接收到的没有特殊对应实现的消息，通常都会使用 [SimbotNativeMiraiMessage] 进行直接包装。
 *
 * @see MiraiSimbotMessage
 * @see SimbotNativeMiraiMessage
 */
public interface MiraiNativeComputableSimbotMessage<E : MiraiNativeComputableSimbotMessage<E>> : MiraiSimbotMessage<E> {
    public suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage
}

/**
 * 实现 [MiraiNativeComputableSimbotMessage], 并提供一个可以直接获取的属性 [nativeMiraiMessage] 来代替需要使用 [Contact] 来计算获取的函数。
 * 此类型代表一些不需要计算便可直接获取到 [NativeMiraiMessage] 实例的消息类型。
 *
 * @see MiraiShare
 * @see MiraiMusicShare
 * @see SimbotNativeMiraiMessage
 * @see MiraiNativeComputableSimbotMessage
 */
public interface MiraiNativeDirectlySimbotMessage<E : MiraiNativeComputableSimbotMessage<E>> :
    MiraiNativeComputableSimbotMessage<E> {
    /**
     * 不需要通过 [Contact] 计算 [nativeMiraiMessage] 而直接获取 [NativeMiraiMessage] 对象。
     */
    public val nativeMiraiMessage: NativeMiraiMessage


    override suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage {
        return nativeMiraiMessage
    }
}

/**
 *
 * [MiraiSendOnlySimbotMessage] 与 [MiraiNativeComputableSimbotMessage] 的整合性接口,
 * 代表了一个可以发送且仅能用于发送的 [MiraiSimbotMessage] 类型。
 * @see MiraiSendOnlySimbotMessage
 * @see MiraiNativeComputableSimbotMessage
 */
public interface MiraiSendOnlyComputableSimbotMessage<E : MiraiSendOnlyComputableSimbotMessage<E>> :
    MiraiSendOnlySimbotMessage<E>, MiraiNativeComputableSimbotMessage<E>


/**
 * 直接将一个 [NativeMiraiSingleMessage] 作为 [Message] 使用，将会忽略掉 [MiraiNativeComputableSimbotMessage.nativeMiraiMessage] 的参数 [Contact].
 * [NativeMiraiSingleMessage] -> [Message].
 *
 * 所有未提供特殊实现的mirai消息对象都会使用此类型进行包装。
 */
@SerialName("mirai.nativeMessage")
@Serializable
public class SimbotNativeMiraiMessage(
    override val nativeMiraiMessage: NativeMiraiSingleMessage,
) : MiraiNativeDirectlySimbotMessage<SimbotNativeMiraiMessage> {
    override val key: Message.Key<SimbotNativeMiraiMessage> get() = Key

    override fun equals(other: Any?): Boolean {
        if (other !is SimbotNativeMiraiMessage) return false
        return other.nativeMiraiMessage == this.nativeMiraiMessage
    }

    override fun toString(): String = nativeMiraiMessage.toString()
    override fun hashCode(): Int = nativeMiraiMessage.hashCode()

    public companion object Key : Message.Key<SimbotNativeMiraiMessage> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<SimbotNativeMiraiMessage>
            get() = SimbotNativeMiraiMessage::class
    }
}


/**
 * 通过函数 (suspend ([Contact]) -> [NativeMiraiMessage]) 得到一个 **仅用于发送** 的 [Message].
 *
 * [SimpleMiraiSendOnlyComputableSimbotMessage] **不可序列化**，仅用于在发送的时候使用任意的 [NativeMiraiMessage] 作为 [Message] 进行发送。
 *
 * [SimpleMiraiSendOnlyComputableSimbotMessage] 通过一个挂起函数 `suspend (Contact) -> NativeMiraiMessage`
 * 接收一个 [Contact] 来根据当前发送消息的目标来获取一个消息实例。
 *
 * 如果你需要发送的消息能够忽略 [Contact] 并直接提供一个 [NativeMiraiMessage], 并且你希望此消息能够序列化
 * （首先需要保证提供的 [NativeMiraiMessage] 能够序列化), 那么考虑使用 [SimbotNativeMiraiMessage].
 *
 * @see SimbotNativeMiraiMessage
 * @see MiraiNativeComputableSimbotMessage
 */
public class SimpleMiraiSendOnlyComputableSimbotMessage(
    private val factory: suspend (Contact) -> NativeMiraiMessage,
) : MiraiSendOnlyComputableSimbotMessage<SimpleMiraiSendOnlyComputableSimbotMessage> {
    override val key: Message.Key<SimpleMiraiSendOnlyComputableSimbotMessage> get() = Key


    override suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage {
        return factory(contact)
    }


    override fun equals(other: Any?): Boolean {
        if (other !is SimpleMiraiSendOnlyComputableSimbotMessage) return false
        return other === this
    }

    override fun toString(): String = "SimbotSendOnlyMiraiMessage($factory)"
    override fun hashCode(): Int = factory.hashCode()


    public companion object Key : Message.Key<SimpleMiraiSendOnlyComputableSimbotMessage> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<SimpleMiraiSendOnlyComputableSimbotMessage>
            get() = SimpleMiraiSendOnlyComputableSimbotMessage::class
    }
}


