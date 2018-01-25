package com.mfk.lockfree.benchmark.map;

import com.mfk.lockfree.map.LockFreeMap;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.mfk.lockfree.benchmark.map.ConcurrentMapOp.performConcurrentGet;
import static com.mfk.lockfree.util.Utils.intr;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class GetWithCollisionsBenchmark {
    @Benchmark
    public long measureLockFreeMap(LockFreeMapProvider lockFreeMapHolder) throws Exception {
        return performConcurrentGet(d -> lockFreeMapHolder.map.get(d).orElse(null));
    }

    @Benchmark
    public long measureJDKMap(JavaMapProvider javaMapProvider) throws Exception {
        ConcurrentMap<Key, Value> map = javaMapProvider.map;
        return performConcurrentGet(map::get);
    }

    @State(Scope.Benchmark)
    public static class LockFreeMapProvider {
        final LockFreeMap<Key, Value> map;

        public LockFreeMapProvider() {
            this.map = LockFreeMap.newMap(1000, 10);
            intr(100_000).mapToObj(CollisionKey::new).forEach(k -> map.put(k, new Value(k.getKey())));
        }
    }

    @State(Scope.Benchmark)
    public static class JavaMapProvider {
        final ConcurrentMap<Key, Value> map;

        public JavaMapProvider() {
            this.map = new ConcurrentHashMap<>();
            intr(100_000).mapToObj(CollisionKey::new).forEach(k -> map.put(k, new Value(k.getKey())));
        }
    }

}
