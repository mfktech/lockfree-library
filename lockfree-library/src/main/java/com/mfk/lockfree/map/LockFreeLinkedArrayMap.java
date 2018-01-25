package com.mfk.lockfree.map;

import com.mfk.lockfree.list.LockFreeList;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

class LockFreeLinkedArrayMap<K, V> implements LockFreeMap<K, V> {
    private final Bucket<K, V>[] buckets;

    LockFreeLinkedArrayMap(final int mapSize, final int fragmentSize) {
        this.buckets = new Bucket[mapSize];
        IntStream.range(0, mapSize).forEach(i -> this.buckets[i] = new Bucket<>(LockFreeList.newList(fragmentSize)));
    }

    @Override
    public void put(K key, V value) {
        if (key == null) return;

        final int hash = getHash(key);
        Entry<K, V> pair = new SimpleImmutableEntry<>(key, value);
        buckets[hash].put(pair);
    }

    @Override
    public Optional<V> get(final K key) {
        if (key == null) return Optional.empty();
        return this.buckets[getHash(key)].get(key);
    }

    @Override
    public Stream<V> getAll(K key) {
        if (key == null) return Stream.empty();
        return buckets[getHash(key)].getAll(key);
    }

    @Override
    public boolean remove(K key) {
        return key != null && buckets[getHash(key)].remove(key);
    }

    @Override
    public long size() {
        return Arrays.stream(buckets)
                .mapToLong(l -> l.stream()
                .collect(groupingBy(Entry::getKey)).size())
                .sum();
    }

    @Override
    public Stream<Entry<K, V>> stream() {
        return Arrays.stream(buckets)
                .map(l -> l.stream().collect(groupingBy(Entry::getKey)))
                .flatMap(this::convertGroupedMap);
    }

    private Stream<Entry<K, V>> convertGroupedMap(Map<K, List<Entry<K, V>>> groupedBy) {
        return groupedBy.entrySet().stream()
                .map(e -> {
                    Optional<Entry<K, V>> reduced = e.getValue().stream().reduce((f, s) -> s);
                    return reduced.map(p -> new SimpleImmutableEntry<>(e.getKey(), p.getValue()));
                }).flatMap(Optional::stream);
    }

    private int getHash(K key) {
        return key.hashCode() % buckets.length;
    }
}
