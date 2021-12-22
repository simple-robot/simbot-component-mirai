
plugins {
    kotlin("jvm") version "1.6.0" apply false
    kotlin("plugin.serialization") version "1.6.0" apply false
    id("org.jetbrains.dokka") version "1.5.30" apply false
    `maven-publish`
    signing
    // see https://github.com/gradle-nexus/publish-plugin
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"

}

group = P.ComponentMirai.GROUP // love.forte.simbot.component
version = P.ComponentMirai.VERSION

repositories {
    mavenLocal()
    mavenCentral()
}

allprojects {
    group = P.ComponentMirai.GROUP
    version = P.ComponentMirai.VERSION

    repositories {
        mavenLocal()
        mavenCentral()
    }

    apply(plugin = "maven-publish")
    apply(plugin = "signing")
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}