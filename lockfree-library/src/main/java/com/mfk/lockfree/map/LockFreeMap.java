package com.mfk.lockfree.map;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface LockFreeMap<K, V> {
    static <E, D> LockFreeMap<E, D> newMap() {
        return new LockFreeLinkedArrayMap<>(1000);
    }

    static <E, D> LockFreeMap<E, D> newMap(final int mapSize) {
        return new LockFreeLinkedArrayMap<>(mapSize);
    }

    Optional<V> put(K key, V value);

    Optional<V> get(K key);

    boolean remove(K key);

    long size();

    Stream<Map.Entry<K, V>> stream();
}
