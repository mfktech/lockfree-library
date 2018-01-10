package com.mfk.lockfree.list;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A list which provides highly-optimized, concurrent and thread-safe append operation by using lock-free algorithms
 * (Atomic Classes). Ideal for scenarios when data/objects from multiple sources need to be sinked concurrently to this
 * list.
 *
 * @author fkhan
 */
public interface LockFreeList<T> {
    /**
     * Static factory method to create new instance of the list with default configuration.
     *
     * @param <E> the type of list
     * @return newly created list
     */
    static <E> LockFreeList<E> newList() {
        return newList(1000);
    }

    /**
     * Static factory method to create new instance of the list.
     *
     * @param fragmentSize the size of the fragment
     * @param <E>          the type of list
     * @return newly created list
     */
    static <E> LockFreeList<E> newList(final int fragmentSize) {
        return new LockFreeLinkedArrayList<>(fragmentSize);
    }

    /**
     * Appends the given object to the list. The runtime analysis is O(1).
     *
     * @param element the item to add
     * @return the status of this operation, whether failed or succeeded.
     */
    boolean append(final T element);

    Stream<T> findAll(final Predicate<T> predicate);

    /**
     * Removes the object from the list in O(n).
     *
     * @param element the item to be deleted
     * @return the removed object if existed.
     */
    Optional<T> remove(final T element);

    Stream<T> removeAll(final Predicate<T> predicate);

    /**
     * Gets and removes the first element from the list.
     *
     * @return gets and removes the first element from the list.
     */
    Optional<T> removeFirst();

    /**
     * Gets the size of the list in O(n); equivalent to {@code list.stream().count()}.
     *
     * @return the total size of list.
     */
    long size();

    /**
     * Gets the stream of objects in the list.
     *
     * @return the stream of objects in the list.
     */
    Stream<T> stream();
}
