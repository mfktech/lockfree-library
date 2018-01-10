package com.mfk.lockfree.map;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface LockFreeMap<K, V> {
    /**
     *
     * @param <E>
     * @param <D>
     * @return
     */
    static <E, D> LockFreeMap<E, D> newMap() {
        return new LockFreeLinkedArrayMap<>(1000);
    }

    /**
     *
     * @param mapSize
     * @param <E>
     * @param <D>
     * @return
     */
    static <E, D> LockFreeMap<E, D> newMap(final int mapSize) {
        return new LockFreeLinkedArrayMap<>(mapSize);
    }

    /**
     *
     * @param key
     * @param value
     */
    void put(K key, V value);

    /**
     *
     * @param key
     * @return
     */
    Optional<V> get(K key);

    Stream<V> getAll(K key);

    boolean remove(K key);

    long size();

    Stream<Map.Entry<K, V>> stream();
}
