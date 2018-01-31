package com.mfk.lockfree.benchmark.map;

import com.mfk.lockfree.map.LockFreeMap;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 100)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PutWithCollisionsBenchmark {

    @Benchmark
    public int measureLockFreeMap() throws Exception {
        LockFreeMap<CollisionKey, Value> map = LockFreeMap.newMap(1000, 100);
        ConcurrentMapOp.performConcurrentPut(i -> map.put(new CollisionKey(i), new Value(i)), 10_000);
        return map.get(new CollisionKey(0)).map(Value::getValue).orElse(0);
    }

    @Benchmark
    public int measureJavaMap() throws Exception {
        ConcurrentMap<CollisionKey, Value> map = new ConcurrentHashMap<>(1000);
        ConcurrentMapOp.performConcurrentPut(i -> map.put(new CollisionKey(i), new Value(i)), 10_000);
        return map.get(new CollisionKey(0)).getValue();
    }
}
