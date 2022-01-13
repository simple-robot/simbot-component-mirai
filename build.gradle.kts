
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

    apply(plugin = "maven-publish")
    apply(plugin = "java")
    apply(plugin = "signing")

    repositories {
        mavenLocal()
        mavenCentral()
    }

    println("[publishing-configure] - [$name] configured.")
    // set gpg file path to root
    val secretKeyRingFileKey = "signing.secretKeyRingFile"
    // val secretKeyRingFile = local().getProperty(secretKeyRingFileKey) ?: throw kotlin.NullPointerException(secretKeyRingFileKey)
    val secretRingFile = File(project.rootDir, "ForteScarlet.gpg")
    extra[secretKeyRingFileKey] = secretRingFile
    setProperty(secretKeyRingFileKey, secretRingFile)

    signing {
        // val key = local().getProperty("signing.keyId")
        // val password = local().getProperty("signing.password")
        // this.useInMemoryPgpKeys(key, password)
        sign(publishing.publications)
    }

}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}


val credentialsUsername: String? = local().getProperty("credentials.username")
val credentialsPassword: String? = local().getProperty("credentials.password")

if (credentialsUsername != null && credentialsPassword != null) {
    nexusPublishing {
        packageGroup.set(P.Simbot.GROUP)

        repositories {
            sonatype {
                username.set(credentialsUsername)
                password.set(credentialsPassword)
            }

        }
    }
}
