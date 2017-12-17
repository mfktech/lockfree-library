package com.mfk.lockfree.list;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by farhankhan on 12/16/17.
 */
class ListContainer<T> {
    private final int size;
    private final Object[] elements;
    private final AtomicInteger atomicIndex = new AtomicInteger(0);
    private final AtomicReference<ListContainer<T>> nextRef = new AtomicReference<>();

    ListContainer(final int size) {
        this.size = size;
        this.elements = new Object[size];
    }

    ListContainer<T> createOrGetNext() {
        if (nextRef.get() == null) {
            ListContainer<T> newContainer = new ListContainer<>(size);
            nextRef.compareAndSet(null, newContainer);
        }

        return nextRef.get();
    }

    boolean add(T element) {
        try {
            elements[atomicIndex.getAndIncrement()] = element;
            return true;
        } catch (IndexOutOfBoundsException ignore) {
            return false;
        }
    }

    Optional<ListContainer<T>> getNext() {
        return Optional.ofNullable(nextRef.get());
    }

    Optional<T> getAt(int index) {
        return Optional.ofNullable(index >= size ? null : (T) elements[index]);
    }
}
