package com.mfk.lockfree.list;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by farhankhan on 12/12/17.
 */
class ListNode<T> {
    private final T value;
    private final AtomicReference<ListNode<T>> nextRef = new AtomicReference<>();
    private Status status = Status.ACTIVE;

    ListNode(T value) {
        this.value = value;
    }

    /**
     * Sets the next pointer atomically, if not set already. If the next pointer is not null then that pointer will be
     * returned. If the next pointer is null and the given pointer becomes the next pointer then the given pointer will
     * be returned.
     *
     * @param next the next pointer to set.
     * @return If the next pointer is not null then that pointer will be returned. If the next pointer is null and the
     * given pointer becomes the next pointer then the given pointer will be returned.
     */
    ListNode<T> setNextIfNull(final ListNode<T> next) {
        return nextRef.compareAndSet(null, next) ? next : nextRef.get();
    }

    /**
     * Gets the next pointer of this node.
     *
     * @return gets the next pointer of this node as {@code Optional} object.
     */
    ListNode<T> getNext() {
        return nextRef.get();
    }

    /**
     * Sets the next pointer.
     *
     * @param next the pointer to set as next
     */
    void setNext(final ListNode<T> next) {
        this.nextRef.set(next);
    }

    boolean isActive() {
        return status == Status.ACTIVE;
    }

    void setStatusAsDeleted() {
        this.status = Status.DELETED;
    }

    /**
     * @return
     */
    T getValue() {
        return value;
    }

    private static enum Status {
        ACTIVE, DELETED;
    }
}
