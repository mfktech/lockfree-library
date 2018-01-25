package com.mfk.lockfree.map;

import org.junit.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.mfk.lockfree.util.Utils.intr;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.*;

public class LockFreeLinkedArrayMapTest {
    /**
     * Tests get method with no collisions and duplicates
     */
    @Test
    public void testGet1() {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap();
        intr(100).mapToObj(i -> new SimpleEntry<>(new StubKey(i), new StubValue(i)))
                .forEach(e -> lockFreeMap.put(e.getKey(), e.getValue()));
        assertEquals(100, lockFreeMap.size());
        assertFalse(lockFreeMap.get(new StubKey(100)).isPresent());
        assertEquals(new StubValue(39), lockFreeMap.get(new StubKey(39)).orElse(null));
        assertEquals(new StubValue(99), lockFreeMap.get(new StubKey(99)).orElse(null));
        assertEquals(new StubValue(0), lockFreeMap.get(new StubKey(0)).orElse(null));
    }

    /**
     * Test get method with collisions and duplicates
     */
    @Test
    public void testGet2() {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap(100, 100);
        intr(1000).mapToObj(i -> new SimpleEntry<>(new StubKey(i), new StubValue(i)))
                .forEach(e -> lockFreeMap.put(e.getKey(), e.getValue()));
        // Duplicates
        intr(1000).mapToObj(i -> new SimpleEntry<>(new StubKey(i), new StubValue(i)))
                .forEach(e -> lockFreeMap.put(e.getKey(), e.getValue()));

        assertEquals(1000, lockFreeMap.size());
        assertFalse(lockFreeMap.get(new StubKey(1000)).isPresent());
        assertEquals(new StubValue(0), lockFreeMap.get(new StubKey(0)).orElse(null));
        assertEquals(new StubValue(394), lockFreeMap.get(new StubKey(394)).orElse(null));
        assertEquals(new StubValue(991), lockFreeMap.get(new StubKey(991)).orElse(null));
        assertEquals(new StubValue(999), lockFreeMap.get(new StubKey(999)).orElse(null));
    }

    @Test
    public void testGetAll() {
        LockFreeMap<String, String> map = LockFreeMap.newMap();
        map.put("key1", "value1");
        map.put("key1", "newValue1");

        final List<String> values = map.getAll("key1").collect(toList());
        assertEquals(Arrays.asList("value1", "newValue1"), values);

        final Optional<String> value = map.get("key1");
        assertTrue(value.isPresent());
        assertEquals("newValue1", "newValue1", value.get());
    }

    /**
     * Tests remove method without duplicates and collisions
     */
    @Test
    public void testRemove1() {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap();
        intr(100).mapToObj(i -> new SimpleEntry<>(new StubKey(i), new StubValue(i)))
                .forEach(e -> lockFreeMap.put(e.getKey(), e.getValue()));
        assertEquals(100, lockFreeMap.size());
        assertFalse(lockFreeMap.remove(new StubKey(100)));
        assertTrue(lockFreeMap.remove(new StubKey(99)));
        assertEquals(99, lockFreeMap.size());
        assertTrue(lockFreeMap.remove(new StubKey(0)));
        assertEquals(98, lockFreeMap.size());
        assertFalse(lockFreeMap.remove(new StubKey(99)));
    }

    @Test
    public void testRemove2() {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap(100, 100);
        intr(1000).mapToObj(i -> new SimpleEntry<>(new StubKey(i), new StubValue(i)))
                .forEach(e -> lockFreeMap.put(e.getKey(), e.getValue()));
        // duplicates
        intr(1000).mapToObj(i -> new SimpleEntry<>(new StubKey(i), new StubValue(i)))
                .forEach(e -> lockFreeMap.put(e.getKey(), e.getValue()));

        assertEquals(1000, lockFreeMap.size());
        assertTrue(lockFreeMap.remove(new StubKey(437)));
        assertEquals(999, lockFreeMap.size());
        assertTrue(lockFreeMap.remove(new StubKey(102)));
        assertEquals(998, lockFreeMap.size());

        assertFalse(lockFreeMap.get(new StubKey(437)).isPresent());
        assertFalse(lockFreeMap.get(new StubKey(102)).isPresent());
    }

    @Test
    public void testRemove3() {
        LockFreeMap<String, String> map = LockFreeMap.newMap();
        map.put("k1", "v1");
        map.put("k1", "v2");
        assertEquals(Arrays.asList("v1", "v2"), map.getAll("k1").collect(toList()));

        map.remove("k1");
        assertEquals(Collections.emptyList(), map.getAll("k1").collect(toList()));
    }

    /**
     * Tests stream with no duplicates and collisions
     */
    @Test
    public void testGetEntries1() {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap();
        intr(100).mapToObj(i -> new SimpleEntry<>(new StubKey(i), new StubValue(i)))
                .forEach(e -> lockFreeMap.put(e.getKey(), e.getValue()));
        assertEquals(100, lockFreeMap.size());
        assertEquals(100, lockFreeMap.stream().count());
    }

    /**
     * Tests stream with duplicates and collisions
     */
    @Test
    public void testGetEntries2() {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap(100, 100);
        intr(1000).mapToObj(i -> new SimpleEntry<>(new StubKey(i), new StubValue(i)))
                .forEach(e -> lockFreeMap.put(e.getKey(), e.getValue()));
        // duplicates
        intr(1000).mapToObj(i -> new SimpleEntry<>(new StubKey(i), new StubValue(i)))
                .forEach(e -> lockFreeMap.put(e.getKey(), e.getValue()));

        assertEquals(1000, lockFreeMap.size());
        assertEquals(1000, lockFreeMap.stream().count());
    }

    @Test
    public void testPutNoCollision() {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap();
        putObjects(lockFreeMap, 0, 100);
        assertEquals(100, lockFreeMap.size());
    }

    @Test
    public void testPutBigSize() {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap(1000, 1000);
        putObjects(lockFreeMap, 0, 100000);
        assertEquals(100000, lockFreeMap.size());
        assertEquals(new StubValue(21344), lockFreeMap.get(new StubKey(21344)).orElse(null));
        assertEquals(new StubValue(54723), lockFreeMap.get(new StubKey(54723)).orElse(null));
        assertEquals(new StubValue(99999), lockFreeMap.get(new StubKey(99999)).orElse(null));
        assertEquals(new StubValue(0), lockFreeMap.get(new StubKey(0)).orElse(null));
        assertFalse(lockFreeMap.get(new StubKey(100000)).isPresent());
    }

    @Test
    public void testMultiThreadedPut() throws Exception {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap(1000, 1000);
        int threads = 4;
        Stream<CompletableFuture<?>> wFut = intr(threads)
                .mapToObj(i -> runAsync(() -> putObjects(lockFreeMap, i * 100, (i * 100) + 100)));
        CompletableFuture.allOf(wFut.toArray(CompletableFuture[]::new)).get();
        assertEquals(400, lockFreeMap.size());
    }

    @Test
    public void testMultiThreadedDuplicatesPut() throws Exception {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap(100, 100);
        int threads = 4;
        Stream<CompletableFuture<?>> wFut = intr(threads)
                .mapToObj(i -> runAsync(() -> putObjects(lockFreeMap, 0, 100)));
        CompletableFuture.allOf(wFut.toArray(CompletableFuture[]::new)).get();
        assertEquals(100, lockFreeMap.size());
    }

    @Test
    public void testMultiThreadedRemove() throws Exception {
        LockFreeMap<StubKey, StubValue> lockFreeMap = LockFreeMap.newMap(1000, 1000);
        putObjects(lockFreeMap, 0, 10000);
        int threads = 4;
        Stream<CompletableFuture<?>> wFut = intr(threads)
                .mapToObj(i -> runAsync(() -> intr(10000).forEach(ind -> lockFreeMap.remove(new StubKey(ind)))));
        CompletableFuture.allOf(wFut.toArray(CompletableFuture[]::new)).get();
        assertEquals(0, lockFreeMap.size());
    }

    private void putObjects(LockFreeMap<StubKey, StubValue> lockFreeMap, int start, int end) {
        IntStream.range(start, end).forEach(i -> lockFreeMap.put(new StubKey(i), new StubValue(i)));
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StubValue stubValue = (StubValue) o;
            return v == stubValue.v;
        }

        @Override
        public int hashCode() {
            return Objects.hash(v);
        }
    }
}
