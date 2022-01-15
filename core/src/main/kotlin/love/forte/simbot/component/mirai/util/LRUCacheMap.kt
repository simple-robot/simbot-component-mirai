package love.forte.simbot.component.mirai.util

/**
 *
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