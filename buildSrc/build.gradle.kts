/*
 *  Copyright (c) 2021-2023 ForteScarlet.
 *
 *  本文件是 simbot-component-mirai 的一部分。
 *
 *  simbot-component-mirai 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero通用公共许可证修改之，无论是版本 3 许可证，还是（按你的决定）任何以后版都可以。
 *
 *  发布 simbot-component-mirai 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero通用公共许可证，了解详情。
 *
 *  你应该随程序获得一份 GNU Affero通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
 *
 *
 */
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val rootProperties =
    rootDir.resolve("../build_versions.properties").useLines { lines ->
        lines.map { it.trim() }
            .filter { !it.startsWith('#') }
            .associate { line ->
                line.split(limit = 2, delimiters = charArrayOf('=')).let { split ->
                    split[0] to split[1]
                }
            }
    }

val kotlinVersion = rootProperties["kotlin_version"]
val dokkaPluginVersion = rootProperties["dokka_version"]
val suspendTransformPlugin = rootProperties["suspend_transform_plugin_version"]
val gradleCommon = rootProperties["gradle_common_version"]

dependencies {
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("serialization", kotlinVersion))

    // dokka
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaPluginVersion")
    implementation("org.jetbrains.dokka:dokka-base:$dokkaPluginVersion")

    // see https://github.com/gradle-nexus/publish-plugin
    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
    implementation("love.forte.plugin.suspend-transform:suspend-transform-plugin-gradle:$suspendTransformPlugin")

    // gradle common
    implementation("love.forte.gradle.common:gradle-common-core:$gradleCommon")
    implementation("love.forte.gradle.common:gradle-common-kotlin-multiplatform:$gradleCommon")
    implementation("love.forte.gradle.common:gradle-common-publication:$gradleCommon")
}
