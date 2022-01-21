package love.forte.simbot.component.mirai.event

import love.forte.simbot.Api4J
import love.forte.simbot.action.ReplySupport
import love.forte.simbot.action.SendSupport
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.component.mirai.MiraiMember
import love.forte.simbot.component.mirai.NativeMiraiMember
import love.forte.simbot.component.mirai.SimbotMiraiMessageReceipt
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.Event
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.doSafeCast
import love.forte.simbot.utils.runInBlocking
import net.mamoe.mirai.event.events.GroupTempMessageEvent


/**
 * @see GroupTempMessageEvent
 */
public typealias NativeMiraiGroupTempMessageEvent = GroupTempMessageEvent


/**
 * mirai群临时会话事件。
 *
 * @author ForteScarlet
 */
public interface MiraiMemberMessageEvent
    : MiraiSimbotContactMessageEvent<GroupTempMessageEvent>,
    ContactMessageEvent, ReplySupport, SendSupport {

    override val bot: MiraiBot

    @OptIn(Api4J::class)
    override val user: MiraiMember

    override val key: Event.Key<MiraiMemberMessageEvent> get() = Key
    override val metadata: Metadata


    override suspend fun send(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember>
    override suspend fun send(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember>

    override suspend fun reply(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember>
    override suspend fun reply(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember>


    //// Impl
    override suspend fun reply(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiMember>
        = reply(message.messages)

    @Api4J
    override fun replyBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember>
        = runInBlocking { reply(text) }

    @Api4J
    override fun replyBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember>
        = runInBlocking { reply(message) }

    @Api4J
    override fun replyBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiMember>
        = runInBlocking { reply(message) }


    override suspend fun send(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiMember>
        = send(message.messages)

    @Api4J
    override fun sendBlocking(text: String): SimbotMiraiMessageReceipt<NativeMiraiMember>
        = runInBlocking { send(text) }

    @Api4J
    override fun sendBlocking(message: Message): SimbotMiraiMessageReceipt<NativeMiraiMember>
        = runInBlocking { send(message) }

    @Api4J
    override fun sendBlocking(message: MessageContent): SimbotMiraiMessageReceipt<NativeMiraiMember>
        = runInBlocking { send(message) }

    public interface Metadata : MiraiSimbotEvent.Metadata<GroupTempMessageEvent>

    public companion object Key : BaseEventKey<MiraiMemberMessageEvent>(
        "mirai.group_temp_message",
        MiraiSimbotContactMessageEvent, ContactMessageEvent
    ) {
        override fun safeCast(value: Any): MiraiMemberMessageEvent? = doSafeCast(value)
    }

}
