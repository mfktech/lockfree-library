package com.mfk.lockfree.queue;

import java.util.Optional;

public class LockFreeUnboundedQueueImpl<T> implements LockFreeQueue<T> {
    private Container<T> container;

    @Override
    public boolean add(T element) {
        return false;
    }

    @Override
    public Optional<T> poll() {
        return Optional.empty();
    }

    @Override
    public Optional<T> peek() {
        return Optional.empty();
    }

    @Override
    public long size() {
        return 0;
    }
}
