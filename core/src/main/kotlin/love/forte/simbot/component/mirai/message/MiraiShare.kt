package love.forte.simbot.component.mirai.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.Component
import love.forte.simbot.component.mirai.ComponentMirai
import love.forte.simbot.message.Message
import net.mamoe.mirai.message.data.RichMessage
import net.mamoe.mirai.message.data.ServiceMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.reflect.KClass

/**
 * mirai的(链接)分享模板实例。
 *
 * 此类型不会在接收消息中存在，仅用于发送。直接通过 [RichMessage.share] 构建真实的消息对象，
 * 请注意参考此函数的相关限制。
 *
 * 可以通过 [nativeMiraiMessage] 获取构建后的 [ServiceMessage] 对象实例。
 *
 * @see RichMessage
 * @see RichMessage.share
 * @author ForteScarlet
 */
@MiraiExperimentalApi
@SerialName("mirai.rich.share")
@Serializable
public data class MiraiShare @JvmOverloads constructor(
    private val url: String,
    private val title: String? = null,
    private val content: String? = null,
    private val coverUrl: String? = null
) : MiraiSendOnlySimbotMessage<MiraiShare>,
    MiraiNativeDirectlySimbotMessage<MiraiShare> {

    override val nativeMiraiMessage: ServiceMessage = RichMessage.share(
        url, title, content, coverUrl
    )

    override val key: Message.Key<MiraiShare>
        get() = Key

    public companion object Key : Message.Key<MiraiShare> {
        override val component: Component
            get() = ComponentMirai.component

        override val elementType: KClass<MiraiShare>
            get() = MiraiShare::class
    }
}


