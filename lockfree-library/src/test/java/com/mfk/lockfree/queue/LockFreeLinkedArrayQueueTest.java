package com.mfk.lockfree.queue;

import com.mfk.lockfree.util.Utils;
import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mfk.lockfree.util.Utils.intr;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static junit.framework.TestCase.assertEquals;

public class LockFreeLinkedArrayQueueTest {
    private static final int MB = 1024 * 1024;
    private static final int KB = 1024;

    @Test
    public void testAdd1() {
        LockFreeLinkedArrayQueue<DataStub> queue = new LockFreeLinkedArrayQueue<>(1000);
        getDataObjStream(MB).forEach(queue::add);
        assertEquals(MB, queue.size());
    }

    @Test
    public void testAdd2() {
        LockFreeQueue<String> queue = LockFreeQueue.newQueue();
        queue.add("element1");
        queue.add("element2");

        queue.poll().ifPresent(System.out::println);
    }

    @Test
    public void testPoll() {
        LockFreeLinkedArrayQueue<DataStub> queue = new LockFreeLinkedArrayQueue<>(1000);

        getDataObjStream(MB).forEach(queue::add);
        assertEquals(MB, queue.size());

        List<DataStub> res = intr(MB).mapToObj(i -> queue.poll()).flatMap(Optional::stream).collect(toList());
        assertEquals(MB, res.size());
        assertEquals(getDataObjStream(MB).collect(toList()), res);
    }

    /**
     * Tests with 2 readers and 2 writers with 1KB of objects
     *
     * @throws Exception if readers or writers behaved unexpectedly.
     */
    @Test
    public void testMultiThreadedAddAndPoll1() throws Exception {
        final int wThreads = 2;
        final int rThreads = 2;

        ExecutorService es = Executors.newFixedThreadPool(4);
        LockFreeLinkedArrayQueue<DataStub> queue = new LockFreeLinkedArrayQueue<>(1000);

        Runnable writerSup = createWriter(queue, KB);
        Supplier<List<DataStub>> readerSup = createReaderWithList(queue, KB * wThreads / rThreads);

        List<CompletableFuture<?>> w = intr(wThreads).mapToObj(i -> runAsync(writerSup, es)).collect(toList());
        List<CompletableFuture<List<DataStub>>> r = intr(rThreads).mapToObj(i -> supplyAsync(readerSup, es)).collect(toList());

        CompletableFuture.allOf(concat(w.stream(), r.stream()).toArray(CompletableFuture[]::new)).get();
        List<DataStub> res = r.stream().flatMap(f -> Utils.get(f).stream()).collect(Collectors.toList());

        assertEquals(2 * KB, res.size());
        assertEquals(intr(KB).boxed().collect(toSet()), res.stream().map(s -> s.data).collect(toSet()));
    }

    /**
     * Tests with 2 readers and 2 writers
     *
     * @throws Exception if readers or writers behaved unexpectedly.
     */
    @Test
    public void testMultiThreadedAddAndPoll2() throws Exception {
        final int wThreads = 2;
        final int rThreads = 2;

        ExecutorService es = Executors.newFixedThreadPool(4);
        LockFreeLinkedArrayQueue<DataStub> queue = new LockFreeLinkedArrayQueue<>(1000);

        Runnable writerSup = createWriter(queue, MB);
        Supplier<Long> readerSup = createReader(queue, MB * wThreads / rThreads);

        List<CompletableFuture<?>> w = intr(wThreads).mapToObj(i -> runAsync(writerSup, es)).collect(toList());
        List<CompletableFuture<Long>> r = intr(rThreads).mapToObj(i -> supplyAsync(readerSup, es)).collect(toList());

        CompletableFuture.allOf(concat(w.stream(), r.stream()).toArray(CompletableFuture[]::new)).get();
        assertEquals(2 * MB, r.stream().map(Utils::get).mapToLong(Long::longValue).sum());
    }

    private Stream<DataStub> getDataObjStream(final int end) {
        return intr(end).mapToObj(DataStub::new);
    }

    private Supplier<List<DataStub>> createReaderWithList(LockFreeQueue<DataStub> queue, int times) {
        return () -> Stream.iterate(0, i -> i < times, i -> i + 1)
                .map(i -> queue.poll()).flatMap(Optional::stream)
                .collect(toList());
    }

    private Supplier<Long> createReader(LockFreeQueue<DataStub> queue, int times) {
        return () -> Stream.iterate(0, i -> i < times, i -> i + 1)
                .map(i -> queue.poll()).flatMap(Optional::stream)
                .count();
    }

    private Runnable createWriter(LockFreeQueue<DataStub> queue, int times) {
        return () -> getDataObjStream(times).forEach(queue::add);
    }

    private static class DataStub {
        private final int data;

        private DataStub(int data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataStub dataStub = (DataStub) o;
            return this.data == dataStub.data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }
    }
}
