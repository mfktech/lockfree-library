package com.mfk.lockfree.benchmark.map;

import java.util.Objects;

public class CollisionKey extends Key {
    public CollisionKey(int k) {
        super(k);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollisionKey key = (CollisionKey) o;
        return getKey() == key.getKey();
    }

    @Override
    public int hashCode() {
        // this would cause collisions
        return Objects.hash(getKey() % 10);
    }
}
