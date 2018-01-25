package com.mfk.lockfree.benchmark.map;

import com.mfk.lockfree.map.LockFreeMap;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.mfk.lockfree.benchmark.map.ConcurrentMapOp.performConcurrentGet;
import static com.mfk.lockfree.util.Utils.intr;
import static java.util.stream.Collectors.toList;

@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class GetBenchmark {
    @Benchmark
    public long measureLockFreeMap1(LockFreeMapProvider lockFreeMapHolder) throws Exception {
        return performConcurrentGet(d -> lockFreeMapHolder.map.get(d).orElse(null));
    }

    @Benchmark
    public long measureJDKMap1(JavaMapProvider javaMapProvider) throws Exception {
        ConcurrentMap<Key, Value> map = javaMapProvider.map;
        return performConcurrentGet(map::get);
    }

    @State(Scope.Benchmark)
    public static class LockFreeMapProvider {
        final LockFreeMap<Key, Value> map;

        public LockFreeMapProvider() {
            this.map = LockFreeMap.newMap(1000, 10);
            intr(1000).mapToObj(Key::new).forEach(k -> map.put(k, new Value(k.getKey())));
        }
    }

    @State(Scope.Benchmark)
    public static class JavaMapProvider {
        final ConcurrentMap<Key, Value> map;

        public JavaMapProvider() {
            this.map = new ConcurrentHashMap<>();
            intr(1000).mapToObj(Key::new).forEach(k -> map.put(k, new Value(k.getKey())));
        }
    }
}
