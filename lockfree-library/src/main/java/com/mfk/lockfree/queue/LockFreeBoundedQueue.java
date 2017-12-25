package com.mfk.lockfree.queue;

import java.io.IOException;
import java.util.Optional;

class LockFreeBoundedQueue<T> implements LockFreeQueue<T> {
    private final long size;

    LockFreeBoundedQueue(final long size) {
        this.size = size;
    }

    @Override
    public boolean add(T element) {
        return false;
    }

    @Override
    public Optional<T> poll() {
        return Optional.empty();
    }

    @Override
    public long size() {
        return size;
    }
}
