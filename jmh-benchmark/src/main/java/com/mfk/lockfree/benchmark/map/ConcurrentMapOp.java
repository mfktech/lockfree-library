package com.mfk.lockfree.benchmark.map;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import static com.mfk.lockfree.util.Utils.intr;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toList;

public final class ConcurrentMapOp {

    public static void performConcurrentPut(final IntConsumer consumer, final int putCount) throws Exception {
        final int nThreads = 8;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        Runnable writer = () -> intr(putCount).forEach(consumer);
        CompletableFuture.allOf(intr(nThreads).mapToObj(i -> runAsync(writer, executorService))
                .toArray(CompletableFuture[]::new)).get();
        executorService.shutdown();
    }


    public static long performConcurrentGet(Function<Key, Value> func) throws Exception {
        final int nThreads = 8;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        final int times = 1_000_1000;

        Supplier<Long> supp = () -> intr(times)
                .map(i -> i % 1000)
                .mapToObj(Key::new)
                .map(func)
                .mapToLong(Value::getValue)
                .count();

        List<CompletableFuture<Long>> futures = intr(nThreads)
                .mapToObj(i -> CompletableFuture.supplyAsync(supp, executorService))
                .collect(toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[nThreads])).get();
        executorService.shutdown();

        return futures.stream().mapToLong(c -> {
            try {
                return c.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).sum();
    }

}
