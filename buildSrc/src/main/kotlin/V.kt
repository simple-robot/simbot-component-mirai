/*
 *  Copyright (c) 2021-2022 ForteScarlet <ForteScarlet@163.com>
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

@file:Suppress("unused")

import org.gradle.api.artifacts.DependencyConstraint

abstract class Dep(val group: String?, val id: String, val version: String?) {
    abstract val isAbsolute: Boolean
    override fun toString(): String = "Dep($notation)"
    open fun constraints(constraints: DependencyConstraint): DependencyConstraint {
        return constraints
    }
}

val Dep.notation
    get() = buildString {
        if (group != null) append(group).append(':')
        append(id)
        if (version != null) append(':').append(version)
    }


sealed class V(group: String?, id: String, version: String?) : Dep(group, id, version) {
    object Mirai {
        const val VERSION = "2.11.1"
        const val NOTATION = "net.mamoe:mirai-core:$VERSION"
    }
    
    override val isAbsolute: Boolean get() = true
    
    sealed class Simbot(group: String = P.Simbot.GROUP, id: String, version: String = VERSION) : V(group, id, version) {
        companion object {
            val VERSION = P.Simbot.VERSION
        }
        
        object Api : Simbot(id = "simbot-api")
        object Core : Simbot(id = "simbot-core")
        object SimbotBootAnnotation : Simbot(group = P.Simboot.GROUP, id = "simboot-core-annotation")
        object ComponentCore : Simbot(id = "simbot-component-core")
        
    }
    
    // /**
    //  * Kotlinx 相关依赖项
    //  */
    // sealed class Kotlinx(id: String, version: String?, override val isAbsolute: Boolean) :
    //     V("org.jetbrains.kotlinx", "kotlinx-$id", version) {
    //
    //
    //     // https://github.com/Kotlin/kotlinx.coroutines
    //     sealed class Coroutines(id: String) : Kotlinx(id = "coroutines-$id", VERSION, true) {
    //         companion object {
    //             const val VERSION = "1.6.2"
    //         }
    //
    //         // https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/README.md
    //         object Core : Coroutines("core") {
    //             object Jvm : Coroutines("core-jvm")
    //             object Js : Coroutines("core-js")
    //         }
    //
    //         object Debug : Coroutines("debug")
    //         object Test : Coroutines("test")
    //
    //         // =======
    //         //   https://github.com/Kotlin/kotlinx.coroutines/blob/master/reactive/README.md
    //         object Reactive : Coroutines("reactive")
    //         object Reactor : Coroutines("reactor")
    //         object Rx2 : Coroutines("rx2")
    //         object Rx3 : Coroutines("rx3")
    //         // =======
    //
    //
    //     }
    //
    //     // https://github.com/Kotlin/kotlinx.serialization
    //     sealed class Serialization(id: String) : Kotlinx(id = "serialization-$id", VERSION, true) {
    //         companion object {
    //             const val VERSION = "1.3.3"
    //         }
    //
    //         object Core : Serialization("core")
    //         object Json : Serialization("json")
    //         object Hocon : Serialization("hocon")
    //         object Protobuf : Serialization("protobuf")
    //         object Cbor : Serialization("cbor")
    //         object Properties : Serialization("properties")
    //         object Yaml : V("com.charleskorn.kaml", "kaml", "0.37.0")
    //     }
    //
    // }
    // sealed class Mirai(group: String, id: String, version: String = VERSION) : V(group, id, version) {
    //     companion object {
    //         const val MAJOR = 2
    //         const val MINOR = 11
    //         const val PATCH = 1
    //         const val VERSION = "$MAJOR.$MINOR.$PATCH"
    //     }
    //
    //     object CoreJvm : Mirai("net.mamoe", "mirai-core-jvm")
    // }
    
    
}
