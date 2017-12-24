package com.mfk.lockfree.queue;

import java.util.Optional;

/**
 * A lock-free queue which is highly optimized for concurrent readers and writers.
 *
 * @param <T> the type of contained elements.
 */
public interface LockFreeQueue<T> {
    /**
     * @param element
     * @return
     */
    boolean add(T element);

    Optional<T> poll();

    Optional<T> peek();

    long size();
}
