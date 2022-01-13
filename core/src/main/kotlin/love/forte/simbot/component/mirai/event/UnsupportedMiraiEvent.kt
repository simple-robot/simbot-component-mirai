package love.forte.simbot.component.mirai.event

import love.forte.simbot.ID
import love.forte.simbot.SimbotDiscreetApi
import love.forte.simbot.Timestamp
import love.forte.simbot.component.mirai.MiraiBot
import love.forte.simbot.event.BaseEventKey
import love.forte.simbot.event.Event
import love.forte.simbot.message.doSafeCast

/**
 * 所有未提供针对性实现的其他mirai事件的包装体。
 *
 * [UnsupportedMiraiEvent] 不实现任何其他事件类型，仅实现mirai组件中的事件父类型 [MiraiEvent] ，是一个完全独立的事件类型。
 *
 * [UnsupportedMiraiEvent] 可以在一定程度上满足仅升级mirai的版本而不更新simbot的版本而带来的事件增量的情况，
 * 所有未支持的事件都会通过此类型进行推送。
 *
 * 如果要监听 [UnsupportedMiraiEvent], 你需要谨慎处理其中的一切，
 * 因为 [UnsupportedMiraiEvent] 能够提供的事件会随着当前组件实现的特定事件的增多而减少，这种减少可能会伴随着版本更新而产生，且可能不会有任何说明或错误提示。
 * 因此你应当首先查看 [MiraiEvent] 下是否有所需的已经实现的事件类型，并且不应当过分依赖 [UnsupportedMiraiEvent]。
 *
 * Kotlin中，你可以通过 [ifIs] 来过滤类型并在匹配的时候执行逻辑, 例如：
 * ```kotlin
 *  suspend fun UnsupportedMiraiEvent.listener() {
 *      ifIs<GroupAllowMemberInviteEvent> {
 *          // 这是Mirai原生的消息发送方式。
 *          group.sendMessage("'允许群员邀请好友加群'变更了: $origin -> $new")
 *      }
 *  }
 * ```
 * *注：实例中的事件类型仅用于演示，不保证其一定会出现在 [UnsupportedMiraiEvent] 中。*
 *
 * @see MiraiEvent
 * @see ifIs
 *
 * @author ForteScarlet
 */
@SimbotDiscreetApi
public class UnsupportedMiraiEvent
internal constructor(override val bot: MiraiBot, nativeEvent: NativeMiraiEvent) : MiraiEvent {
    override val key: Event.Key<UnsupportedMiraiEvent> get() = Key
    override val timestamp: Timestamp = Timestamp.now()
    override val visibleScope: Event.VisibleScope get() = Event.VisibleScope.PRIVATE
    override val metadata: Metadata = Metadata("".ID, nativeEvent)


    public data class Metadata(override val id: ID, override val nativeEvent: NativeMiraiEvent) : MiraiEvent.Metadata

    public companion object Key : BaseEventKey<UnsupportedMiraiEvent>("mirai.unsupported", MiraiEvent) {
        override fun safeCast(value: Any): UnsupportedMiraiEvent? = doSafeCast(value)
    }
}

/**
 * 当 metadata 中的 nativeEvent 类型符合 [E] 的时候，执行逻辑。
 */
@OptIn(SimbotDiscreetApi::class)
public inline fun <reified E : NativeMiraiEvent> UnsupportedMiraiEvent.ifIs(block: E.() -> Unit) {
    val e = metadata.nativeEvent
    if (e is E) {
        e.block()
    }
}


