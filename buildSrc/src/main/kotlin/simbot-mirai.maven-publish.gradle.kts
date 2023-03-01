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

import love.forte.gradle.common.core.Gpg
import love.forte.gradle.common.publication.configure.jvmConfigPublishing
import util.checkPublishConfigurable


plugins {
    id("signing")
    id("maven-publish")
}


checkPublishConfigurable {
    jvmConfigPublishing {
        project = P.ComponentMirai
        publicationName = "simbotDist"


        val jarSources by tasks.registering(Jar::class) {
            from(sourceSets["main"].allSource)
            archiveClassifier.set("sources")
        }


//        val dokkaHtmlJar by tasks.registering(Jar::class) {
//            dependsOn(tasks.dokkaHtml)
//            from(tasks.dokkaHtml.flatMap { it.outputDirectory })
//            archiveClassifier.set("html-docs")
//        }

        val jarJavadoc by tasks.registering(Jar::class) {
//            dependsOn(tasks.dokkaJavadoc)
//            from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
            dependsOn(tasks.dokkaHtml)
            from(tasks.dokkaHtml.flatMap { it.outputDirectory })
//            from(tasks.dokkaHtml)
            archiveClassifier.set("javadoc")
        }

        artifact(jarSources)
        artifact(jarJavadoc)
//        artifact(dokkaHtmlJar)

        isSnapshot = project.version.toString().contains("SNAPSHOT", true)
        releasesRepository = ReleaseRepository
        snapshotRepository = SnapshotRepository
        gpg = Gpg.ofSystemPropOrNull()

    }
    show()


}

internal val TaskContainer.dokkaJavadoc: TaskProvider<org.jetbrains.dokka.gradle.DokkaTask>
    get() = named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaJavadoc")

internal val TaskContainer.dokkaHtml: TaskProvider<org.jetbrains.dokka.gradle.DokkaTask>
    get() = named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml")


fun show() {
    //// show project info
    println("========================================================")
    println("== project.group:       $group")
    println("== project.name:        $name")
    println("== project.version:     $version")
    println("== project.description: $description")
    println("========================================================")
}


