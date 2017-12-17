package com.mfk.lockfree.list;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by farhankhan on 12/16/17.
 */
public class LockFreeConcurrentComboList<T> implements LockFreeConcurrentList<T> {
    private final int batch = 100;
    private ListContainer<T> headRef;
    private ListContainer<T> tailRef;

    public LockFreeConcurrentComboList() {
        ListContainer<T> listContainer = new ListContainer<>(batch);
        this.headRef = listContainer;
        this.tailRef = listContainer;
    }

    @Override
    public ListCode append(T t) {
        if (t == null) return ListCode.NULL;

        while (!tailRef.add(t)) {
            this.tailRef = tailRef.createOrGetNext();
        }

        return ListCode.SUCCESS;
    }

    @Override
    public ListCode delete(T t) {
        return null;
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public Stream<T> stream() {
        Iterator<T> iterator = new Iterator<T>() {
            private ListContainer<T> container = headRef;
            private int index;

            @Override
            public boolean hasNext() {
                while (!container.getAt(index).isPresent()) {
                    if (index >= batch) {
                        Optional<ListContainer<T>> nextOpt = container.getNext();
                        if (nextOpt.isPresent()) {
                            container = nextOpt.get();
                        } else {
                            return false;
                        }

                        index = 0;
                    } else {
                        index++;
                    }
                }

                return true;
            }

            @Override
            public T next() {
                Optional<T> optElement = container.getAt(index++);

                if (optElement.isPresent()) {
                    return optElement.get();
                } else {
                    throw new NoSuchElementException("Element not found.");
                }
            }
        };

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator,
                Spliterator.ORDERED | Spliterator.NONNULL), false);
    }
}
