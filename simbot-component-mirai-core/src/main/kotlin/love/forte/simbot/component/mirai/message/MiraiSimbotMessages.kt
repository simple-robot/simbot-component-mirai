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
 */

package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.simbot.message.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message as OriginalMiraiMessage
import net.mamoe.mirai.message.data.SingleMessage as OriginalMiraiSingleMessage


/**
 * 所有在mirai组件中使用的Message所使用的消息标记。
 *
 * 如果对于一个消息实现希望使其可以进行发送，
 * 应当实现 [OriginalMiraiComputableSimbotMessage] 接口并提供 [OriginalMiraiComputableSimbotMessage.originalMiraiMessage]
 * 计算逻辑。
 *
 * @see MiraiSendOnlySimbotMessage
 * @see OriginalMiraiComputableSimbotMessage
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
 * @see SimpleMiraiSendOnlyComputableMessage
 */
public interface MiraiSendOnlySimbotMessage<E : MiraiSendOnlySimbotMessage<E>> : MiraiSimbotMessage<E>


/**
 *
 * mirai与simbot相互转化的用于发送的 [Message.Element]. 可计算的 message 通常可用于发送与接收。当作为接收消息使用的时候，
 * 必须保证其可以进行序列化。
 *
 * 对于不需要计算的消息以及接收到的没有特殊对应实现的消息，通常都会使用 [SimbotOriginalMiraiMessage] 进行直接包装。
 *
 * @see MiraiSimbotMessage
 * @see SimbotOriginalMiraiMessage
 */
public interface OriginalMiraiComputableSimbotMessage<E : OriginalMiraiComputableSimbotMessage<E>> :
    MiraiSimbotMessage<E> {
    
    /**
     * 通过一个 [Contact] 计算得到一个具体的 [OriginalMiraiMessage]。
     *
     * @param isDropAction 当实现者的 [OriginalMiraiMessage] 不会返回一个具体的消息对象，而是进行一个操作后返回占位对象 [EmptySingleMessage] 的话，
     * 则 [isDropAction] 代表为要求此消息不执行相应操作。例如对于 [MiraiNudge] 消息，其发送时不会得到真的"戳一戳消息"对象，而是**直接**发送戳一戳，
     * 并返回 [EmptySingleMessage]。当 [isDropAction] 为true时，代表禁止其过程中发生的"直接发送戳一戳"的行为。
     *
     */
    @JvmBlocking(baseName = "getOriginalMiraiMessage", suffix = "")
    @JvmAsync(baseName = "getOriginalMiraiMessage")
    public suspend fun originalMiraiMessage(contact: Contact, isDropAction: Boolean): OriginalMiraiMessage
    
    /**
     * 通过一个 [Contact] 计算得到一个具体的 [OriginalMiraiMessage]。
     *
     */
    @JvmBlocking(baseName = "getOriginalMiraiMessage", suffix = "")
    @JvmAsync(baseName = "getOriginalMiraiMessage")
    public suspend fun originalMiraiMessage(contact: Contact): OriginalMiraiMessage =
        originalMiraiMessage(contact = contact, isDropAction = false)
    
}

/**
 * 实现 [OriginalMiraiComputableSimbotMessage], 并提供一个可以直接获取的属性 [originalMiraiMessage] 来代替需要使用 [Contact] 来计算获取的函数。
 * 此类型代表一些不需要计算便可直接获取到 [OriginalMiraiMessage] 实例的消息类型。
 *
 * @see MiraiShare
 * @see MiraiMusicShare
 * @see SimbotOriginalMiraiMessage
 * @see OriginalMiraiComputableSimbotMessage
 */
public interface OriginalMiraiDirectlySimbotMessage<out M : OriginalMiraiMessage, E : OriginalMiraiComputableSimbotMessage<E>> :
    OriginalMiraiComputableSimbotMessage<E> {
    /**
     * 不需要通过 [Contact] 计算 [originalMiraiMessage] 而直接获取 [OriginalMiraiMessage] 对象。
     */
    public val originalMiraiMessage: M
    
    /**
     * 直接得到 [originalMiraiMessage], 不会使用 [contact] 和 [isDropAction]。
     *
     */
    @JvmBlocking(baseName = "getOriginalMiraiMessage", suffix = "")
    @JvmAsync(baseName = "getOriginalMiraiMessage")
    override suspend fun originalMiraiMessage(contact: Contact, isDropAction: Boolean): M = originalMiraiMessage
    
    
    /**
     * 直接得到 [originalMiraiMessage], 不会使用 [contact]。
     *
     */
    @JvmBlocking(baseName = "getOriginalMiraiMessage", suffix = "")
    @JvmAsync(baseName = "getOriginalMiraiMessage")
    override suspend fun originalMiraiMessage(contact: Contact): M = originalMiraiMessage
}

/**
 *
 * [MiraiSendOnlySimbotMessage] 与 [OriginalMiraiComputableSimbotMessage] 的整合性接口,
 * 代表了一个可以发送且仅能用于发送的 [MiraiSimbotMessage] 类型。
 * @see MiraiSendOnlySimbotMessage
 * @see OriginalMiraiComputableSimbotMessage
 */
public interface MiraiSendOnlyComputableMessage<E : MiraiSendOnlyComputableMessage<E>> :
    MiraiSendOnlySimbotMessage<E>, OriginalMiraiComputableSimbotMessage<E>


/**
 * 直接将一个 [OriginalMiraiSingleMessage] 作为 [Message] 使用，将会忽略掉 [OriginalMiraiComputableSimbotMessage.originalMiraiMessage] 的参数 [Contact].
 * [OriginalMiraiSingleMessage] -> [Message].
 *
 * 所有未提供特殊实现的mirai消息对象都会使用此类型进行包装。
 */
@SerialName("mirai.nativeMessage")
@Serializable
public class SimbotOriginalMiraiMessage(
    override val originalMiraiMessage: OriginalMiraiSingleMessage,
) : OriginalMiraiDirectlySimbotMessage<OriginalMiraiSingleMessage, SimbotOriginalMiraiMessage> {
    override val key: Message.Key<SimbotOriginalMiraiMessage> get() = Key
    
    
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is SimbotOriginalMiraiMessage) return false
        return other.originalMiraiMessage == this.originalMiraiMessage
    }
    
    override fun toString(): String = originalMiraiMessage.toString()
    override fun hashCode(): Int = originalMiraiMessage.hashCode()
    
    public companion object Key : Message.Key<SimbotOriginalMiraiMessage> {
        override fun safeCast(value: Any): SimbotOriginalMiraiMessage? = doSafeCast(value)
    }
}


/**
 * 通过函数 (suspend ([Contact]) -> [OriginalMiraiMessage]) 得到一个 **仅用于发送** 的 [Message].
 *
 * [SimpleMiraiSendOnlyComputableMessage] **不可序列化**，仅用于在发送的时候使用任意的 [OriginalMiraiMessage] 作为 [Message] 进行发送。
 *
 * [SimpleMiraiSendOnlyComputableMessage] 通过一个挂起函数 `suspend (Contact) -> NativeMiraiMessage`
 * 接收一个 [Contact] 来根据当前发送消息的目标来获取一个消息实例。
 *
 * 如果你需要发送的消息能够忽略 [Contact] 并直接提供一个 [OriginalMiraiMessage], 并且你希望此消息能够序列化
 * （首先需要保证提供的 [OriginalMiraiMessage] 能够序列化), 那么考虑使用 [SimbotOriginalMiraiMessage].
 *
 * @see SimbotOriginalMiraiMessage
 * @see OriginalMiraiComputableSimbotMessage
 */
public class SimpleMiraiSendOnlyComputableMessage private constructor(
    private val factory: suspend (Contact, Boolean) -> OriginalMiraiMessage,
) : MiraiSendOnlyComputableMessage<SimpleMiraiSendOnlyComputableMessage> {
    override val key: Message.Key<SimpleMiraiSendOnlyComputableMessage> get() = Key
    
    /**
     * 通过提供的 [factory] 计算并得到结果。
     */
    @JvmSynthetic
    override suspend fun originalMiraiMessage(contact: Contact, isDropAction: Boolean): OriginalMiraiMessage {
        return factory(contact, isDropAction)
    }
    
    
    override fun equals(other: Any?): Boolean {
        if (other !is SimpleMiraiSendOnlyComputableMessage) return false
        return other === this
    }
    
    override fun toString(): String = "SimbotSendOnlyMiraiMessage($factory)"
    override fun hashCode(): Int = factory.hashCode()
    
    
    public companion object Key : Message.Key<SimpleMiraiSendOnlyComputableMessage> {
        override fun safeCast(value: Any): SimpleMiraiSendOnlyComputableMessage? = doSafeCast(value)
        
        @JvmName("of")
        @JvmStatic
        public operator fun invoke(factory: suspend (Contact, Boolean) -> OriginalMiraiMessage): SimpleMiraiSendOnlyComputableMessage {
            return SimpleMiraiSendOnlyComputableMessage(factory)
        }
        
        @JvmName("of")
        @JvmStatic
        public operator fun invoke(factory: suspend (Contact) -> OriginalMiraiMessage): SimpleMiraiSendOnlyComputableMessage {
            return SimpleMiraiSendOnlyComputableMessage { c, _ ->
                factory(c)
            }
        }
    }
}



