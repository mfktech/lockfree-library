package com.mfk.lockfree.queue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

class Entry<T> {
    private final T value;
    private final AtomicReference<Entry<T>> nextRef;
    private final AtomicReference<Status> statusRef;

    Entry(final T value) {
        this.value = value;
        this.nextRef = new AtomicReference<>();
        statusRef = new AtomicReference<>(Status.NEW);
    }

    Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    Optional<Entry<T>> getNext() {
        return Optional.ofNullable(nextRef.get());
    }

    Entry<T> setNextIfNull(Entry<T> nextEntry) {
        if (getNext().isPresent()) return nextRef.get();
        return nextRef.compareAndSet(null, nextEntry) ? nextEntry : nextRef.get();
    }

    boolean consume() {
        return statusRef.compareAndSet(Status.NEW, Status.CONSUMED);
    }

    enum Status {
        NEW, CONSUMED
    }
}
