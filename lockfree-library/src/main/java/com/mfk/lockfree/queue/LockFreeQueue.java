package com.mfk.lockfree.queue;

import java.util.Optional;

/**
 * A lock-free queue which is highly optimized for concurrent readers and writers.
 *
 * @param <T> the type of contained elements.
 */
public interface LockFreeQueue<T> {
    static <E> LockFreeQueue<E> newUnboundedQueue() {
        return new LockFreeLinkedArrayQueue<>(1000);
    }

    /**
     * @param element
     * @return
     */
    boolean add(T element);

    Optional<T> poll();

    long size();
}
