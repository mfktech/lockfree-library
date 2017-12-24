package com.mfk.lockfree.list;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.*;

public class LockFreeListImplTest {
    /**
     * Tests when only one element needs to be deleted.
     */
    @Test
    public void testRemove1() {
        LockFreeList<Integer> list = new LockFreeListImpl<>(100);
        IntStream.range(0, 10).forEach(e -> list.append(e));
        assertEquals(10, list.size());
        list.remove(2);
        assertEquals(9, list.size());
        assertEquals(Arrays.asList(0, 1, 3, 4, 5, 6, 7, 8, 9), list.stream().parallel().collect(toList()));
    }

    /**
     * Tests when mid fragment needs to be deleted.
     */
    @Test
    public void testRemove2() {
        LockFreeList<Integer> list = new LockFreeListImpl<>(100);
        IntStream.range(0, 250).forEach(e -> list.append(e));
        assertEquals(250, list.size());
        IntStream.range(80, 210).forEach(e -> list.remove(e));
        assertEquals(120, list.size());
        List<Integer> verifyList = Stream.concat(IntStream.range(0, 80).boxed(),
                IntStream.range(210, 250).boxed())
                .collect(toList());
        assertEquals(verifyList, list.stream().collect(toList()));

        Fragment<Integer> head = ((LockFreeListImpl) list).getHead();
        assertEquals(101, head.getCurrentIndex());
        assertEquals(IntStream.range(0, 80).boxed().collect(toList()), head.getAll());
        assertTrue(head.getNextFragment().isPresent());

        Fragment<Integer> next = head.getNextFragment().get();
        assertEquals(50, next.getCurrentIndex());
        assertEquals(IntStream.range(210, 250).boxed().collect(toList()), next.getAll());
        assertFalse(next.getNextFragment().isPresent());
    }

    /**
     * Tests when tail needs to be deleted.
     */
    @Test
    public void testRemove3() {
        LockFreeList<Integer> list = new LockFreeListImpl<>(100);
        IntStream.range(0, 300).forEach(e -> list.append(e));
        assertEquals(300, list.size());
        IntStream.range(200, 300).forEach(e -> list.remove(e));
        assertEquals(200, list.size());
        List<Integer> verifyList = IntStream.range(0, 200).boxed().collect(toList());
        assertEquals(verifyList, list.stream().collect(toList()));

        Fragment<Integer> head = ((LockFreeListImpl) list).getHead();
        assertEquals(101, head.getCurrentIndex());
        assertEquals(IntStream.range(0, 100).boxed().collect(toList()), head.getAll());
        assertTrue(head.getNextFragment().isPresent());

        Fragment<Integer> next = head.getNextFragment().get();
        assertEquals(101, next.getCurrentIndex());
        assertEquals(IntStream.range(100, 200).boxed().collect(toList()), next.getAll());
        assertTrue(next.getNextFragment().isPresent());

        Fragment<Integer> next1 = next.getNextFragment().get();
        assertEquals(0, next1.getCurrentIndex());
        assertEquals(new ArrayList<>(), next1.getAll());
        assertFalse(next1.getNextFragment().isPresent());
    }

    /**
     * Tests when tail needs to be deleted.
     */
    @Test
    public void testRemove4() {
        LockFreeList<Integer> list = new LockFreeListImpl<>(100);
        IntStream.range(0, 250).forEach(e -> list.append(e));
        assertEquals(250, list.size());
        IntStream.range(200, 250).forEach(e -> list.remove(e));
        assertEquals(200, list.size());
        List<Integer> verifyList = IntStream.range(0, 200).boxed().collect(toList());
        assertEquals(verifyList, list.stream().collect(toList()));

        Fragment<Integer> head = ((LockFreeListImpl) list).getHead();
        assertEquals(101, head.getCurrentIndex());
        assertEquals(IntStream.range(0, 100).boxed().collect(toList()), head.getAll());
        assertTrue(head.getNextFragment().isPresent());

        Fragment<Integer> next = head.getNextFragment().get();
        assertEquals(101, next.getCurrentIndex());
        assertEquals(IntStream.range(100, 200).boxed().collect(toList()), next.getAll());
        assertTrue(next.getNextFragment().isPresent());

        Fragment<Integer> next1 = next.getNextFragment().get();
        assertEquals(50, next1.getCurrentIndex());
        assertEquals(new ArrayList<>(), next1.getAll());
        assertFalse(next1.getNextFragment().isPresent());
    }

    /**
     * Tests when head and tail both needs to be deleted.
     */
    @Test
    public void testRemove5() {
        LockFreeList<Integer> list = new LockFreeListImpl<>(100);
        IntStream.range(0, 100).forEach(e -> list.append(e));
        assertEquals(100, list.size());
        IntStream.range(0, 100).forEach(e -> list.remove(e));
        assertEquals(0, list.size());
        assertEquals(new ArrayList<>(), list.stream().collect(toList()));

        Fragment<Integer> head = ((LockFreeListImpl) list).getHead();
        assertEquals(0, head.getCurrentIndex());
        assertEquals(new ArrayList<>(), head.getAll());
        assertFalse(head.getNextFragment().isPresent());
    }


    /**
     * Tests when head needs to be deleted
     */
    @Test
    public void testRemove6() {
        LockFreeList<Integer> list = new LockFreeListImpl<>(100);
        IntStream.range(0, 300).forEach(e -> list.append(e));
        assertEquals(300, list.size());
        IntStream.range(0, 100).forEach(e -> list.remove(e));
        assertEquals(200, list.size());
        List<Integer> verifyList = IntStream.range(100, 300).boxed().collect(toList());
        assertEquals(verifyList, list.stream().collect(toList()));

        Fragment<Integer> head = ((LockFreeListImpl) list).getHead();
        assertEquals(101, head.getCurrentIndex());
        assertEquals(IntStream.range(100, 200).boxed().collect(toList()), head.getAll());
        assertTrue(head.getNextFragment().isPresent());

        Fragment<Integer> next = head.getNextFragment().get();
        assertEquals(100, next.getCurrentIndex());
        assertEquals(IntStream.range(200, 300).boxed().collect(toList()), next.getAll());
        assertFalse(next.getNextFragment().isPresent());
    }

//
//    @Test
//    public void testAppendMultiThreaded() throws ExecutionException, InterruptedException {
//    }
//
//    @Test
//    public void testAppendMultiThreadedWithSynchronizedList() throws ExecutionException, InterruptedException {
//        List<String> list = Collections.synchronizedList(new LinkedList<>());
//
//        CompletableFuture<Void> fut1 = createFutureWithList("T1", list);
//        CompletableFuture<Void> fut2 = createFutureWithList("T2", list);
//        CompletableFuture<Void> fut3 = createFutureWithList("T3", list);
//        CompletableFuture<Void> fut4 = createFutureWithList("T4", list);
//
//        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(fut1, fut2, fut3, fut4);
//        completableFuture.get();
//        long count = 0;
//
//        System.out.println("Count: " + list.stream().count());
//    }
//
//
//    private CompletableFuture<Void> createFutureWithList(final String prefix, final List<String> list) {
//        return CompletableFuture.runAsync(() ->
//                IntStream.range(0, 1000000).forEach(i -> list.add(prefix + ":" + i))
//        );
//    }
}
