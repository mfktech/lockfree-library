package com.mfk.lockfree.queue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

class Entry<T> {
    private final AtomicReference<T> elementRef;
    private final AtomicReference<Entry> nextRef;

    Entry(final T element) {
        this.elementRef = new AtomicReference<>(element);
        this.nextRef = new AtomicReference<>();
    }

    Optional<T> getElement() {
        return Optional.ofNullable(elementRef.get());
    }

    Optional<Entry<T>> getNext() {
        return Optional.ofNullable(nextRef.get());
    }

    Entry<T> setNextIfNull(Entry<T> nextEntry) {
        if (getNext().isPresent()) return nextRef.get();
        return nextRef.compareAndSet(null, nextEntry) ? nextEntry : nextRef.get();
    }
}
