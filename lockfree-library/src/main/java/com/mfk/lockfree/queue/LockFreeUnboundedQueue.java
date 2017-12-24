package com.mfk.lockfree.queue;

import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;

class LockFreeUnboundedQueue<T> implements LockFreeQueue<T> {
    private final LongAdder longAdder = new LongAdder();
    private Entry<T> head;
    private Entry<T> tail;

    LockFreeUnboundedQueue() {
        // initially head and tail points to empty/null entry
        this.head = new Entry<>(null);
        this.head.consume();
        this.tail = this.head;
    }

    @Override
    public boolean add(T element) {
        if (element == null) return false;
        Entry<T> newEntry = new Entry<>(element);
        Entry<T> curr = this.tail;
        Entry<T> next;

        while ((next = curr.setNextIfNull(newEntry)) != newEntry) {
            curr = next;
        }

        // the tail pointer may be "stale" here in case of concurrent writes,
        // but the while loop above will make sure that the new elements are always appended at the end.
        // the tail pointer doesn't need to be "volatile" for the same reason.
        this.tail = newEntry;
        this.longAdder.increment();
        return true;
    }

    @Override
    public Optional<T> poll() {
        Optional<Entry<T>> curr = Optional.of(head);

        while (curr.isPresent() && !curr.get().consume()) {
            curr = curr.get().getNext();
        }

        if (curr.isPresent()) {
            // the head pointer may be stale in case of concurrent polls by other threads, but since the list is
            // structurally immutable (apart from adding new entries), the while loop above will catch up.
            // the head pointer doesn't need to be "volatile" for the same reason.
            this.head = curr.get();
            this.longAdder.decrement();
            return curr.get().getValue();
        } else return Optional.empty();
    }

    @Override
    public long size() {
        return longAdder.longValue();
    }
}
