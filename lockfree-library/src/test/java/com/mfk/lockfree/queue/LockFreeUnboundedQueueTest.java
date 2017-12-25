package com.mfk.lockfree.queue;

import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static junit.framework.TestCase.assertEquals;

public class LockFreeUnboundedQueueTest {
    private static final int MEG = 1024 * 1024;

    @Test
    public void testAdd() {
        LockFreeUnboundedQueue<DataObj> queue = new LockFreeUnboundedQueue<>();
        getDataObjStream(MEG).forEach(queue::add);
        assertEquals(MEG, queue.size());
    }

    @Test
    public void testPoll() {
        LockFreeUnboundedQueue<DataObj> queue = new LockFreeUnboundedQueue<>();

        getDataObjStream(MEG).forEach(queue::add);
        assertEquals(MEG, queue.size());

        List<DataObj> res = intr(MEG).mapToObj(i -> queue.poll()).flatMap(Optional::stream).collect(toList());
        assertEquals(MEG, res.size());
        assertEquals(getDataObjStream(MEG).collect(toList()), res);
    }

    /**
     * Tests with 2 readers and 2 writers
     *
     * @throws Exception if readers or writers behaved unexpectedly.
     */
    @Test
    public void testMultiThreadedAddAndPoll() throws Exception {
        final int wThreads = 2;
        final int rThreads = 2;

        LockFreeUnboundedQueue<DataObj> queue = new LockFreeUnboundedQueue<>();

        Runnable writerSup = createWriter(queue, MEG);
        Supplier<Long> readerSup = createReader(queue, MEG * wThreads / rThreads);

        List<CompletableFuture<?>> w = intr(wThreads).mapToObj(i -> runAsync(writerSup)).collect(toList());
        List<CompletableFuture<Long>> r = intr(rThreads).mapToObj(i -> supplyAsync(readerSup)).collect(toList());

        CompletableFuture.allOf(concat(w.stream(), r.stream()).toArray(CompletableFuture[]::new)).get();
        assertEquals(2 * MEG, r.stream().map(this::getSilently).mapToLong(Long::longValue).sum());
    }

    private IntStream intr(int writers) {
        return IntStream.range(0, writers);
    }

    private Stream<DataObj> getDataObjStream(final int end) {
        return intr(end).mapToObj(DataObj::new);
    }

    private <T> T getSilently(CompletableFuture<T> f) {
        try {
            return f.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Supplier<Long> createReader(LockFreeQueue<DataObj> queue, int times) {
        return () -> Stream.iterate(0, i -> i < times, i -> i + 1)
                .map(i -> queue.poll()).flatMap(Optional::stream)
                .count();
    }

    private Runnable createWriter(LockFreeQueue<DataObj> queue, int times) {
        return () -> getDataObjStream(times).forEach(queue::add);
    }

    private static class DataObj {
        private final int data;

        private DataObj(int data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataObj dataObj = (DataObj) o;
            return data == dataObj.data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }
    }
}
