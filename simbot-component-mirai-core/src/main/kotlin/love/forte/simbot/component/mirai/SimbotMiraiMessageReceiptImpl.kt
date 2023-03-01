/*
 *  Copyright (c) 2022-2023 ForteScarlet.
 *
 *  This file is part of simbot-component-mirai.
 *
 *  simbot-component-mirai is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  simbot-component-mirai is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with simbot-component-mirai. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package love.forte.simbot.component.mirai

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import love.forte.simbot.CharSequenceID
import love.forte.simbot.ID
import love.forte.simbot.action.DeleteSupport
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.component.mirai.message.MiraiQuoteReply
import love.forte.simbot.component.mirai.message.toOriginalMiraiMessage
import love.forte.simbot.literal
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.SingleMessageReceipt
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import org.jetbrains.annotations.ApiStatus
import net.mamoe.mirai.message.MessageReceipt as OriginalMiraiMessageReceipt


/**
 * Mirai组件中，封装使用 [OriginalMiraiMessageReceipt] 作为消息发送的回执对象。
 *
 *
 */
@JvmAsync
@JvmBlocking
public abstract class SimbotMiraiMessageReceipt<out C : Contact> : SingleMessageReceipt(), DeleteSupport {
    /**
     * mirai发送消息后得到的真正回执。
     */
    public abstract val receipt: OriginalMiraiMessageReceipt<C>

    /**
     * 此消息回执的可引用ID, 通过 [MessageSource.ID] 获得，具体内容参考其文档描述。
     *
     *
     * @see MessageSource.ID
     */
    public abstract override val id: ID

    /**
     * 此消息回执的可引用完整ID, 通过 [MessageSource.fullSerialID] 获得，相比 [id] 其包含了 [MessageSource] 的**所有**信息，
     * 但是其内容会更长，本质上是 [MessageSource] 的JSON序列化结果。
     *
     * @see MessageSource.fullSerialID
     */
    public abstract val fullId: ID

    /**
     * @suppress '回执'并没有 Reply 的语义，将会择期取消对 ReplySupport 的实现。
     * 如果希望达到'引用回复'的效果，参考使用 [MiraiQuoteReply].
     */
    @Deprecated("Will remove.", level = DeprecationLevel.ERROR)
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0.0")
    public abstract suspend fun reply(message: Message): SimbotMiraiMessageReceipt<Contact>

    /**
     * @suppress '回执'并没有 Reply 的语义，将会择期取消对 ReplySupport 的实现。
     * 如果希望达到'引用回复'的效果，参考使用 [MiraiQuoteReply].
     */
    @Deprecated("Will remove.", level = DeprecationLevel.ERROR)
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0.0")
    public abstract suspend fun reply(text: String): SimbotMiraiMessageReceipt<Contact>

    /**
     * @suppress '回执'并没有 Reply 的语义，将会择期取消对 ReplySupport 的实现。
     * 如果希望达到'引用回复'的效果，参考使用 [MiraiQuoteReply].
     */
    @Deprecated("Will remove.", level = DeprecationLevel.ERROR)
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0.0")
    public abstract suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<Contact>

    /**
     * 删除/撤回这条消息.
     */
    abstract override suspend fun delete(): Boolean
}


/**
 *
 * @see DeleteSupport
 * @see ReplySupport
 * @author ForteScarlet
 */
internal class SimbotMiraiMessageReceiptImpl<out C : Contact>(
    override val receipt: OriginalMiraiMessageReceipt<C>,
) : SimbotMiraiMessageReceipt<C>() {
    override val id: ID by lazy(LazyThreadSafetyMode.PUBLICATION) { receipt.source.ID }
    override val fullId: ID by lazy(LazyThreadSafetyMode.PUBLICATION) { receipt.source.fullSerialID }
    override val isSuccess: Boolean get() = true

    /**
     * 删除/撤回这条消息.
     */
    override suspend fun delete(): Boolean {
        receipt.recall()
        return true
    }

    @Deprecated("Will remove.", level = DeprecationLevel.ERROR)
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0.0")
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<Contact> {
        val quote = receipt.quote()
        val sendMessage = message.toOriginalMiraiMessage(receipt.target)
        val newReceipt = receipt.target.sendMessage(quote + sendMessage)
        return SimbotMiraiMessageReceiptImpl(newReceipt)
    }

    @Deprecated("Will remove.", level = DeprecationLevel.ERROR)
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0.0")
    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<Contact> {
        val quote = receipt.quote()
        val newReceipt = receipt.target.sendMessage(quote + text.toPlainText())
        return SimbotMiraiMessageReceiptImpl(newReceipt)
    }

    @Deprecated("Will remove.", level = DeprecationLevel.ERROR)
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0.0")
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<Contact> {
        val quote = receipt.quote()
        val sendMessage = message.messages.toOriginalMiraiMessage(receipt.target)
        val newReceipt = receipt.target.sendMessage(quote + sendMessage)
        return SimbotMiraiMessageReceiptImpl(newReceipt)
    }
}


public object MessageSourceIDConstant {
    public const val ARRAY_SEPARATOR: String = "."
    public const val ELEMENT_SEPARATOR: String = ":"
}

private val messageSourceSerializerJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    serializersModule = MessageSerializers.serializersModule
}

/**
 * 通过一个默认的 [Json] 对 [MessageSource] 进行序列化并将其作为ID使用。
 * 与 [MessageSource.ID] 相比，[fullSerialID] 保留了 [MessageSource] 内的**所有信息**，
 * 因此可以保证不丢失任何信息（例如在使用 [MiraiQuoteReply][love.forte.simbot.component.mirai.message.MiraiQuoteReply] 进行引用回复时候基本不会导致表现效果出现异常），
 * 但是同样因此其效率会更低，当消息内容过多时还可能会导致此ID非常长。
 *
 * @throws SerializationException 序列化过程中出现异常
 * @see MessageSource.ID
 */
public val MessageSource.fullSerialID: CharSequenceID
    get() {
        return messageSourceSerializerJson.encodeToString(MessageSource.serializer(), this).ID
    }

/**
 * 从通过 [MessageSource.fullSerialID] 序列化而来的ID格式中反序列化出 [MessageSource] 结果。
 *
 * @throws SerializationException 反序列化过程中出现异常
 * @throws IllegalArgumentException 反序列化过程中出现异常
 */
public fun ID.buildMessageSourceFromFullSerialId(): MessageSource {
    return messageSourceSerializerJson.decodeFromString(MessageSource.serializer(), this.literal)
}

/*
    三个定位属性 ids, internalId, time,
    然后botId和一个消息源类型
 */

/**
 * 通过 [MessageSource.ids]、[MessageSource.internalIds]、[MessageSource.time]、[MessageSource.botId]、[MessageSource.kind]
 * 来构建一个具有一定规则的ID。
 *
 * 相比较于 [MessageSource.fullSerialID], [MessageSource.ID] 可能会丢失部分信息（例如消息源的具体消息内容），
 * 而导致在部分情况下出现问题（比如通过 [MiraiQuoteReply][love.forte.simbot.component.mirai.message.MiraiQuoteReply]
 * 进行消息引用时可能会使得具体表现效果异常）
 *
 * @see MessageSource.fullSerialID
 */
public val MessageSource.ID: CharSequenceID
    get() {
        return buildString {
            ids.joinTo(this, MessageSourceIDConstant.ARRAY_SEPARATOR)
            append(MessageSourceIDConstant.ELEMENT_SEPARATOR)
            internalIds.joinTo(this, MessageSourceIDConstant.ARRAY_SEPARATOR)
            append(MessageSourceIDConstant.ELEMENT_SEPARATOR).append(time)
            append(MessageSourceIDConstant.ELEMENT_SEPARATOR).append(botId)
            append(MessageSourceIDConstant.ELEMENT_SEPARATOR).append(kind.ordinal)
        }.ID
    }

/**
 * 通过由 [MessageSource.ID] 得到的ID反序列化得到 [MessageSource] 实例。会丢失部分信息，例如原本的消息内容等。
 *
 * @throws IllegalArgumentException 当无法被正确解析时
 * @see MessageSource.ID
 * @see buildMessageSourceFromFullSerialId
 */
public inline fun ID.buildMessageSource(andThen: MessageSourceBuilder.() -> Unit = {}): MessageSource {
    val value = literal
    val elements = value.split(MessageSourceIDConstant.ELEMENT_SEPARATOR)
    require(elements.size == 5) { "The number of elements in the message source ID [$value] must be 5, but ${elements.size}" }

    // ids
    val ids = elements[0].splitToSequence(MessageSourceIDConstant.ARRAY_SEPARATOR).map(String::toInt)

    // internal ids
    val internalIds = elements[1].splitToSequence(MessageSourceIDConstant.ARRAY_SEPARATOR).map(String::toInt)

    // time
    val time = elements[2].toInt()

    // botId
    val botId = elements[3].toLong()

    // kind
    val kind = MessageSourceKind.values()[(elements[4].toInt())]

    return MessageSourceBuilder().id(*ids.toList().toIntArray()).internalId(*internalIds.toList().toIntArray())
            .time(time).apply(andThen).build(botId, kind)

}

/**
 *通过由 [MessageSource.ID] 得到的ID反序列化得到 [MessageSource] 实例。会丢失部分信息，例如原本的消息内容等。
 *
 * @throws IllegalArgumentException 当无法被正确解析时
 * @see MessageSource.ID
 * @see buildMessageSourceFromFullSerialId
 *
 */
public fun ID.buildMessageSource(): MessageSource {
    return buildMessageSource { }
}


/**
 * 如果ID以 `{` 开头、`}` 结尾，则会使用 [buildMessageSourceFromFullSerialId] 进行反序列化，否则将会使用 [buildMessageSource].
 *
 * @throws IllegalArgumentException 当无法被正确解析时
 * @throws SerializationException 当无法被正确解析时
 */
internal fun ID.tryBuildMessageSource(): MessageSource {
    val value = literal
    if (value.startsWith('{') && value.endsWith('}')) {
        return buildMessageSourceFromFullSerialId()
    }

    return buildMessageSource()
}
