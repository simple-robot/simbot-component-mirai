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


plugins {
    id("simbot-mirai.root-module-conventions")
    id("simbot-mirai.nexus-publish")
    id("simbot-mirai.dokka-multi-module")
    id("simbot-mirai.changelog-generator")
}

group = P.ComponentMirai.GROUP
version = P.ComponentMirai.VERSION
description = P.ComponentMirai.DESCRIPTION

println("=== Current version: $version ===")

tasks.create("createChangelog") {
    group = "build"
    doFirst {
        val realVersion = P.ComponentMirai.version.fullVersion(false)
        val version = "v$realVersion"
        println("Generate change log for $version ...")
        // configurations.runtimeClasspath
        val changelogDir = rootProject.file(".changelog").also {
            it.mkdirs()
        }
        val file = File(changelogDir, "$version.md")
        if (!file.exists()) {
            file.createNewFile()
            val coreVersion = libs.versions.simbot.get() // P.Simbot.version.fullVersion(false)
            val miraiVersion = libs.mirai.get().versionConstraint.requiredVersion // V.Mirai.VERSION
            val autoGenerateText = """
                > 对应核心版本: [**v$coreVersion**](https://github.com/ForteScarlet/simpler-robot/releases/tag/v$coreVersion)
                >
                > 对应[**mirai**](https://github.com/mamoe/mirai)版本: [v$miraiVersion](https://github.com/ForteScarlet/simpler-robot/releases/tag/$miraiVersion)
               
                **仓库参考:**
                
                | **模块** | **repo1.maven** | **search.maven** |
                |---------|-----------------|------------------|
                ${repoRow("simbot-mirai-core", "love.forte.simbot.component", "simbot-component-mirai-core", realVersion)}
                ${repoRow("simbot-mirai-boot", "love.forte.simbot.component", "simbot-component-mirai-boot", realVersion)}
                ${repoRow("simbot-mirai-extra-catcode", "love.forte.simbot.component", "simbot-component-mirai-extra-catcode", realVersion)}
                
            """.trimIndent()
            
            
            file.writeText(autoGenerateText)
        }
    }
}

fun repoRow(moduleName: String, group: String, id: String, version: String): String {
    return "| $moduleName | [$moduleName: v$version](https://repo1.maven.org/maven2/${group.replace(".", "/")}/${id.replace(".", "/")}/$version) | [$moduleName: v$version](https://search.maven.org/artifact/$group/$id/$version/jar)  |"
}