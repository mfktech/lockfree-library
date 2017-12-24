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
    private final int maxSize;
    private final Object[] elements;
    private final AtomicInteger last = new AtomicInteger(0);
    private AtomicReference<Fragment<T>> nextRef = new AtomicReference<>();

    Fragment(final int maxSize) {
        this.maxSize = maxSize;
        this.elements = new Object[maxSize];
    }

    Fragment<T> createOrGetNextFragment() {
        if (nextRef.get() == null) {
            Fragment<T> newContainer = new Fragment<>(maxSize);
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
            elements[last.getAndIncrement()] = element;
            return true;
        } catch (IndexOutOfBoundsException ignore) {
            return false;
        }
    }

    OptionalInt find(final T elementToFind) {
        return IntStream.range(0, maxSize)
                .filter(i -> Objects.equals(elements[i], elementToFind))
                .findFirst();
    }

    void remove(final int index) {
        if (index < maxSize) {
            elements[index] = null;
        }
    }

    boolean isEmpty() {
        return Arrays.stream(elements).noneMatch(Objects::nonNull);
    }

    boolean isWritable() {
        return last.get() < maxSize;
    }

    int getCurrentIndex() {
        return last.get();
    }

    @SuppressWarnings("unchecked")
    Optional<T> get(final int index) {
        return Optional.ofNullable(index >= maxSize ? null : (T) elements[index]);
    }

    @SuppressWarnings("unchecked")
    List<T> getAll() {
        return Arrays.stream(elements).filter(Objects::nonNull)
                .map(t -> (T) t)
                .collect(Collectors.toList());
    }

    int getMaxSize() {
        return maxSize;
    }
}
