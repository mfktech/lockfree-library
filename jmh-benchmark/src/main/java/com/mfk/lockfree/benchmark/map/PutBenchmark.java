package com.mfk.lockfree.benchmark.map;

import com.mfk.lockfree.map.LockFreeMap;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.*;
import java.util.function.IntConsumer;

import static com.mfk.lockfree.util.Utils.intr;
import static java.util.concurrent.CompletableFuture.runAsync;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 100)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PutBenchmark {
    @Benchmark
    public int measureJavaMap() throws Exception {
        ConcurrentMap<Key, Value> javaMap = new ConcurrentHashMap<>(1000);
        ConcurrentMapOp.performConcurrentPut(i -> javaMap.put(new Key(i), new Value(i)), 1000);
        return javaMap.get(new Key(0)).getValue();
    }

    @Benchmark
    public long measureLockFreeMap() throws Exception {
        LockFreeMap<Key, Value> lockFreeMap = LockFreeMap.newMap(1000,10);
        ConcurrentMapOp.performConcurrentPut(i -> lockFreeMap.put(new Key(i), new Value(i)), 1000);
        return lockFreeMap.get(new Key(0)).map(Value::getValue).orElse(0);
    }
}
