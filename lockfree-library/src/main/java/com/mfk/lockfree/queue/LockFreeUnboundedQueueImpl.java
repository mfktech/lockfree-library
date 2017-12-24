package com.mfk.lockfree.queue;

import java.util.Optional;

public class LockFreeUnboundedQueueImpl<T> implements LockFreeQueue<T> {
    private Entry<T> head;
    private Entry<T> tail;

    public LockFreeUnboundedQueueImpl() {
        this.head = new Entry<>(null);
        this.tail = this.head;
    }

    @Override
    public boolean add(T element) {
        Entry<T> newEntry = new Entry<>(element);
        Entry<T> curr = this.head;
        Entry<T> next;

        while ((next = curr.setNextIfNull(newEntry)) != newEntry) {
            curr = next;
        }

        // setting tail may be "stale" as other threads may have appended more elements meanwhile,
        // but the while loop above will make sure that the new elements are always appended at the end.
        // Also, the tail pointer doesn't need to be volatile because even if any thread cached the pointer,
        // the while loop above would make sure to append the new elements to the up-to-dated tail pointer.
        // In case of highly concurrent environment, the tail pointer would always be catching up.
        this.tail = newEntry;
        return true;
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
