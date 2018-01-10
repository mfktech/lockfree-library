package com.mfk.lockfree.map;

import com.mfk.lockfree.list.LockFreeList;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

class LockFreeLinkedArrayMap<K, V> implements LockFreeMap<K, V> {
    private final LockFreeList<Pair<K, V>>[] hashArray;

    LockFreeLinkedArrayMap(final int mapSize) {
        this.hashArray = new LockFreeList[mapSize];
        IntStream.range(0, mapSize).forEach(i -> this.hashArray[i] = LockFreeList.newList(100));
    }

    @Override
    public void put(K key, V value) {
        if (key == null) return;

        final int hash = getHash(key);
        Pair<K, V> pair = new Pair<>(key, value);
        hashArray[hash].append(pair);
    }

    @Override
    public Optional<V> get(K key) {
        if (key == null) return Optional.empty();

        return this.hashArray[getHash(key)]
                .findAll(p -> Objects.equals(p.k, key))
                .map(p -> p.v)
                .reduce((f, s) -> s);
    }

    @Override
    public Stream<V> getAll(K key) {
        if (key == null) return Stream.empty();

        return this.hashArray[getHash(key)]
                .findAll(p -> Objects.equals(p.k, key))
                .map(p -> p.v);
    }

    @Override
    public boolean remove(K key) {
        return key != null && hashArray[getHash(key)].removeAll(p -> Objects.equals(p.k, key)).findFirst().isPresent();
    }

    @Override
    public long size() {
        return Arrays.stream(hashArray).mapToLong(l -> l.stream()
                .collect(groupingBy(p -> p.k)).size()).sum();
    }

    @Override
    public Stream<Map.Entry<K, V>> stream() {
        return Arrays.stream(hashArray)
                .map(l -> l.stream().collect(groupingBy(p -> p.k)))
                .flatMap(this::convertGroupedMap);
    }

    private Stream<Map.Entry<K, V>> convertGroupedMap(Map<K, List<Pair<K, V>>> groupedBy) {
        return groupedBy.entrySet().stream()
                .map(e -> {
                    Optional<Pair<K, V>> reduced = e.getValue().stream().reduce((f, s) -> s);
                    return reduced.map(p -> new AbstractMap.SimpleEntry<>(e.getKey(), p.v));
                }).flatMap(Optional::stream);
    }


    private int getHash(K key) {
        return key.hashCode() % hashArray.length;
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
