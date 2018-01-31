package com.mfk.lockfree.singleton;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mfk.lockfree.util.Utils.intr;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class SingleRefTest {
    @Test
    public void testGet1() {
        SingleRef<HeavyObject> singleRef = new SingleRef<>(HeavyObject::new);
        HeavyObject singletonObj = singleRef.get();
        assertNotNull(singletonObj);
    }

    @Test
    public void testGet2() {
        intr(100).forEach(i -> testSingletonWithMultiThreads(100));
        intr(100).forEach(i -> testSingletonWithMultiThreads(Runtime.getRuntime().availableProcessors()));
    }

    private void testSingletonWithMultiThreads(final int nThreads) {
        final HeavyObject singletonObj = new HeavyObject();
        final SingleRef<HeavyObject> singletonRef = new SingleRef<>(() -> singletonObj.inc());
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        List<CompletableFuture<HeavyObject>> futures = intr(nThreads)
                .mapToObj(j -> CompletableFuture.supplyAsync(singletonRef::get, executorService))
                .collect(Collectors.toList());

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[nThreads])).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        futures.forEach(f -> assertEquals(1, getSilently(f).atomicInteger.get()));
        executorService.shutdown();
    }

    private HeavyObject getSilently(CompletableFuture<HeavyObject> f) {
        try {
            return f.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class HeavyObject {
        private AtomicInteger atomicInteger = new AtomicInteger();

        HeavyObject inc() {
            atomicInteger.incrementAndGet();
            return this;
        }
    }
}
