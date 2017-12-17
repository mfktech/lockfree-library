package com.mfk.lockfree.list;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class LockFreeConcurrentListImpl<T> implements LockFreeConcurrentList<T> {
    private final AtomicReference<ListNode<T>> headRef;
    private final AtomicReference<ListNode<T>> tailRef;
//    private final LongAdder longAdder;

    LockFreeConcurrentListImpl() {
        ListNode<T> nullObject = new ListNode<>(null);
        this.headRef = new AtomicReference<>(nullObject);
        this.tailRef = new AtomicReference<>(nullObject);
//        this.longAdder = new LongAdder();
    }

    @Override
    public ListCode append(T item) {
        if (item == null) return ListCode.NULL;

        ListNode<T> newNode = new ListNode<>(item);
        ListNode<T> curr = tailRef.get();

        while (curr.setNextIfNull(newNode) != newNode) {
            ListNode<T> nextNode = curr.getNext();
            if (nextNode != null) {
                curr = nextNode;
            }
        }

        tailRef.set(newNode);
//        longAdder.increment();
        return ListCode.SUCCESS;
    }

    @Override
    public ListCode delete(T item) {
        if (item == null) return ListCode.NULL;
        if (length() == 0) return ListCode.EMPTY_LIST;

        ListNode<T> prev = headRef.get();
        ListNode<T> curr = headRef.get().getNext();

        while (curr != null && curr.isActive()) {
            if (Objects.equals(curr.getValue(), item)) {
                curr.setStatusAsDeleted();
                ListNode<T> next = curr.getNext();

                if (next != null) {
                    prev.setNext(next);
                } else {
                    // FIXME: How do we update prev and tail pointer atomically
                }

                return ListCode.SUCCESS;
            }

            curr = curr.getNext();
        }

        return ListCode.NOT_FOUND;
    }

    @Override
    public long length() {
        return 0L;
    }

    @Override
    public Stream<T> stream() {
        Iterator<T> iterator = new Iterator<T>() {
            private ListNode<T> currNode = headRef.get();

            @Override
            public boolean hasNext() {
                return currNode.getNext() != null;
            }

            @Override
            public T next() {
                if (currNode.getNext() != null)
                    currNode = currNode.getNext();
                else
                    throw new RuntimeException("Next element does not exist.");

                return currNode.getValue();
            }
        };

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator,
                Spliterator.ORDERED | Spliterator.NONNULL), false);
    }
}
