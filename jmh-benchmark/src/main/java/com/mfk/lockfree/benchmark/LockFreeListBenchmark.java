package com.mfk.lockfree.benchmark;

import com.mfk.lockfree.list.LockFreeList;
import com.mfk.lockfree.list.LockFreeListImpl;
import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Fork(5)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@SuppressWarnings("unused")
public class LockFreeListBenchmark {
    @Benchmark
    @Threads(4)
    public long measureLockFreeListAppend(LockFreeListHolder lockFreeListHolder) {
        final LockFreeList<Integer> lockFreeList = lockFreeListHolder.lockFreeList;
        IntStream.range(0, 1024).forEach(i -> lockFreeList.append(i));
        return lockFreeList.stream().count();
    }

    @Benchmark
    @Threads(4)
    public long measureJDKListAppend(SynchronizedJDKListHolder syncJDKListHolder) {
        final List<Integer> syncJDKList = syncJDKListHolder.syncJDKList;
        IntStream.range(0, 1024).forEach(i -> syncJDKList.add(i));
        return syncJDKList.stream().count();
    }

    @State(Scope.Benchmark)
    public static class LockFreeListHolder {
        final LockFreeList<Integer> lockFreeList = new LockFreeListImpl<>(100);
    }

    @State(Scope.Benchmark)
    public static class SynchronizedJDKListHolder {
        final List<Integer> syncJDKList = Collections.synchronizedList(new LinkedList<>());
    }
}
