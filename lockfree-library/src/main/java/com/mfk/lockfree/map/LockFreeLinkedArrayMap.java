package com.mfk.lockfree.map;

import com.mfk.lockfree.list.LockFreeList;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class LockFreeLinkedArrayMap<K, V> implements LockFreeMap<K, V> {
    private final LockFreeList<Pair<K, V>>[] hashedArray;

    LockFreeLinkedArrayMap(final int mapSize) {
        this.hashedArray = new LockFreeList[mapSize];
        IntStream.range(0, mapSize).forEach(i -> this.hashedArray[i] = LockFreeList.newList(100));
    }

    @Override
    public Optional<V> put(K key, V value) {
        if (key == null) Optional.empty();
        final int hash = key.hashCode();
        Pair<K, V> pair = new Pair<>(key, value);
        final Optional<Pair<K, V>> removedObj = hashedArray[hash].remove(pair);
        hashedArray[hash].append(pair);
        return removedObj.map(p -> p.v);
    }

    @Override
    public Optional<V> get(K key) {
        if (key == null) Optional.empty();
        final int hash = getHash(key);
        return hashedArray[hash].stream().filter(p -> Objects.equals(p.k, key)).findFirst().map(p -> p.v);
    }

    @Override
    public boolean remove(K key) {
        if (key == null) Optional.empty();
        final int hash = key.hashCode();
        Pair<K, V> pair = new Pair<>(key, null);
        return hashedArray[hash].remove(pair).isPresent();
    }

    @Override
    public long size() {
        return IntStream.range(0, hashedArray.length).mapToLong(i -> hashedArray[i].size()).sum();
    }

    @Override
    public Stream<Map.Entry<K, V>> stream() {
        return null;
    }

    int getHash(K key) {
        return key.hashCode() % hashedArray.length;
    }

    private static class Pair<K, V> {
        private final K k;
        private final V v;

        private Pair(K k, V v) {
            this.k = k;
            this.v = v;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(k, pair.k);
        }

        @Override
        public int hashCode() {
            return k.hashCode();
        }
    }
}
