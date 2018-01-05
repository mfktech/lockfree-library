package com.mfk.lockfree.map;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class LockFreeLinkedArrayMap<K, V> implements LockFreeMap<K, V> {
    @Override
    public boolean put(K key, V value) {
        return false;
    }

    @Override
    public Optional<V> get(K key) {
        return Optional.empty();
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Stream<Map.Entry<K, V>> stream() {
        return null;
    }
}
