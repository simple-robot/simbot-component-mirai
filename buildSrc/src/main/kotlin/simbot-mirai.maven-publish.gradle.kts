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
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        val jarJavadoc by tasks.registering(Jar::class) {
            archiveClassifier.set("javadoc")
        }

        artifact(jarSources)
        artifact(jarJavadoc)

        isSnapshot = project.version.toString().contains("SNAPSHOT", true)
        releasesRepository = ReleaseRepository
        snapshotRepository = SnapshotRepository
        gpg = Gpg.ofSystemPropOrNull()


    }
    show()


}

//val (isSnapshotOnly, isReleaseOnly, isPublishConfigurable) = checkPublishConfigurable()
//
//
//println("isSnapshotOnly: $isSnapshotOnly")
//println("isReleaseOnly: $isReleaseOnly")
//println("isPublishConfigurable: $isPublishConfigurable")
//
//
//if (isPublishConfigurable) {
//    val sonatypeUsername: String? = systemProp("OSSRH_USER")
//    val sonatypePassword: String? = systemProp("OSSRH_PASSWORD")
//
//    if (sonatypeUsername == null || sonatypePassword == null) {
//        println("[WARN] - sonatype.username or sonatype.password is null, cannot config nexus publishing.")
//    }
//
//    val jarSources by tasks.registering(Jar::class) {
//        archiveClassifier.set("sources")
//        from(sourceSets["main"].allSource)
//    }
//
//    val jarJavadoc by tasks.registering(Jar::class) {
//        archiveClassifier.set("javadoc")
//    }
//
//    publishing {
//        publications {
//            create<MavenPublication>("miraiDist") {
//                from(components["java"])
//                artifact(jarSources)
//                artifact(jarJavadoc)
//
//                groupId = project.group.toString()
//                artifactId = project.name
//                version = project.version.toString()
//                description = project.description ?: P.ComponentMirai.DESCRIPTION
//
//                pom {
//                    show()
//
//                    name.set("${project.group}:${project.name}")
//                    description.set(project.description ?: P.ComponentMirai.DESCRIPTION)
//                    url.set("https://github.com/simple-robot/simbot-component-mirai")
//                    licenses {
//                        license {
//                            name.set("GNU GENERAL PUBLIC LICENSE, Version 3")
//                            url.set("https://www.gnu.org/licenses/gpl-3.0-standalone.html")
//                        }
//                        license {
//                            name.set("GNU LESSER GENERAL PUBLIC LICENSE, Version 3")
//                            url.set("https://www.gnu.org/licenses/lgpl-3.0-standalone.html")
//                        }
//                    }
//                    scm {
//                        url.set("https://github.com/simple-robot/simbot-component-mirai")
//                        connection.set("scm:git:https://github.com/simple-robot/simbot-component-mirai.git")
//                        developerConnection.set("scm:git:ssh://git@github.com/simple-robot/simbot-component-mirai.git")
//                    }
//
//                    setupDevelopers()
//                }
//            }
//
//
//
//            repositories {
//                configMaven(Sonatype.Central, sonatypeUsername, sonatypePassword)
//                configMaven(Sonatype.Snapshot, sonatypeUsername, sonatypePassword)
//            }
//        }
//    }
//
//    val keyId = System.getenv("GPG_KEY_ID")
//    val secretKey = System.getenv("GPG_SECRET_KEY")
//    val password = System.getenv("GPG_PASSWORD")
//    if (keyId != null) {
//        signing {
//            setRequired {
//                !project.version.toString().endsWith("SNAPSHOT")
//            }
//
//            useInMemoryPgpKeys(keyId, secretKey, password)
//
//            sign(publishing.publications)
//        }
//    }
//
//
//
//    println("[publishing-configure] - [$name] configured.")
//}
//
//
//fun RepositoryHandler.configMaven(sonatype: Sonatype, username: String?, password: String?) {
//    maven {
//        name = sonatype.name
//        url = uri(sonatype.url)
//        credentials {
//            this.username = username
//            this.password = password
//        }
//    }
//}


fun show() {
    //// show project info
    println("========================================================")
    println("== project.group:       $group")
    println("== project.name:        $name")
    println("== project.version:     $version")
    println("== project.description: $description")
    println("========================================================")
}


