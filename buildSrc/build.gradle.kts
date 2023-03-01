/*
 *  Copyright (c) 2021-2023 ForteScarlet.
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
