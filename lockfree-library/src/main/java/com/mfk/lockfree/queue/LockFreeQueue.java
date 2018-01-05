package com.mfk.lockfree.queue;

import java.util.Optional;

/**
 * A lock-free queue which is highly optimized for concurrent readers and writers.
 *
 * @param <T> the type of contained elements.
 */
public interface LockFreeQueue<T> {
    static <E> LockFreeQueue<E> newQueue() {
        return new LockFreeLinkedArrayQueue<>(1000);
    }

    static <E> LockFreeQueue<E> newQueue(final int fragmentSize) {
        return new LockFreeLinkedArrayQueue<>(fragmentSize);
    }

    /**
     * @param element
     * @return
     */
    boolean add(T element);

    Optional<T> poll();

    long size();
}
