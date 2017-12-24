package com.mfk.lockfree.benchmark;

import com.mfk.lockfree.list.LockFreeList;
import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 100)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@SuppressWarnings("unused")
public class LockFreeListBenchmark {
    @Benchmark
    public long measureLockFreeListAppend() {
        final LockFreeList<Integer> lockFreeList = LockFreeList.newList();
        appendItems(6, () -> IntStream.range(0, 1024 * 1024).forEach(lockFreeList::append));
        return lockFreeList.size();
    }

    @Benchmark
    public long measureJDKListAppend() {
        final List<Integer> syncJDKList = Collections.synchronizedList(new LinkedList<>());
        appendItems(6, () -> IntStream.range(0, 1024 * 1024).forEach(syncJDKList::add));
        return syncJDKList.size();
    }

    private void appendItems(final int threads, final Runnable runnable) {
        List<CompletableFuture<?>> futures = IntStream.range(0, threads)
                .mapToObj(i -> CompletableFuture.runAsync(runnable)).collect(Collectors.toList());
        CompletableFuture<?> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[threads]));

        try {
            allFutures.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
