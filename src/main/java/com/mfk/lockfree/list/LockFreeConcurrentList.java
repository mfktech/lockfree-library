package com.mfk.lockfree.list;

import java.util.stream.Stream;

/**
 * A linked list which provides thread-safe operations by using lock-free algorithms. It's one-way list in which
 * items can only be appended at the tail.
 *
 * @author farhankhan
 */
public interface LockFreeConcurrentList<T> {
    /**
     * Appends the item to the list, this operation is thread-safe and objects can be appended concurrently. Lock-free
     * algorithms (Atomic classes) are used to achieve thread-safety.
     * <p/>
     * The runtime analysis is O(1).
     *
     * @param t the item to add
     * @return the status of this operation, whether failed or succeeded.
     */
    ListCode append(T t);

    /**
     * Marks an object for deletion. For thread-safety reasons, the object will stay in the list therefore, it will not
     * improve the performance of iteration. Marked object will not be included in other list operations such as
     * iteration, length etc.
     * <p/>
     * The runtime analysis is O(n). Marking an element as deleted will not improve the performance of iteration.
     *
     * @param t the item to be deleted
     * @return the failure or success code
     */
    ListCode delete(T t);

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
