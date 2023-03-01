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
}


dependencies {
    api(project(":simbot-component-mirai-core"))
    api("love.forte:catcode:1.0.0-BETA.1")
    
    compileOnly(SIMBOT_CORE)
    testImplementation(SIMBOT_CORE)
}

