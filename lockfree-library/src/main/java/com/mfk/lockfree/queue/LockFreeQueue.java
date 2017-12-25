package com.mfk.lockfree.queue;

import java.io.Closeable;
import java.util.Optional;

/**
 * A lock-free queue which is highly optimized for concurrent readers and writers.
 *
 * @param <T> the type of contained elements.
 */
public interface LockFreeQueue<T> {
    static <E> LockFreeQueue<E> newUnboundedQueue() {
        return new LockFreeUnboundedQueue<>();
    }

    static <E> LockFreeQueue<E> newBoundedQueue(final long size) {
        return new LockFreeBoundedQueue<>(size);
    }

    /**
     * @param element
     * @return
     */
    boolean add(T element);

    Optional<T> poll();

    long size();
}
