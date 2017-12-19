package com.mfk.lockfree.list;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.*;

public class LockFreeConcurrentListImplTest {
    /**
     * Tests when only one element needs to be deleted.
     */
    @Test
    public void testDelete1() {
        LockFreeConcurrentList<Integer> list = new LockFreeConcurrentListImpl<>(100);
        IntStream.range(0, 10).forEach(e -> list.append(e));
        assertEquals(10, list.length());
        list.delete(2);
        assertEquals(9, list.length());
        assertEquals(Arrays.asList(0, 1, 3, 4, 5, 6, 7, 8, 9), list.stream().collect(toList()));
    }

    /**
     * Tests when mid fragment needs to be deleted.
     */
    @Test
    public void testDelete2() {
        LockFreeConcurrentList<Integer> list = new LockFreeConcurrentListImpl<>(100);
        IntStream.range(0, 250).forEach(e -> list.append(e));
        assertEquals(250, list.length());
        IntStream.range(80, 210).forEach(e -> list.delete(e));
        assertEquals(120, list.length());
        List<Integer> verifyList = Stream.concat(IntStream.range(0, 80).boxed(),
                IntStream.range(210, 250).boxed())
                .collect(toList());
        assertEquals(verifyList, list.stream().collect(toList()));

        Fragment<Integer> head = ((LockFreeConcurrentListImpl) list).getHead();
        assertEquals(101, head.getCurrentIndex());
        assertEquals(IntStream.range(0, 80).boxed().collect(toList()), head.getNonNullElements());
        assertTrue(head.getNextFragment().isPresent());

        Fragment<Integer> next = head.getNextFragment().get();
        assertEquals(50, next.getCurrentIndex());
        assertEquals(IntStream.range(210, 250).boxed().collect(toList()), next.getNonNullElements());
        assertFalse(next.getNextFragment().isPresent());
    }

    /**
     * Tests when tail needs to be deleted.
     */
    @Test
    public void testDelete3() {
        LockFreeConcurrentList<Integer> list = new LockFreeConcurrentListImpl<>(100);
        IntStream.range(0, 300).forEach(e -> list.append(e));
        assertEquals(300, list.length());
        IntStream.range(200, 300).forEach(e -> list.delete(e));
        assertEquals(200, list.length());
        List<Integer> verifyList = IntStream.range(0, 200).boxed().collect(toList());
        assertEquals(verifyList, list.stream().collect(toList()));

        Fragment<Integer> head = ((LockFreeConcurrentListImpl) list).getHead();
        assertEquals(101, head.getCurrentIndex());
        assertEquals(IntStream.range(0, 100).boxed().collect(toList()), head.getNonNullElements());
        assertTrue(head.getNextFragment().isPresent());

        Fragment<Integer> next = head.getNextFragment().get();
        assertEquals(101, next.getCurrentIndex());
        assertEquals(IntStream.range(100, 200).boxed().collect(toList()), next.getNonNullElements());
        assertTrue(next.getNextFragment().isPresent());

        Fragment<Integer> next1 = next.getNextFragment().get();
        assertEquals(0, next1.getCurrentIndex());
        assertEquals(new ArrayList<>(), next1.getNonNullElements());
        assertFalse(next1.getNextFragment().isPresent());
    }

    /**
     * Tests when tail needs to be deleted.
     */
    @Test
    public void testDelete4() {
        LockFreeConcurrentList<Integer> list = new LockFreeConcurrentListImpl<>(100);
        IntStream.range(0, 250).forEach(e -> list.append(e));
        assertEquals(250, list.length());
        IntStream.range(200, 250).forEach(e -> list.delete(e));
        assertEquals(200, list.length());
        List<Integer> verifyList = IntStream.range(0, 200).boxed().collect(toList());
        assertEquals(verifyList, list.stream().collect(toList()));

        Fragment<Integer> head = ((LockFreeConcurrentListImpl) list).getHead();
        assertEquals(101, head.getCurrentIndex());
        assertEquals(IntStream.range(0, 100).boxed().collect(toList()), head.getNonNullElements());
        assertTrue(head.getNextFragment().isPresent());

        Fragment<Integer> next = head.getNextFragment().get();
        assertEquals(101, next.getCurrentIndex());
        assertEquals(IntStream.range(100, 200).boxed().collect(toList()), next.getNonNullElements());
        assertTrue(next.getNextFragment().isPresent());

        Fragment<Integer> next1 = next.getNextFragment().get();
        assertEquals(50, next1.getCurrentIndex());
        assertEquals(new ArrayList<>(), next1.getNonNullElements());
        assertFalse(next1.getNextFragment().isPresent());
    }

    /**
     * Tests when head and tail both needs to be deleted.
     */
    @Test
    public void testDelete5() {
        LockFreeConcurrentList<Integer> list = new LockFreeConcurrentListImpl<>(100);
        IntStream.range(0, 100).forEach(e -> list.append(e));
        assertEquals(100, list.length());
        IntStream.range(0, 100).forEach(e -> list.delete(e));
        assertEquals(0, list.length());
        assertEquals(new ArrayList<>(), list.stream().collect(toList()));

        Fragment<Integer> head = ((LockFreeConcurrentListImpl) list).getHead();
        assertEquals(0, head.getCurrentIndex());
        assertEquals(new ArrayList<>(), head.getNonNullElements());
        assertFalse(head.getNextFragment().isPresent());
    }


    /**
     * Tests when head needs to be deleted
     */
    @Test
    public void testDelete6() {
        LockFreeConcurrentList<Integer> list = new LockFreeConcurrentListImpl<>(100);
        IntStream.range(0, 300).forEach(e -> list.append(e));
        assertEquals(300, list.length());
        IntStream.range(0, 100).forEach(e -> list.delete(e));
        assertEquals(200, list.length());
        List<Integer> verifyList = IntStream.range(100, 300).boxed().collect(toList());
        assertEquals(verifyList, list.stream().collect(toList()));

        Fragment<Integer> head = ((LockFreeConcurrentListImpl) list).getHead();
        assertEquals(101, head.getCurrentIndex());
        assertEquals(IntStream.range(100, 200).boxed().collect(toList()), head.getNonNullElements());
        assertTrue(head.getNextFragment().isPresent());

        Fragment<Integer> next = head.getNextFragment().get();
        assertEquals(100, next.getCurrentIndex());
        assertEquals(IntStream.range(200, 300).boxed().collect(toList()), next.getNonNullElements());
        assertFalse(next.getNextFragment().isPresent());
    }

    @Test
    public void testAppendMultiThreaded() throws ExecutionException, InterruptedException {
        LockFreeConcurrentList<String> lockFreeConcurrentList = new LockFreeConcurrentListImpl<>();

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
            count++;
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
