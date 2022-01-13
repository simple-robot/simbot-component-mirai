plugins {
    `java-library`
    kotlin("jvm")
}


repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(project(P.ComponentMirai.CORE))
    api("love.forte.simbot.boot:simboot-api:${P.Simbot.VERSION}")

    testImplementation(V.Kotlin.Test.Junit.notation)
    testImplementation(V.Log4j.Api.notation)
    testImplementation(V.Log4j.Core.notation)
    testImplementation(V.Log4j.Slf4jImpl.notation)
    testImplementation(V.Kotlinx.Serialization.Yaml.notation)

    // implementation("love.forte.simple-robot:api:3.0.0-PREVIEW")
}

tasks.getByName<Test>("test") {
    useJUnit()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        javaParameters = true
        jvmTarget = "1.8"
    }
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