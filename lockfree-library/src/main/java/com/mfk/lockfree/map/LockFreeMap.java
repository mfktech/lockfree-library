package com.mfk.lockfree.map;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A concurrent map which provides thread-safe operations using Lock-Free algorithms (Atomic classes). Please
 * note following important points:
 * <ul>
 * <li>This map relies on the same rules for {@code equals} and {@code hashCode}. Poorly written {@code hashCode}
 * will affect performance drastically.</li>
 * <p>
 * <li>It does not increase dynamically which means that the collisions will increase, if
 * reaching/exceeding max size of the underlying lookup table. It works very efficiently if the map is sized
 * appropriately.<br>
 * For example, for 75 keys the map should be created with the {@code maxSize} of 100.
 * Use {@link #newMap(int, int)} to create the map with the max size of the underlying lookup table.
 * </li>
 * <p>
 * <li>The value objects are never overwritten if {@link #put(Object, Object)} is called multiple times for the same
 * key with different value objects. The {@link #get(Object)} will retrieve the "last" put value for the given key.
 * {@link #getAll(Object)} will retrieve all the associated values objects for the given key.</li>
 * <p>
 * <li></li>
 * </ul>
 *
 * @param <K> the type of the key
 * @param <V> the type of the value.
 */
public interface LockFreeMap<K, V> {
    /**
     * Static factory method that creates the map with default settings.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     * @return created map with default configuration.
     */
    static <K, V> LockFreeMap<K, V> newMap() {
        return newMap(1000, 1000);
    }

    /**
     * Static factory method that creates the map with the given size.
     *
     * @param mapSize the size of the map.
     * @param <K>     the type of the key
     * @param <V>     the type of the value
     * @return created map with default configuration.
     */
    static <K, V> LockFreeMap<K, V> newMap(final int mapSize) {
        return new LockFreeLinkedArrayMap<>(mapSize, 1000);
    }

    /**
     * Static factory method that creates the map with the given size.
     *
     * @param mapSize the size of the map.
     * @param fragmentSize the size of the fragment of the Linked-Array list which is used to handle collisions
     * @param <K>     the type of the key
     * @param <V>     the type of the value
     * @return created map with default configuration.
     */
    static <K, V> LockFreeMap<K, V> newMap(final int mapSize, final int fragmentSize) {
        return new LockFreeLinkedArrayMap<>(mapSize, fragmentSize);
    }

    /**
     * @param key
     * @param value
     */
    void put(K key, V value);

    /**
     * @param key
     * @return
     */
    Optional<V> get(K key);

    Stream<V> getAll(K key);

    boolean remove(K key);

    long size();

    Stream<Map.Entry<K, V>> stream();
}
