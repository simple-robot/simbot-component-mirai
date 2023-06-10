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

import love.forte.gradle.common.core.project.setup

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
 */

extra["mirai_version"] = libs.mirai.get().version

plugins {
    id("simbot-mirai.root-module-conventions")
    id("simbot-mirai.nexus-publish")
    id("simbot-mirai.dokka-multi-module")
    id("simbot-mirai.changelog-generator")
}

setup(P.ComponentMirai)


logger.info("=== Current version: {} ===", version)

tasks.create("createChangelog") {
    group = "build"
    doFirst {
        val realVersion = P.ComponentMirai.version.toString()
        val version = "v$realVersion"
        println("Generate change log for $version ...")
        // configurations.runtimeClasspath
        val changelogDir = rootProject.file(".changelog").also {
            it.mkdirs()
        }
        val file = File(changelogDir, "$version.md")
        if (!file.exists()) {
            file.createNewFile()
            val coreVersion = SIMBOT_VERSION
            val miraiVersion = libs.mirai.get().versionConstraint.requiredVersion // V.Mirai.VERSION
            val autoGenerateText = """
                **部分库版本参考**
                
                | **库** | **版本** |
                |:---------:|:------:|
                | [simbot核心库](https://github.com/simple-robot/simpler-robot) | [`v$coreVersion`](https://github.com/simple-robot/simpler-robot/releases/tag/v$coreVersion) |
                | [mirai](https://github.com/mamoe/mirai) | [`v$miraiVersion`](https://github.com/mamoe/mirai/releases/tag/v$miraiVersion) |
                
            """.trimIndent()


            file.writeText(autoGenerateText)
        }


    }
}

tasks.create("updateWebsiteVersionJson") {
    group = "build"
    doFirst {
        val version = P.ComponentMirai.version.toString()

        val websiteVersionJsonDir = rootProject.file("website/static")
        if (!websiteVersionJsonDir.exists()) {
            websiteVersionJsonDir.mkdirs()
        }
        val websiteVersionJsonFile = File(websiteVersionJsonDir, "version.json")
        if (!websiteVersionJsonFile.exists()) {
            websiteVersionJsonFile.createNewFile()
        }

        websiteVersionJsonFile.writeText(
            """
            {
              "version": "$version"
            }
        """.trimIndent()
        )
    }
}



fun repoRow(moduleName: String, group: String, id: String, version: String): String {
    return "| $moduleName | [$moduleName: v$version](https://repo1.maven.org/maven2/${
        group.replace(
            ".",
            "/"
        )
    }/${
        id.replace(
            ".",
            "/"
        )
    }/$version) | [$moduleName: v$version](https://search.maven.org/artifact/$group/$id/$version/jar)  |"
}

