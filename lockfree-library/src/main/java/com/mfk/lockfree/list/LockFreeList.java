package com.mfk.lockfree.list;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A list which provides thread-safe operations by using lock-free algorithms. It's one-way list in which items can only
 * be appended. Best for use-cases when there are many concurrent writers.
 *
 * @author fkhan
 */
public interface LockFreeList<T> {
    /**
     * Appends the given object to the list. The runtime analysis is O(1).
     *
     * @param element the item to add
     * @return the status of this operation, whether failed or succeeded.
     */
    ListCode append(final T element);

    /**
     * Removes the object from the list.
     * <p/>
     * The runtime analysis is O(n).
     *
     * @param element the item to be deleted
     * @return the failure or success code
     */
    ListCode delete(final T element);

    /**
     * Removes the object from the list.
     * <p/>
     * The runtime analysis is O(n).
     *
     * @param element  the item to be deleted
     * @param consumer the element removed from the list. This consumer gives the opportunity to do cleanup if needed.
     * @return the failure or success code
     */
    ListCode delete(final T element, final Consumer<T> consumer);

    /**
     * Gets the length of the list excluding the nodes marked for deletion.
     * <p/>
     * The runtime analysis is O(1) because the total number of items are maintained on every add/delete
     * operation.
     *
     * @return the total length of list.
     */
    long length();

    Stream<T> stream();
}
