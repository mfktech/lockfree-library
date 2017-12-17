package com.mfk.lockfree.list;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertEquals;

public class LockFreeConcurrentListImplTest {
    @Test
    public void testAppendSingleThreaded() {
        LockFreeConcurrentList<Integer> lockFreeConcurrentList = new LockFreeConcurrentComboList<>();

        IntStream.range(0, 150).forEach(i -> lockFreeConcurrentList.append(i));
        System.out.println("Count: " + lockFreeConcurrentList.stream().count());
//        assertEquals(5, lockFreeConcurrentList.length());
//        assertEquals(Arrays.asList(1, 2, 3, 4, 5), lockFreeConcurrentList.stream().collect(toList()));
    }

    @Test
    public void testAppendMultiThreaded() throws ExecutionException, InterruptedException {
        LockFreeConcurrentList<String> lockFreeConcurrentList = new LockFreeConcurrentComboList<>();

        CompletableFuture<Void> fut1 = createFuture("T1", lockFreeConcurrentList);
        CompletableFuture<Void> fut2 = createFuture("T2", lockFreeConcurrentList);
        CompletableFuture<Void> fut3 = createFuture("T3", lockFreeConcurrentList);
        CompletableFuture<Void> fut4 = createFuture("T4", lockFreeConcurrentList);

        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(fut1, fut2, fut3, fut4);
        completableFuture.get();

        System.out.println("Count: " + lockFreeConcurrentList.stream().count());
    }

    @Test
    public void testAppendMultiThreadedWithSynchronizedList() throws ExecutionException, InterruptedException {
        List<String> list = Collections.synchronizedList(new LinkedList<>());

        CompletableFuture<Void> fut1 = createFutureWithList("T1", list);
        CompletableFuture<Void> fut2 = createFutureWithList("T2", list);
        CompletableFuture<Void> fut3 = createFutureWithList("T3", list);
        CompletableFuture<Void> fut4 = createFutureWithList("T4", list);

        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(fut1, fut2, fut3, fut4);
        completableFuture.get();
        long count = 0;

        for (String s : list) {
            count ++;
        }

        System.out.println("Count: " + count);
    }

    private CompletableFuture<Void> createFuture(final String prefix, final LockFreeConcurrentList<String> lockFreeConcurrentList) {
        return CompletableFuture.runAsync(() ->
                IntStream.range(0, 1000000).forEach(i -> lockFreeConcurrentList.append(prefix + ":" + i))
        );
    }

    private CompletableFuture<Void> createFutureWithList(final String prefix, final List<String> list) {
        return CompletableFuture.runAsync(() ->
                IntStream.range(0, 1000000).forEach(i -> list.add(prefix + ":" + i))
        );
    }
}
