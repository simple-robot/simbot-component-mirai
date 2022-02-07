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
        const val GROUP = "love.forte.simbot"
        const val VERSION = "3.0.0.preview.2.0"

    }

    object Simboot {
        const val GROUP = "love.forte.simbot.boot"
        const val VERSION = Simbot.VERSION
    }

    object ComponentMirai {
        const val GROUP = "${Simbot.GROUP}.component" // love.forte.simbot.component
        val VERSION = "${Simbot.VERSION}-${V.Mirai.VERSION_SIM}.0.1"

        const val API = ":simbot-component-mirai-api"
        const val CORE = ":simbot-component-mirai-core"
        const val BOOT = ":simbot-component-mirai-boot"
    }

}



