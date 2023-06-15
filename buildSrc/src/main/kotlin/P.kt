/*
 *  Copyright (c) 2023-2023 ForteScarlet.
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

import love.forte.gradle.common.core.project.ProjectDetail
import love.forte.gradle.common.core.project.Version
import love.forte.gradle.common.core.project.minus
import love.forte.gradle.common.core.project.version

val simbotVersionInfo = version(3, 0, 0) //- version("RC", 3)

@JvmField
val SIMBOT_VERSION = simbotVersionInfo.toString() //"3.0.0"

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
            val status = version("M7")
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
