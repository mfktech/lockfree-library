package com.mfk.lockfree.map;

import org.junit.Test;

import java.util.AbstractMap.SimpleEntry;

import static com.mfk.lockfree.util.Utils.intr;
import static junit.framework.TestCase.assertEquals;

public class LockFreeMapTest {
    @Test
    public void testHash() {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap(100);
        assertEquals(0, ((LockFreeLinkedArrayMap) lockFreeMap).getHash(new StubKey(100)));
        assertEquals(1, ((LockFreeLinkedArrayMap) lockFreeMap).getHash(new StubKey(101)));
    }

    @Test
    public void testPutNoCollision() {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap();
        intr(100).mapToObj(i -> new SimpleEntry<>(new StubKey(i), new StubValue(i))).forEach(e -> lockFreeMap.put(e.getKey(), e.getValue()));
        assertEquals(100, lockFreeMap.size());
    }

    private static class StubKey {
        private final int k;

        private StubKey(int k) {
            this.k = k;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StubKey stubKey = (StubKey) o;
            return k == stubKey.k;
        }

        @Override
        public int hashCode() {
            return k;
        }
    }

    private static class StubValue {
        private final int v;

        private StubValue(int v) {
            this.v = v;
        }
    }
}
