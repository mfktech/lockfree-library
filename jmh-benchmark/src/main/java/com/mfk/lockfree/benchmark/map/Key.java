package com.mfk.lockfree.benchmark.map;

import java.util.Objects;

public class Key {
    private final int k;

    public Key(int k) {
        this.k = k;
    }

    public int getKey() {
        return k;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return k == key.k;
    }

    @Override
    public int hashCode() {
        return Objects.hash(k);
    }
}
