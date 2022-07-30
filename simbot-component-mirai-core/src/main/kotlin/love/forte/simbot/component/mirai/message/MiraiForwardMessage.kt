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
import love.forte.simbot.bot.Bot
import love.forte.simbot.component.mirai.message.MiraiForwardMessage.Node.Companion.asSimbot
import love.forte.simbot.definition.OrganizationBot
import love.forte.simbot.message.Message
import love.forte.simbot.message.Messages
import love.forte.simbot.message.doSafeCast
import love.forte.simbot.message.toMessages
import love.forte.simbot.tryToLong
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.*


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
         * 发送人 [User.id]。 在mirai中，id相关的属性必然为 [Long] 类型。
         *
         * @see ForwardMessage.Node.senderId
         */
        public val senderId: LongID
        
        /**
         * 消息发送的时间戳。
         *
         * @see ForwardMessage.Node.time
         */
        public val time: Timestamp
        
        /**
         * 发送人昵称
         *
         * @see ForwardMessage.Node.senderName
         */
        public val senderName: String get() = originalNode.senderName
        
        /**
         * 消息内容.
         *
         * 来源于 [ForwardMessage.Node.messageChain].
         *
         */
        public val messages: Messages
        
        
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
private data class MiraiForwardMessageImpl(override val originalForwardMessage: ForwardMessage) : MiraiForwardMessage {
    @Transient
    @kotlin.jvm.Transient
    override val nodeList: List<MiraiForwardMessage.Node> = originalForwardMessage.nodeList.map { it.asSimbot() }
    
}


@SerialName("mirai.forwardNode")
@Serializable
private data class MiraiForwardMessageNodeImpl(override val originalNode: ForwardMessage.Node) :
    MiraiForwardMessage.Node {
    @Transient
    @kotlin.jvm.Transient
    private val messageContent = MiraiMessageChainContent(originalNode.messageChain)
    
    @Transient
    @kotlin.jvm.Transient
    override val senderId: LongID = originalNode.senderId.ID
    
    @Transient
    @kotlin.jvm.Transient
    override val time: Timestamp = Timestamp.bySecond(originalNode.time.toLong())
    
    override val messages: Messages get() = messageContent.messages
}


/**
 * 仅可用于发送的 "转发消息" 包装。
 *
 * [MiraiSendOnlyForwardMessage] 不会真正生成 [ForwardMessage],
 * 而是仅作为一个 _预处理_ 类型存在，只有当进行真正发送的时候才会构建
 * [ForwardMessage].
 *
 * 当通过 [ForwardMessageBuilder] 构建转发消息的时候是需要提供 [Contact]
 * 对象的，[MiraiSendOnlyForwardMessage] 作为预处理类型则不会需要此参数。
 *
 * 通过 [MiraiForwardMessageBuilder] 构建。
 *
 * ## 不可序列化
 *
 *
 * @see MiraiForwardMessageBuilder
 *
 */
public class MiraiSendOnlyForwardMessage private constructor(
    private val displayStrategy: ForwardMessage.DisplayStrategy,
    private val nodes: List<ComputableForwardMessageNode>,
) :
    MiraiSendOnlyComputableMessage<MiraiSendOnlyForwardMessage> {
    
    override val key: Message.Key<MiraiSendOnlyForwardMessage>
        get() = TODO("Not yet implemented")
    
    override fun equals(other: Any?): Boolean {
        TODO("Not yet implemented")
    }
    
    override fun hashCode(): Int {
        return super.hashCode()
    }
    
    override fun toString(): String {
        TODO("Not yet implemented")
    }
    
    override suspend fun originalMiraiMessage(contact: Contact): net.mamoe.mirai.message.data.Message {
        val miraiNodes = nodes.map { node -> node.toNode(contact) }
        return RawForwardMessage(miraiNodes).render(displayStrategy)
    }
}


@Serializable
private sealed interface ComputableForwardMessageNode {
    suspend fun toNode(contact: Contact): ForwardMessage.Node
}

@SerialName("DelegatedComputableForwardMessageNode")
@Serializable
private data class DelegatedComputableForwardMessageNode(private val node: ForwardMessage.Node) :
    ComputableForwardMessageNode {
    override suspend fun toNode(contact: Contact): ForwardMessage.Node {
        return node
    }
}

@SerialName("SampleComputableForwardMessageNode")
@Serializable
private data class SampleComputableForwardMessageNode(
    private val senderId: Long,
    private val time: Int,
    private val senderName: String,
    private val messages: Messages,
) : ComputableForwardMessageNode {
    override suspend fun toNode(contact: Contact): ForwardMessage.Node {
        val msg = messages.toOriginalMiraiMessage(contact)
        val chain = if (msg !is MessageChain) msg.toMessageChain() else msg
        return ForwardMessage.Node(senderId, time, senderName, chain)
    }
}


/**
 *
 * 用于构建 [MiraiForwardMessage] 的构建器。
 *
 * 类似于 [ForwardMessageBuilder], 但是**有所简化**，且使用的为 simbot 中的实体对象。
 *
 * [MiraiForwardMessageBuilder] 屏蔽了构建 [ForwardMessageBuilder] 时所需要的 [Contact] 参数，
 * 但取而代之的，当你想要向转发消息中添加消息时，将不能直接使用 [Bot] 来拼接消息，而应该使用 [OrganizationBot]
 * 或其下其他衍生类型。
 *
 *
 * @see ForwardMessageBuilder
 */
public class MiraiForwardMessageBuilder {
    private val container: MutableList<ComputableForwardMessageNode> = mutableListOf()
    private fun append(node: ForwardMessage.Node) {
        container.add(DelegatedComputableForwardMessageNode(node))
    }
    
    private fun append(senderId: Long, senderName: String, time: Int, messages: Messages) {
        container.add(SampleComputableForwardMessageNode(senderId, time, senderName, messages))
    }
    
    /**
     * mirai中合并转发卡片展示策略.
     *
     * @see ForwardMessage.DisplayStrategy
     */
    public var displayStrategy: ForwardMessage.DisplayStrategy = ForwardMessage.DisplayStrategy
    
    
    /**
     * 当前时间.
     * 构建node时若不指定时间, 则会使用 [currentTime] 自增 1 的时间.
     *
     * 类似于 [ForwardMessageBuilder.currentTime]。
     *
     */
    public var currentTime: Int = (System.currentTimeMillis() / 1000).toInt()
    
    
    /**
     * 直接添加一个 [ForwardMessage.Node].
     */
    public fun add(node: ForwardMessage.Node): MiraiForwardMessageBuilder = apply {
        append(node)
    }
    
    /**
     * 直接添加一个 [MiraiForwardMessage.Node].
     */
    public fun add(node: MiraiForwardMessage.Node): MiraiForwardMessageBuilder = apply {
        append(node.originalNode)
    }
    
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID. 必须保证其结果可以转化为 [Long], 比如提供一个 [LongID].
     * @param senderName 发送者名称
     */
    public fun add(senderId: ID, senderName: String, time: Timestamp, message: Message): MiraiForwardMessageBuilder =
        apply {
            append(
                senderId.tryToLong(), senderName, time.second.toInt(), when (message) {
                    is Message.Element<*> -> message.toMessages()
                    is Messages -> message
                }
            )
        }
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID
     * @param senderName 发送者名称
     */
    public fun add(senderId: Long, senderName: String, time: Timestamp, message: Message): MiraiForwardMessageBuilder =
        apply {
            append(
                senderId, senderName, time.second.toInt(), when (message) {
                    is Message.Element<*> -> message.toMessages()
                    is Messages -> message
                }
            )
        }
    
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID. 必须保证其结果可以转化为 [Long], 比如提供一个 [LongID].
     * @param senderName 发送者名称
     */
    public fun add(senderId: ID, senderName: String, time: Int, message: Message): MiraiForwardMessageBuilder =
        apply {
            append(
                senderId.tryToLong(), senderName, time, when (message) {
                    is Message.Element<*> -> message.toMessages()
                    is Messages -> message
                }
            )
        }
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID
     * @param senderName 发送者名称
     */
    public fun add(senderId: Long, senderName: String, time: Int, message: Message): MiraiForwardMessageBuilder =
        apply {
            append(
                senderId, senderName, time, when (message) {
                    is Message.Element<*> -> message.toMessages()
                    is Messages -> message
                }
            )
        }
    
    
    public fun build(): MiraiSendOnlyForwardMessage {
        
        TODO()
    }
    
}


/*
        public val senderId: LongID
        public val time: Timestamp
        public val senderName: String get() = originalNode.senderName
        public val messageContent: MessageContent
 */


