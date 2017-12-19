package com.mfk.lockfree.list;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by farhankhan on 12/16/17.
 */
class Fragment<T> {
    private final int size;
    private final Object[] elements;
    private final AtomicInteger atomicIndex = new AtomicInteger(0);
    private AtomicReference<Fragment<T>> nextRef = new AtomicReference<>();

    Fragment(final int size) {
        this.size = size;
        this.elements = new Object[size];
    }

    Fragment<T> createOrGetNextFragment() {
        if (nextRef.get() == null) {
            Fragment<T> newContainer = new Fragment<>(size);
            nextRef.compareAndSet(null, newContainer);
        }

        return nextRef.get();
    }

    Optional<Fragment<T>> getNextFragment() {
        return Optional.ofNullable(nextRef.get());
    }

    void setNextFragment(final Fragment<T> nextRef) {
        this.nextRef.set(nextRef);
    }

    boolean add(final T element) {
        try {
            elements[atomicIndex.getAndIncrement()] = element;
            return true;
        } catch (IndexOutOfBoundsException ignore) {
            return false;
        }
    }

    Optional<T> get(final int index) {
        return Optional.ofNullable(index >= size ? null : (T) elements[index]);
    }

    OptionalInt find(final T elementToFind) {
        return IntStream.range(0, size)
                .filter(i -> Objects.equals(elements[i], elementToFind))
                .findFirst();
    }

    void delete(final int index) {
        if (index < size) {
            T t = (T) elements[index];
            elements[index] = null;
        }
    }

    boolean isAllElementsNull() {
        return !Arrays.stream(elements)
                .filter(Objects::nonNull)
                .findFirst()
                .isPresent();
    }

    boolean isAppendable() {
        return atomicIndex.get() < size;
    }

    int getCurrentIndex() {
        return atomicIndex.get();
    }

    List<T> getNonNullElements() {
        return Arrays.stream(elements).filter(Objects::nonNull)
                .map(t -> (T) t)
                .collect(Collectors.toList());
    }
}
