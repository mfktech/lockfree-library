package com.mfk.lockfree.list;

import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LockFreeConcurrentListImpl<T> implements LockFreeConcurrentList<T> {
    private final int fragmentSize;
    private final LongAdder length = new LongAdder();
    private Fragment<T> head;
    private Fragment<T> tail;

    public LockFreeConcurrentListImpl() {
        this(1000);
    }

    LockFreeConcurrentListImpl(final int fragmentSize) {
        final Fragment<T> fragment = new Fragment<>(fragmentSize);
        this.fragmentSize = fragmentSize;
        this.head = fragment;
        this.tail = fragment;
    }

    @Override
    public ListCode append(T t) {
        if (t == null) return ListCode.NULL;

        while (!tail.add(t)) {
            this.tail = tail.createOrGetNextFragment();
        }

        length.increment();
        return ListCode.SUCCESS;
    }

    @Override
    public ListCode delete(T element) {
        return delete(element, null);
    }

    @Override
    public ListCode delete(final T t, final Consumer<T> consumer) {
        Optional<Fragment<T>> optFragment = Optional.of(head);
        Optional<Fragment<T>> optPrev = Optional.empty();

        while (optFragment.isPresent()) {
            Fragment<T> thisFragment = optFragment.get();
            OptionalInt optIndex = thisFragment.find(t);

            if (optIndex.isPresent()) {
                Optional<T> optElem = thisFragment.get(optIndex.getAsInt());
                thisFragment.delete(optIndex.getAsInt());
                deleteFragment(optPrev, optFragment);
                length.decrement();
                useConsumer(consumer, optElem);
                return ListCode.SUCCESS;
            }

            optPrev = optFragment;
            optFragment = thisFragment.getNextFragment();
        }

        return ListCode.NOT_FOUND;
    }

    @Override
    public long length() {
        return length.longValue();
    }

    @Override
    public Stream<T> stream() {
        Iterator<T> iterator = new Iterator<T>() {
            private Fragment<T> fragment = head;
            private int index;

            @Override
            public boolean hasNext() {
                while (!fragment.get(index).isPresent()) {
                    if (index >= fragmentSize) {
                        Optional<Fragment<T>> optNextFragment = fragment.getNextFragment();
                        if (optNextFragment.isPresent()) {
                            fragment = optNextFragment.get();
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
                Optional<T> optElement = fragment.get(index++);

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

    Fragment<T> getHead() {
        return head;
    }

    private void deleteFragment(Optional<Fragment<T>> optPrev, Optional<Fragment<T>> optFragmentToDelete) {
        if (optFragmentToDelete.isPresent()) {
            Fragment<T> thisFragment = optFragmentToDelete.get();
            Optional<Fragment<T>> optNextFragment = thisFragment.getNextFragment();

            if (thisFragment.isAllElementsNull() && !thisFragment.isAppendable()) {
                // if all elements in the given fragment are null and if new elements cannot be appended
                if (optNextFragment.isPresent()) {
                    // this means that this fragment is not tail
                    rewireFragments(optPrev, optNextFragment);
                } else {
                    // we are dealing with tail, just create or get next fragment
                    Fragment<T> nextFragment = thisFragment.createOrGetNextFragment();
                    rewireFragments(optPrev, Optional.of(nextFragment));
                }
            }
        }
    }

    private void rewireFragments(Optional<Fragment<T>> optPrev, Optional<Fragment<T>> optNextFragment) {
        Fragment<T> nextFragment = optNextFragment.get();
        if (optPrev.isPresent()) {
            optPrev.get().setNextFragment(nextFragment);
        } else {
            head = nextFragment;
        }
    }

    private void useConsumer(Consumer<T> consumer, Optional<T> optElem) {
        if (consumer != null) {
            try {
                if (optElem.isPresent()) consumer.accept(optElem.get());
            } catch (Throwable ignore) {
            }
        }
    }
}
