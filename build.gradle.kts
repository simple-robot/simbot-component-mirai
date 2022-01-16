
plugins {
    kotlin("jvm") version "1.6.0" apply false
    kotlin("plugin.serialization") version "1.6.0" apply false
    id("org.jetbrains.dokka") version "1.5.30" apply false
    `maven-publish`
    signing
    // see https://github.com/gradle-nexus/publish-plugin
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    idea
}

group = P.ComponentMirai.GROUP // love.forte.simbot.component
version = P.ComponentMirai.VERSION

println("=== Current version: $version ===")

repositories {
    mavenLocal()
    mavenCentral()
}

allprojects {
    group = P.ComponentMirai.GROUP
    version = P.ComponentMirai.VERSION

    apply(plugin = "maven-publish")
    apply(plugin = "java")
    apply(plugin = "signing")

    repositories {
        mavenLocal()
        mavenCentral()
    }


}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}


val credentialsUsername: String? = getProp("credentials.username")?.toString()
val credentialsPassword: String? = getProp("credentials.password")?.toString()

println("credentialsUsername: $credentialsUsername")

if (credentialsUsername != null && credentialsPassword != null) {
    nexusPublishing {
       packageGroup.set(P.ComponentMirai.GROUP)
       repositories {
           sonatype {
               username.set(credentialsUsername)
               password.set(credentialsPassword)
           }

       }
    }
}


 idea {
     module {
         isDownloadSources = true
     }
 }