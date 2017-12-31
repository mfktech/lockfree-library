package com.mfk.lockfree.queue;

import com.mfk.lockfree.list.LockFreeList;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

class LockFreeLinkedArrayQueue<T> implements LockFreeQueue<T> {
    private final LockFreeList<T> list;

    LockFreeLinkedArrayQueue(final int fragmentSize) {
        this.list = LockFreeList.newList(fragmentSize);
    }

    @Override
    public boolean add(T element) {
        return list.append(element);
    }

    @Override
    public Optional<T> poll() {
        return list.removeFirst();
    }

    @Override
    public long size() {
        return list.size();
    }
}
