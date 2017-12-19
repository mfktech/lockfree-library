package com.mfk.lockfree.benchmark;

import com.mfk.lockfree.list.LockFreeConcurrentList;
import com.mfk.lockfree.list.LockFreeConcurrentListImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by farhankhan on 12/19/17.
 */
@SuppressWarnings("unused")
public class LockFreeListBenchMark {
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void benchMarkAppend() throws Exception {
        LockFreeConcurrentList<String> lockFreeConcurrentList = new LockFreeConcurrentListImpl<>();

        CompletableFuture<Void> fut1 = createFuture("T1", lockFreeConcurrentList);
        CompletableFuture<Void> fut2 = createFuture("T2", lockFreeConcurrentList);
        CompletableFuture<Void> fut3 = createFuture("T3", lockFreeConcurrentList);
        CompletableFuture<Void> fut4 = createFuture("T4", lockFreeConcurrentList);
        CompletableFuture<Void> fut5 = createFuture("T5", lockFreeConcurrentList);
        CompletableFuture<Void> fut6 = createFuture("T6", lockFreeConcurrentList);

        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(fut1, fut2, fut3, fut4, fut5, fut6);
        completableFuture.get();
    }

    private CompletableFuture<Void> createFuture(final String prefix, final LockFreeConcurrentList<String> lockFreeConcurrentList) {
        return CompletableFuture.runAsync(() ->
                IntStream.range(0, 1024 * 1024 * 1024).forEach(i -> lockFreeConcurrentList.append(prefix + ":" + i))
        );
    }
}
