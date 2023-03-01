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


// tasks.create("createChangelog") {
//     group = "build"
//     doFirst {
//         val realVersion = P.ComponentMirai.version.fullVersion(false)
//         val version = "v$realVersion"
//         println("Generate change log for $version ...")
//         // configurations.runtimeClasspath
//         val changelogDir = rootProject.file(".changelog").also {
//             it.mkdirs()
//         }
//         val file = File(changelogDir, "$version.md")
//         if (!file.exists()) {
//             file.createNewFile()
//             val coreVersion = P.Simbot.version.fullVersion(false)
//             val miraiVersion = V.Mirai.VERSION
//             val autoGenerateText = """
//                 > 对应核心版本: [**v$coreVersion**](https://github.com/ForteScarlet/simpler-robot/releases/tag/v$coreVersion)
//                 >
//                 > 对应[**mirai**](https://github.com/mamoe/mirai)版本: [v$miraiVersion](https://github.com/ForteScarlet/simpler-robot/releases/tag/$miraiVersion)
//
//                 **仓库参考:**
//
//                 | **模块** | **repo1.maven** | **search.maven** |
//                 |---------|-----------------|------------------|
//                 ${repoRow("simbot-mirai-core", "love.forte.simbot.component", "simbot-component-mirai-core", realVersion)}
//                 ${repoRow("simbot-mirai-boot", "love.forte.simbot.component", "simbot-component-mirai-boot", realVersion)}
//                 ${repoRow("simbot-mirai-extra-catcode", "love.forte.simbot.component", "simbot-component-mirai-extra-catcode", realVersion)}
//
//             """.trimIndent()
//
//
//             file.writeText(autoGenerateText)
//         }
//     }
// }
//
//
// fun repoRow(moduleName: String, group: String, id: String, version: String): String {
//     return "| $moduleName | [$moduleName: v$version](https://repo1.maven.org/maven2/${group.replace(".", "/")}/${id.replace(".", "/")}/$version) | [$moduleName: v$version](https://search.maven.org/artifact/$group/$id/$version/jar)  |"
// }
