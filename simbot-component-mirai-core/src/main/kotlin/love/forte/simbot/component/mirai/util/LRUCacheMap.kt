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

package love.forte.simbot.component.mirai.util

/**
 * 基于 [LinkedHashMap] 的简易LRU缓存器。
 * @author ForteScarlet
 */
public class LRUCacheMap<K, V>(
    private val removeEldestEntry: (eldest: MutableMap.MutableEntry<K, V>, currentSize: Int) -> Boolean,
    initialCapacity: Int,
    loadFactor: Float
) : LinkedHashMap<K, V>(initialCapacity, loadFactor, true) {
    public constructor(
        removeEldestEntry: (eldest: MutableMap.MutableEntry<K, V>, currentSize: Int) -> Boolean,
        initialCapacity: Int
    ) : this(removeEldestEntry, initialCapacity, 0.75F)

    public constructor(removeEldestEntry: (eldest: MutableMap.MutableEntry<K, V>, currentSize: Int) -> Boolean) : this(
        removeEldestEntry,
        16,
        0.75F
    )

    public constructor(maxSize: Int) : this({ _, currentSize -> currentSize >= maxSize }, 16, 0.75F)

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>): Boolean {
        return removeEldestEntry(eldest, size)
    }
}
