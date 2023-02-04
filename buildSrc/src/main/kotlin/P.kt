/*
 *  Copyright (c) 2023-2023 ForteScarlet <ForteScarlet@163.com>
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

import love.forte.gradle.common.core.project.ProjectDetail
import love.forte.gradle.common.core.project.Version
import love.forte.gradle.common.core.project.minus
import love.forte.gradle.common.core.project.version

/*
 *  Copyright (c) 2022-2023 ForteScarlet <ForteScarlet@163.com>
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

val simbotVersionInfo = version(3, 0, 0) - version("RC", 2)

@JvmField
val SIMBOT_VERSION = simbotVersionInfo.toString() //"3.0.0-RC.2"

@JvmField
val SIMBOT_CORE = "love.forte.simbot:simbot-core:$SIMBOT_VERSION"

/**
 * Project versions.
 */
@Suppress("MemberVisibilityCanBePrivate")
sealed class P {
    object ComponentMirai : ProjectDetail() {
        override val version: Version
        val versionWithoutSnapshot: Version

        init {

            val mainVersion = version(
                major = "${simbotVersionInfo.major}.${simbotVersionInfo.minor}",
                0, 0
            )
            val status = version("M4")
            versionWithoutSnapshot = mainVersion - status

            val mainStatus = if (isSnapshot()) (status - Version.SNAPSHOT) else status
            version = mainVersion - mainStatus
        }

        const val GROUP = "love.forte.simbot.component"
        const val DESCRIPTION = "simbot3框架针对mirai框架的组件实现"
        const val HOMEPAGE = "https://github.com/simple-robot/simbot-component-mirai"

        override val description: String
            get() = DESCRIPTION

        override val group: String
            get() = GROUP

        override val homepage: String
            get() = HOMEPAGE

        override val developers: List<Developer> = developers {
            developer {
                id = "forte"
                name = "ForteScarlet"
                email = "ForteScarlet@163.com"
                url = "https://github.com/ForteScarlet"
            }
            developer {
                id = "forliy"
                name = "ForliyScarlet"
                email = "ForliyScarlet@163.com"
                url = "https://github.com/ForliyScarlet"
            }
        }
        override val licenses: List<License> = licenses {
            license {
                name = "GNU GENERAL PUBLIC LICENSE, Version 3"
                url = "https://www.gnu.org/licenses/gpl-3.0-standalone.html"
            }
            license {
                name = "GNU LESSER GENERAL PUBLIC LICENSE, Version 3"
                url = "https://www.gnu.org/licenses/lgpl-3.0-standalone.html"
            }
        }
        override val scm: Scm = scm {
            url = HOMEPAGE
            connection = "scm:git:$HOMEPAGE.git"
            developerConnection = "scm:git:ssh://git@github.com/simple-robot/simbot-component-mirai.git"
        }
    }

}

inline fun isSnapshot(b: () -> Unit = {}): Boolean {
    b()
    val snapProp = System.getProperty("isSnapshot")?.toBoolean() ?: false
    val snapEnv = System.getenv(Env.IS_SNAPSHOT)?.toBoolean() ?: false

    println("IsSnapshot from system.property: $snapProp")
    println("IsSnapshot from system.env:      $snapEnv")

    return snapProp || snapEnv
}
