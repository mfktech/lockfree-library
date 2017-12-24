package com.mfk.lockfree.map;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface LockFreeMap<K, V> {
    boolean put(K key, V value);

    Optional<V> get(K key);

    long size();

    Stream<Map.Entry<K, V>> stream();
}
