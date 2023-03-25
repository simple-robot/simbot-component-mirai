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


plugins {
    id("simbot-mirai.module-conventions")
    id("simbot-mirai.maven-publish")
    id("simbot.suspend-transform-configure")
}


dependencies {
    compileOnly(SIMBOT_CORE)
    
    api(libs.mirai)
    api(libs.kotlinx.serialization.json)
    
    compileOnly(libs.kotlinx.serialization.properties)
    compileOnly(libs.charleskorn.kaml)
    
    
    testImplementation(libs.kotlinx.serialization.properties)
    testImplementation(libs.kotlinx.serialization.hocon)
    testImplementation(libs.charleskorn.kaml)
    testImplementation(SIMBOT_CORE)
    val log4j2Version = "2.9.1"
    testImplementation("org.apache.logging.log4j", "log4j-api", log4j2Version)
    testImplementation("org.apache.logging.log4j", "log4j-core", log4j2Version)
    testImplementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4j2Version)
    
    //https://github.com/Ricky12Awesome/json-schema-serialization
    testImplementation("com.github.Ricky12Awesome:json-schema-serialization:0.6.6")
//    testImplementation(rootProject.files("libs/fix-protocol-version-1.3.0.mirai2.jar"))
}

repositories {
    @Suppress("DEPRECATION")
    jcenter()
}

