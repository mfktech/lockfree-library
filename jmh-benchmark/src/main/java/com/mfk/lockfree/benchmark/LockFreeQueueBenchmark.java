package com.mfk.lockfree.benchmark;

import com.mfk.lockfree.queue.LockFreeQueue;
import com.mfk.lockfree.util.Utils;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        LockFreeQueue<Data> queue = LockFreeQueue.newQueue();
        return performOp(queue::add, () -> queue.poll().orElse(null));
    }

    @Benchmark
    public long measureJDKQueue() throws Exception {
        ConcurrentLinkedQueue<Data> queue = new ConcurrentLinkedQueue<>();
        return performOp(queue::add, queue::poll);
    }

    private long performOp(Consumer<Data> consumer, Supplier<Data> producer) throws Exception {
        final int wThreads = 2;
        final int rThreads = 2;
        final int writeCount = 1000 * 1000;
        final int pollCount = writeCount * wThreads / rThreads;

        Runnable writer = () -> intr(writeCount).mapToObj(Data::new).forEach(consumer);
        Supplier<?> reader = () -> intr(pollCount).mapToObj(i -> producer).count();

        List<CompletableFuture<?>> wFut = intr(wThreads).mapToObj(i -> runAsync(writer)).collect(toList());
        List<CompletableFuture<?>> rFut = intr(rThreads).mapToObj(i -> supplyAsync(reader)).collect(toList());
        CompletableFuture.allOf(concat(wFut.stream(), rFut.stream()).toArray(CompletableFuture[]::new)).get();

        return rFut.stream()
                .map(Utils::get)
                .filter(Objects::nonNull)
                .mapToLong(o -> Long.parseLong(o.toString()))
                .sum();
    }
}
