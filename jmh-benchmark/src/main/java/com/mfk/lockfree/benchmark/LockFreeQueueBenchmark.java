package com.mfk.lockfree.benchmark;

import com.mfk.lockfree.queue.LockFreeQueue;
import com.mfk.lockfree.util.Utils;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.mfk.lockfree.util.Utils.intr;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 100)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@SuppressWarnings("unused")
public class LockFreeQueueBenchmark {
    @Benchmark
    public long measureLockFreeQueue() throws Exception {
        final int MB = 1024 * 1024;

        final int wThreads = 3;
        final int rThreads = 3;

        ExecutorService es = Executors.newFixedThreadPool(6);
        LockFreeQueue<DataStub> queue = LockFreeQueue.newUnboundedQueue();

        Runnable w = () -> intr(MB).mapToObj(DataStub::new).forEach(queue::add);
        Supplier<Long> r = createReader(queue, MB * wThreads / rThreads);

        List<CompletableFuture<?>> writers = intr(wThreads).mapToObj(i -> runAsync(w, es)).collect(toList());
        List<CompletableFuture<Long>> readers = intr(rThreads).mapToObj(i -> supplyAsync(r, es)).collect(toList());

        CompletableFuture.allOf(concat(writers.stream(), readers.stream()).toArray(CompletableFuture[]::new)).get();
        long total = readers.stream().map(Utils::get).mapToLong(d -> d).sum();
        System.out.println("Total: " + total);
        return total;
    }

    private Supplier<Long> createReader(LockFreeQueue<DataStub> queue, int times) {
        return () -> Stream.iterate(0, i -> i < times, i -> i + 1)
                .map(i -> queue.poll()).flatMap(Optional::stream)
                .count();
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
