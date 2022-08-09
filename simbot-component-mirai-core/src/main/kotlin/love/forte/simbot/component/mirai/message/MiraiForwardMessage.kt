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

package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import love.forte.simbot.*
import love.forte.simbot.bot.Bot
import love.forte.simbot.component.mirai.bot.MiraiBot
import love.forte.simbot.component.mirai.message.MiraiForwardMessage.Node.Companion.asSimbot
import love.forte.simbot.definition.Contact
import love.forte.simbot.definition.Member
import love.forte.simbot.definition.OrganizationBot
import love.forte.simbot.definition.User
import love.forte.simbot.event.ChatRoomMessageEvent
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.*
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.ForwardMessageDsl
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.util.concurrent.TimeUnit
import net.mamoe.mirai.contact.Contact as OriginalMiraiContact
import net.mamoe.mirai.contact.Group as OriginalMiraiGroup


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
internal data class MiraiForwardMessageImpl(override val originalForwardMessage: ForwardMessage) : MiraiForwardMessage {
    @Transient
    @kotlin.jvm.Transient
    override val nodeList: List<MiraiForwardMessage.Node> = originalForwardMessage.nodeList.map { it.asSimbot() }
    
}


@SerialName("mirai.forwardNode")
@Serializable
internal data class MiraiForwardMessageNodeImpl(override val originalNode: ForwardMessage.Node) :
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
 * 当通过 [ForwardMessageBuilder] 构建转发消息的时候是需要提供 [OriginalMiraiContact]
 * 对象的，[MiraiSendOnlyForwardMessage] 作为预处理类型则不会需要此参数。
 *
 * ## 构建
 *
 * 通过 [MiraiForwardMessageBuilder.build] 构建而得。
 *
 * ## 不可序列化
 *
 * [MiraiSendOnlyForwardMessage] **不支持序列化**。
 *
 * @see MiraiForwardMessageBuilder
 *
 */
public class MiraiSendOnlyForwardMessage private constructor(
    private val displayStrategy: ForwardMessage.DisplayStrategy,
    private val nodes: List<ComputableForwardMessageNode>,
) : MiraiSendOnlyComputableMessage<MiraiSendOnlyForwardMessage> {
    
    override val key: Message.Key<MiraiSendOnlyForwardMessage>
        get() = Key
    
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MiraiSendOnlyForwardMessage) return false
        
        return displayStrategy == other.displayStrategy && nodes == other.nodes
    }
    
    override fun hashCode(): Int {
        return displayStrategy.hashCode() * 31 + nodes.hashCode()
    }
    
    override fun toString(): String = "MiraiSendOnlyForwardMessage(displayStrategy=$displayStrategy, nodes=$nodes)"
    
    @OptIn(MiraiExperimentalApi::class)
    override suspend fun originalMiraiMessage(
        contact: OriginalMiraiContact,
        isDropAction: Boolean,
    ): net.mamoe.mirai.message.data.Message {
        val miraiNodes = nodes.map { node -> node.toNode(contact) }
        
        return buildForwardMessage(contact) {
            displayStrategy = this@MiraiSendOnlyForwardMessage.displayStrategy
            miraiNodes.forEach {
                add(it)
            }
        }
        
        // return RawForwardMessage(miraiNodes).render(displayStrategy)
    }
    
    public companion object Key : Message.Key<MiraiSendOnlyForwardMessage> {
        override fun safeCast(value: Any): MiraiSendOnlyForwardMessage? = doSafeCast(value)
        
        internal fun create(
            displayStrategy: ForwardMessage.DisplayStrategy,
            nodes: List<ComputableForwardMessageNode>,
        ): MiraiSendOnlyForwardMessage = MiraiSendOnlyForwardMessage(displayStrategy, nodes)
    }
}

internal sealed class ComputableForwardMessageNode {
    abstract suspend fun toNode(contact: OriginalMiraiContact): ForwardMessage.Node
}

private data class DelegatedComputableForwardMessageNode(private val node: ForwardMessage.Node) :
    ComputableForwardMessageNode() {
    override suspend fun toNode(contact: OriginalMiraiContact): ForwardMessage.Node {
        return node
    }
}

private data class SampleComputableForwardMessageNode(
    private val senderId: Long,
    private val time: Int,
    private val senderName: String,
    private val messages: Messages,
) : ComputableForwardMessageNode() {
    override suspend fun toNode(contact: OriginalMiraiContact): ForwardMessage.Node {
        val chain = messages.toOriginalMiraiMessageChain(contact, true)
        return ForwardMessage.Node(senderId, time, senderName, chain)
    }
}

private data class StandardComputableForwardMessageNode(
    private val function: suspend (OriginalMiraiContact) -> ForwardMessage.Node,
) : ComputableForwardMessageNode() {
    override suspend fun toNode(contact: OriginalMiraiContact): ForwardMessage.Node = function(contact)
    
    
    companion object {
        inline fun calculateSender(
            time: Int, messages: Messages,
            crossinline senderCalculator: suspend (OriginalMiraiContact) -> Pair<Long, String>,
        ): StandardComputableForwardMessageNode {
            return StandardComputableForwardMessageNode { contact ->
                val (senderId, senderName) = senderCalculator(contact)
                val chain = messages.toOriginalMiraiMessageChain(contact, true)
                ForwardMessage.Node(senderId, time, senderName, chain)
            }
        }
    }
}

/**
 * 通过 [MiraiForwardMessage] 构建一个仅用于发送的转发消息 [MiraiSendOnlyForwardMessage]。
 *
 */
public inline fun buildMiraiForwardMessage(block: @ForwardMessageDsl MiraiForwardMessageBuilder.() -> Unit): MiraiSendOnlyForwardMessage {
    return MiraiForwardMessageBuilder().also(block).build()
}


/**
 *
 * 用于构建 [MiraiForwardMessage] 的构建器。
 *
 * 类似于 [ForwardMessageBuilder], 但是**有所简化**，且使用的为 simbot 中的实体对象。
 *
 * [MiraiForwardMessageBuilder] 屏蔽了构建 [ForwardMessageBuilder] 时所需要的 [OriginalMiraiContact] 参数，
 * 但取而代之的，当你想要向转发消息中添加消息时，将不能直接使用 [Bot] 来拼接消息，而应该使用 [OrganizationBot]
 * 或其下其他衍生类型。
 *
 * 在 Kotlin 中，你可以直接使用顶层函数 [buildMiraiForwardMessage] 来构建。
 * ```kotlin
 * buildMiraiForwardMessage(displayStrategy = ...) {
 *     add(114514, "用户名", "你好".toText(), Timestamp.now())
 *     add(114514, "用户名", At(810.ID) + "你好".toText())
 *
 *     add(messageEvent)
 *     add(messageEvent, 12345678)
 *     add(messageEvent, Timestamp.now())
 *
 *     bot.says("最近如何".toText() + Face(5.ID))
 *     user.says("感觉不错".toText())
 *     // ....
 * }
 * ```
 *
 * 对于 Java 开发者，直接构建并使用 [MiraiForwardMessageBuilder] 即可。
 *
 * ```java
 * MiraiForwardMessageBuilder builder = new MiraiForwardMessageBuilder();
 * builder.add(123456, "forte", Text.of("早上好"));
 * builder.add(bot)
 *
 * builder.add(event);
 * builder.add(event.getBot(), Text.of("早上好"));
 *
 * // 在 Java 中，基本所有的API都叫 'add'。包括在Kotlin中被成为 'says' 的那些。
 *
 * MiraiSendOnlyForwardMessage message = builder.build();
 *
 * ```
 *
 *
 *
 * @see ForwardMessageBuilder
 */
public class MiraiForwardMessageBuilder(
    /**
     * mirai中合并转发卡片展示策略.
     *
     * @see ForwardMessage.DisplayStrategy
     */
    public var displayStrategy: ForwardMessage.DisplayStrategy = ForwardMessage.DisplayStrategy,
) {
    private val nodes: MutableList<ComputableForwardMessageNode> = mutableListOf()
    
    private fun append(node: ComputableForwardMessageNode) {
        nodes.add(node)
    }
    
    private fun append(node: ForwardMessage.Node) {
        append(DelegatedComputableForwardMessageNode(node))
    }
    
    private fun append(senderId: Long, senderName: String, time: Int, messages: Messages) {
        append(SampleComputableForwardMessageNode(senderId, time, senderName, messages))
    }
    
    private fun append(senderId: Long, senderName: String, time: Int, message: Message) {
        append(senderId, senderName, time, message.asMessages())
    }
    
    private fun append(block: suspend (OriginalMiraiContact) -> ForwardMessage.Node) {
        append(StandardComputableForwardMessageNode(block))
    }
    
    private inline fun append(
        time: Int,
        messages: Messages,
        crossinline block: suspend (OriginalMiraiContact) -> Pair<Long, String>,
    ) {
        append(StandardComputableForwardMessageNode.calculateSender(time, messages, block))
    }
    
    /**
     * 当前时间。
     * 构建node（追加消息节点时）时若不指定时间, 则会使用 [currentSecondTimestamp] 自增 1 的时间。
     *
     * 类似于 [ForwardMessageBuilder.currentTime]，单位为秒。
     *
     */
    public var currentSecondTimestamp: Int = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt()
    
    
    private fun userCurrentTime(): Int = currentSecondTimestamp++
    
    // region 直接操作
    
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
    
    // endregion
    
    // region 基础属性
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID. 必须保证其结果可以转化为 [Long], 比如提供一个 [LongID].
     * @param senderName 发送者名称
     */
    public fun add(
        senderId: ID,
        senderName: String,
        time: Timestamp,
        message: Message,
    ): MiraiForwardMessageBuilder = apply {
        append(senderId.tryToLong(), senderName, time.second.toInt(), message.asMessages())
    }
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID
     * @param senderName 发送者名称
     */
    public fun add(
        senderId: Long,
        senderName: String,
        time: Timestamp,
        message: Message,
    ): MiraiForwardMessageBuilder = apply {
        append(senderId, senderName, time.second.toInt(), message.asMessages())
        
    }
    
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID. 必须保证其结果可以转化为 [Long], 比如提供一个 [LongID].
     * @param senderName 发送者名称
     * @param secondTimestamp 秒时间戳
     */
    @JvmOverloads
    public fun add(
        senderId: ID,
        senderName: String,
        message: Message,
        secondTimestamp: Int = userCurrentTime(),
    ): MiraiForwardMessageBuilder = apply {
        append(senderId.tryToLong(), senderName, secondTimestamp, message.asMessages())
    }
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID
     * @param senderName 发送者名称
     * @param secondTimestamp 秒时间戳
     */
    @JvmOverloads
    public fun add(
        senderId: Long,
        senderName: String,
        message: Message,
        secondTimestamp: Int = userCurrentTime(),
    ): MiraiForwardMessageBuilder = apply {
        append(senderId, senderName, secondTimestamp, message.asMessages())
    }
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID. 必须保证其结果可以转化为 [Long], 比如提供一个 [LongID].
     * @param senderName 发送者名称
     */
    public fun add(
        senderId: ID,
        senderName: String,
        time: Timestamp,
        messageContent: MessageContent,
    ): MiraiForwardMessageBuilder = add(senderId, senderName, time, messageContent.messages)
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID
     * @param senderName 发送者名称
     */
    public fun add(
        senderId: Long,
        senderName: String,
        time: Timestamp,
        messageContent: MessageContent,
    ): MiraiForwardMessageBuilder = add(senderId, senderName, time, messageContent.messages)
    
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID. 必须保证其结果可以转化为 [Long], 比如提供一个 [LongID].
     * @param senderName 发送者名称
     * @param secondTimestamp 秒时间戳
     */
    @JvmOverloads
    public fun add(
        senderId: ID,
        senderName: String,
        messageContent: MessageContent,
        secondTimestamp: Int = userCurrentTime(),
    ): MiraiForwardMessageBuilder = add(senderId, senderName, messageContent.messages, secondTimestamp)
    
    /**
     * 根据 [MiraiForwardMessage.Node] 中的最基本的属性添加内容。
     *
     * @param senderId 消息发送者的ID
     * @param senderName 发送者名称
     * @param secondTimestamp 秒时间戳
     */
    @JvmOverloads
    public fun add(
        senderId: Long,
        senderName: String,
        messageContent: MessageContent,
        secondTimestamp: Int = userCurrentTime(),
    ): MiraiForwardMessageBuilder = add(senderId, senderName, messageContent.messages, secondTimestamp)
    
    // endregion
    
    // region User&Bot
    
    /**
     * 追加一个 [Bot] 说的话。
     */
    @JvmName("add")
    @JvmOverloads
    public fun Bot.says(
        message: Message,
        secondTimestamp: Int = userCurrentTime(),
        name: String? = null,
    ): MiraiForwardMessageBuilder = this@MiraiForwardMessageBuilder.apply {
        val bot = this@says
        
        if (name != null) {
            append(bot.id.tryToLong(), name, secondTimestamp, message)
            return@apply
        }
        
        if (bot is OrganizationBot) {
            append { contact ->
                val botAsMember = bot.asMember()
                ForwardMessage.Node(
                    botAsMember.id.tryToLong(),
                    secondTimestamp,
                    botAsMember.nickOrUsername,
                    message.toOriginalMiraiMessageChain(contact, true)
                )
            }
            return@apply
        }
        
        if (bot !is MiraiBot) {
            throw SimbotIllegalArgumentException("The type of bot only supports [MiraiBot]. but ${bot::class.simpleName} ($bot)")
        }
        
        val originalBot = bot.originalBot
        append(secondTimestamp, message.asMessages()) {
            when {
                it is OriginalMiraiGroup && it.bot.id == originalBot.id -> {
                    val member = it.botAsMember
                    member.id to member.nick
                }
                
                else -> originalBot.id to originalBot.nick
            }
        }
    }
    
    /**
     * 追加一个 [Bot] 说的话。
     */
    @JvmName("add")
    @JvmOverloads
    public fun Bot.says(
        text: String,
        secondTimestamp: Int = userCurrentTime(),
        name: String? = null,
    ): MiraiForwardMessageBuilder =
        says(text.toText(), secondTimestamp, name)
    
    /**
     * 追加一个 [Contact] 说的话。
     */
    @JvmName("add")
    @JvmOverloads
    public fun Contact.says(
        message: Message,
        secondTimestamp: Int = userCurrentTime(),
        name: String = when (val c = this@says) {
            is Member -> c.nickOrUsername
            else -> c.username
        },
    ): MiraiForwardMessageBuilder = this@MiraiForwardMessageBuilder.apply {
        append(id.tryToLong(), name, secondTimestamp, message)
    }
    
    /**
     * 追加一个 [Contact] 说的话。
     */
    @JvmName("add")
    @JvmOverloads
    public fun Contact.says(
        text: String,
        secondTimestamp: Int = userCurrentTime(),
        name: String = when (val c = this@says) {
            is Member -> c.nickOrUsername
            else -> c.username
        },
    ): MiraiForwardMessageBuilder = says(text.toText(), secondTimestamp, name)
    
    // endregion
    
    // region MessageEvent
    /**
     * 将一个 [MessageEvent] 转化为一个消息节点。
     */
    private fun addEvent(
        messageEvent: MessageEvent, secondTimestamp: Int,
    ): MiraiForwardMessageBuilder = apply {
        append { contact ->
            val userId = messageEvent.id.tryToLong()
            val username = when (messageEvent) {
                is ChatRoomMessageEvent -> messageEvent.author().nickOrUsername
                is ContactMessageEvent -> messageEvent.user().username
                else -> throw SimbotIllegalArgumentException("not ChatRoomMessageEvent or ContactMessageEvent")
            }
            val chain = messageEvent.messageContent.messages.toOriginalMiraiMessageChain(contact, true)
            ForwardMessage.Node(userId, secondTimestamp, username, chain)
        }
    }
    
    /**
     * 追加一个 [ChatRoomMessageEvent] 作为转发消息的元素。
     */
    @JvmOverloads
    public fun add(
        messageEvent: ChatRoomMessageEvent,
        time: Timestamp = messageEvent.timestamp,
    ): MiraiForwardMessageBuilder = add(messageEvent, time.second.toInt())
    
    /**
     * 追加一个 [ContactMessageEvent] 作为转发消息的元素。
     */
    @JvmOverloads
    public fun add(
        messageEvent: ContactMessageEvent,
        time: Timestamp = messageEvent.timestamp,
    ): MiraiForwardMessageBuilder = add(messageEvent, time.second.toInt())
    
    /**
     * 追加一个 [ChatRoomMessageEvent] 作为转发消息的元素。
     *
     * @param secondTimestamp 消息发送的秒时间戳
     */
    public fun add(
        messageEvent: ChatRoomMessageEvent,
        secondTimestamp: Int,
    ): MiraiForwardMessageBuilder = addEvent(messageEvent, secondTimestamp)
    
    /**
     * 追加一个 [ContactMessageEvent] 作为转发消息的元素。
     *
     * @param secondTimestamp 消息发送的秒时间戳
     */
    public fun add(
        messageEvent: ContactMessageEvent,
        secondTimestamp: Int,
    ): MiraiForwardMessageBuilder = addEvent(messageEvent, secondTimestamp)
    
    
    // endregion
    
    
    /**
     * 将当前构建器中的信息整合转化为 [MiraiSendOnlyForwardMessage]。
     */
    public fun build(): MiraiSendOnlyForwardMessage {
        return MiraiSendOnlyForwardMessage.create(displayStrategy, nodes.toList())
    }
    
    
    private fun Message.asMessages(): Messages = when (this) {
        is Message.Element<*> -> toMessages()
        is Messages -> this
    }
}


