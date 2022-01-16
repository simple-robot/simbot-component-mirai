import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(V.Simbot.Api.notation)
    api(V.Mirai.CoreJvm.notation)
    testImplementation(V.Kotlin.Test.Junit5.notation)
    testImplementation(V.Log4j.Api.notation)
    testImplementation(V.Log4j.Core.notation)
    testImplementation(V.Log4j.Slf4jImpl.notation)
}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        javaParameters = true
        jvmTarget = "1.8"
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(File(rootProject.projectDir, "doc"))
}

kotlin {
    // 严格模式
    explicitApiWarning()


    sourceSets.all {
        languageSettings {
            optIn("kotlin.RequiresOptIn")
        }
    }
}


configurePublishing(name)
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