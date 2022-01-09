package love.forte.simbot.component.mirai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.Component
import love.forte.simbot.message.Message
import net.mamoe.mirai.contact.Contact
import kotlin.reflect.KClass

/**
 *
 *
 * @see SimbotSendOnlyComputableMiraiMessage
 */
public sealed class MiraiSimbotMessage<E : MiraiSimbotMessage<E>> : Message.Element<E> {
    public abstract suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage
}


/**
 * 直接将一个 [NativeMiraiMessage] 作为 [Message] 使用，将会忽略掉 [MiraiSimbotMessage.nativeMiraiMessage] 的参数 [Contact].
 * [NativeMiraiMessage] -> [Message]
 */
@SerialName("mirai.nativeMessage")
@Serializable
public class SimbotNativeMiraiMessage(
    public val nativeMiraiMessage: NativeMiraiMessage
) : MiraiSimbotMessage<SimbotNativeMiraiMessage>() {
    override val key: Message.Key<SimbotNativeMiraiMessage> get() = Key

    override suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage {
        return nativeMiraiMessage
    }

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
 * 通过函数 suspend ([Contact]) -> [NativeMiraiMessage] 得到一个 **仅用于发送** 的 [Message].
 *
 * [SimbotSendOnlyComputableMiraiMessage] **不可序列化**，仅用于在发送的时候使用任意的 [NativeMiraiMessage] 作为 [Message] 进行发送。
 *
 * [SimbotSendOnlyComputableMiraiMessage] 通过一个挂起函数 `suspend (Contact) -> NativeMiraiMessage`
 * 根据一个 [Contact] 来根据当前发送消息的目标来获取一个消息实例。
 *
 */
public class SimbotSendOnlyComputableMiraiMessage(
    private val factory: suspend (Contact) -> NativeMiraiMessage,
) : MiraiSimbotMessage<SimbotSendOnlyComputableMiraiMessage>() {
    override val key: Message.Key<SimbotSendOnlyComputableMiraiMessage> get() = Key


    override suspend fun nativeMiraiMessage(contact: Contact): NativeMiraiMessage {
        return factory(contact)
    }


    override fun equals(other: Any?): Boolean {
        if (other !is SimbotSendOnlyComputableMiraiMessage) return false
        return other === this
    }

    override fun toString(): String = "SimbotSendOnlyMiraiMessage($factory)"
    override fun hashCode(): Int = factory.hashCode()


    public companion object Key : Message.Key<SimbotSendOnlyComputableMiraiMessage> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<SimbotSendOnlyComputableMiraiMessage>
            get() = SimbotSendOnlyComputableMiraiMessage::class
    }
}
