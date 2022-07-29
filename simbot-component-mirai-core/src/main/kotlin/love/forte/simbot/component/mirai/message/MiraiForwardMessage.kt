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
import kotlinx.serialization.Transient
import love.forte.simbot.ID
import love.forte.simbot.LongID
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.message.MiraiForwardMessage.Node.Companion.asSimbot
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.ForwardMessageBuilder


/**
 * 对一个 [ForwardMessage] 的直接包装, 并提供此类型的属性代理。
 */
public interface MiraiForwardMessage : OriginalMiraiDirectlySimbotMessage<MiraiForwardMessage> {
    
    /**
     * 得到当前消息中包装的 [ForwardMessage] 类型。
     */
    public val originalForwardMessage: ForwardMessage
    
    // region ForwardMessage渲染后属性
    /**
     * @see ForwardMessage.preview
     */
    public val preview: List<String> get() = originalForwardMessage.preview
    
    /**
     * @see ForwardMessage.title
     */
    public val title: String get() = originalForwardMessage.title
    
    /**
     * @see ForwardMessage.brief
     */
    public val brief: String get() = originalForwardMessage.brief
    
    /**
     * @see ForwardMessage.source
     */
    public val source: String get() = originalForwardMessage.source
    
    /**
     * @see ForwardMessage.summary
     */
    public val summary: String get() = originalForwardMessage.summary
    // endregion
    
    /**
     * 当前转发消息中的消息节点列表。
     */
    public val nodeList: List<Node>
    
    override val originalMiraiMessage: ForwardMessage
        get() = originalForwardMessage
    
    override val key: Message.Key<MiraiForwardMessage>
        get() = Key
    
    public companion object Key : Message.Key<MiraiForwardMessage> {
        override fun safeCast(value: Any): MiraiForwardMessage? = doSafeCast(value)
    
        /**
         * 通过一个 [ForwardMessage] 对象构建一个在simbot中流通的 [MiraiForwardMessage] 对象实例。
         *
         * **Kotlin**
         *
         * ```kotlin
         * val miraiForward = forward.asSimbot()
         * ```
         *
         * **Java**
         *
         * ```java
         * MiraiForwardMessage miraiForward = MiraiForwardMessage.of(forward);
         * ```
         */
        @JvmStatic
        @JvmName("of")
        public fun ForwardMessage.asSimbot(): MiraiForwardMessage = MiraiForwardMessageImpl(this)
        
    }
    
    
    /**
     * mirai转发消息中的各消息节点。
     *
     * 与 [ForwardMessage.Node] 基本类似，区别在于 [MiraiForwardMessage.Node]
     * 中会将 [ForwardMessage.Node.messageChain] 等信息转化为在simbot中流通的类型，
     * 并保留对原始 [ForwardMessage.Node] 的获取。
     *
     */
    public interface Node {
        /**
         * 得到此节点对应包装的原始节点类型。
         */
        public val originalNode: ForwardMessage.Node
        
        /**
         * 发送人 [User.id]
         *
         * @see ForwardMessage.Node.senderId
         */
        public val senderId: LongID
        
        /**
         * 时间戳 秒
         *
         * @see ForwardMessage.Node.time
         */
        public val time: Timestamp
        
        /**
         * 发送人昵称
         *
         * @see ForwardMessage.Node.senderName
         */
        public val senderName: String
        
        /**
         * 消息内容.
         *
         * 来源于 [ForwardMessage.Node.messageChain].
         *
         */
        public val messageContent: MessageContent
        
        
        public companion object {
            
            /**
             * 将一个 [ForwardMessage.Node] 转化为simbot的 [Node] 类型。
             *
             * **Java**
             *
             * ```java
             * MiraiForwardMessage.Node node = MiraiForwardMessage.Node.of(originalNode);
             * ```
             *
             * **Kotlin**
             *
             * ```kotlin
             * val node = originalNode.asSimbot()
             * ```
             *
             *
             */
            @JvmStatic
            @JvmName("of")
            public fun ForwardMessage.Node.asSimbot(): Node = MiraiForwardMessageNodeImpl(this)
            
        }
        
    }
}


@SerialName("mirai.forward")
@Serializable
internal data class MiraiForwardMessageImpl(override val originalForwardMessage: ForwardMessage) : MiraiForwardMessage {
    @Transient
    @kotlin.jvm.Transient
    override val nodeList: List<MiraiForwardMessage.Node> = originalForwardMessage.nodeList.map { it.asSimbot() }
    
}


@SerialName("mirai.forwardNode")
@Serializable
internal data class MiraiForwardMessageNodeImpl(override val originalNode: ForwardMessage.Node) :
    MiraiForwardMessage.Node {
    override val senderId: LongID = originalNode.senderId.ID
    override val time: Timestamp = Timestamp.bySecond(originalNode.time.toLong())
    override val senderName: String = originalNode.senderName
    
    
    @Transient
    @kotlin.jvm.Transient
    override val messageContent: MessageContent = MiraiMessageChainContent(originalNode.messageChain)
}


/**
 *
 * @see ForwardMessageBuilder
 */
public class MiraiForwardMessageBuilder // TODO
