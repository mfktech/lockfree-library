package com.mfk.lockfree.map;

import com.mfk.lockfree.list.LockFreeList;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.Map.Entry;

class Bucket<K, V> {
    private final LockFreeList<Entry<K, V>> collisionsList;
    private volatile Entry<K, V> cachedEntry;

    Bucket(LockFreeList<Entry<K, V>> collisionsList) {
        this.collisionsList = collisionsList;
    }

    void put(final Entry<K, V> Entry) {
        this.cachedEntry = Entry;
        collisionsList.append(Entry);
    }

    Optional<V> get(final K key) {
        if (cachedEntry == null) {
            return Optional.empty();
        } else if (Objects.equals(key, cachedEntry.getKey())) {
            return Optional.of(cachedEntry.getValue());
        } else {
            return collisionsList.findAll(p -> Objects.equals(p.getKey(), key))
                    .map(Entry::getValue)
                    .reduce((f, s) -> s);
        }
    }

    Stream<V> getAll(final K key) {
        return collisionsList.findAll(p -> Objects.equals(p.getKey(), key)).map(Entry::getValue);
    }

    boolean remove(K key) {
        return this.collisionsList.removeAll(p -> Objects.equals(p.getKey(), key))
                .findFirst()
                .isPresent();
    }

    Stream<Entry<K, V>> stream() {
        return collisionsList.stream();
    }
}
