package com.mfk.lockfree.list;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Created by farhankhan on 12/16/17.
 */
class Fragment<T> {
    private final int maxSize;
    private final Object[] elements;
    private final AtomicInteger tail = new AtomicInteger(0);
    private final AtomicInteger head = new AtomicInteger(0);
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
            elements[tail.getAndIncrement()] = element;
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

    IntStream find(final Predicate<T> predicate) {
        return IntStream.range(0, this.elements.length)
                .filter(i -> {
                    Object obj = elements[i];
                    return Objects.nonNull(obj) && predicate.test(castNonNullElement(obj));
                });
    }

    Optional<T> remove(final int index) {
        if (index < maxSize) {
            Object elem = elements[index];
            elements[index] = null;
            return optional(elem);
        }

        return optional(null);
    }

    Optional<T> removeFirst() {
        Optional<T> elem = Optional.empty();
        while (!elem.isPresent() && head.get() < elements.length && head.get() < tail.get()) {
            int localHead = head.get();
            if (head.compareAndSet(localHead, localHead + 1)) {
                elem = remove(localHead);
            }
        }

        return elem;
    }

    boolean isEmpty() {
        return Arrays.stream(elements).noneMatch(Objects::nonNull);
    }

    boolean isWritable() {
        return tail.get() < maxSize;
    }

    int getCurrentIndex() {
        return tail.get();
    }

    Optional<T> get(final int index) {
        return optional(index >= maxSize ? null : elements[index]);
    }

    int getMaxSize() {
        return maxSize;
    }

    List<T> getAll() {
        return Arrays.stream(elements).filter(Objects::nonNull)
                .map(this::castNonNullElement)
                .collect(toList());
    }

    private Optional<T> optional(Object element) {
        return Optional.ofNullable(castNonNullElement(element));
    }

    @SuppressWarnings("unchecked")
    private T castNonNullElement(Object element) {
        return element == null ? null : (T) element;
    }
}
