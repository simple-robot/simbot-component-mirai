/*
 *  Copyright (c) 2022 ForteScarlet <ForteScarlet@163.com>
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

import P.Simbot.SNAPSHOT

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


abstract class SimbotProject {
    abstract val group: String
    abstract val version: String
}


/**
 * Project versions.
 */
sealed class P : SimbotProject() {
    object Simbot {
        val SNAPSHOT = System.getProperty("isSnapshot")?.equals("true", true) ?: false
        const val GROUP = "love.forte.simbot"
        const val REAL_VERSION = "3.0.0.preview.4.0"

        val VERSION = if (SNAPSHOT) "$REAL_VERSION-SNAPSHOT" else REAL_VERSION

    }

    object Simboot {
        const val GROUP = "love.forte.simbot.boot"
        val VERSION = Simbot.VERSION
    }

    object ComponentMirai {
        val isSnapshot get() = SNAPSHOT
        const val GROUP = "${Simbot.GROUP}.component"
        private const val REAL_VERSION = "0.2"
        private val MERGE_VERSION = "${Simbot.REAL_VERSION}-${V.Mirai.VERSION_SIM}.$REAL_VERSION"
        val VERSION: String =
            if (isSnapshot) "$MERGE_VERSION-SNAPSHOT"
            else MERGE_VERSION


        const val API = ":simbot-component-mirai-api"
        const val CORE = ":simbot-component-mirai-core"
        const val BOOT = ":simbot-component-mirai-boot"
    }

}



