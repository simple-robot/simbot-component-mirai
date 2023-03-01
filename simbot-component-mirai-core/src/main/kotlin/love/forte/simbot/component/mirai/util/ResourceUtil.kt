/*
 *  Copyright (c) 2023-2023 ForteScarlet.
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

@file:JvmName("ResourceUtil")
package love.forte.simbot.component.mirai.util

import love.forte.simbot.resources.ByteArrayResource
import love.forte.simbot.resources.FileResource
import love.forte.simbot.resources.PathResource
import love.forte.simbot.resources.Resource
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource

/**
 * 将 [Resource] 转化为 [ExternalResource].
 *
 * 得到的 [ExternalResource] 需要手动关闭。
 */
@JvmOverloads
public fun Resource.toExternalResource(formatName: String? = null): ExternalResource {
    return when (this) {
        is PathResource -> path.toFile().toExternalResource(formatName)
        is FileResource -> file.toExternalResource(formatName)
        is ByteArrayResource -> bytes.toExternalResource(formatName)
        else -> openStream().use { `in` -> `in`.toExternalResource(formatName) }
    }
}
