package com.mfk.lockfree.list;

import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LockFreeListImpl<T> implements LockFreeList<T> {
    private final int maxFragmentSize;
    private final LongAdder total = new LongAdder();
    private final LongAdder removed = new LongAdder();
    private Fragment<T> head;
    private Fragment<T> tail;

    LockFreeListImpl(final int maxFragmentSize) {
        final Fragment<T> fragment = new Fragment<>(maxFragmentSize);
        this.maxFragmentSize = maxFragmentSize;
        this.head = fragment;
        this.tail = fragment;
    }

    @Override
    public boolean append(T t) {
        if (t == null) return false;
        while (!tail.add(t)) {
            this.tail = tail.createOrGetNextFragment();
        }

        total.increment();
        return true;
    }

    @Override
    public boolean remove(T element) {
        Optional<Fragment<T>> optFragment = Optional.of(head);
        Optional<Fragment<T>> optPrev = Optional.empty();

        while (optFragment.isPresent()) {
            Fragment<T> thisFragment = optFragment.get();
            OptionalInt optIndex = thisFragment.find(element);

            if (optIndex.isPresent()) {
                thisFragment.remove(optIndex.getAsInt());
                removeFragment(optPrev.orElse(null), thisFragment);
                removed.increment();
                return true;
            }

            optPrev = optFragment;
            optFragment = thisFragment.getNextFragment();
        }

        return false;

    }

    @Override
    public long size() {
        return total.longValue() - removed.longValue();
    }

    @Override
    public Stream<T> stream() {
        Iterator<T> iterator = new Iterator<>() {
            private Fragment<T> fragment = head;
            private int index;

            @Override
            public boolean hasNext() {
                while (!fragment.get(index).isPresent()) {
                    if (index >= maxFragmentSize) {
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
                LockFreeListSpliterator.ORDERED | LockFreeListSpliterator.NONNULL), false);
    }

    Fragment<T> getHead() {
        return head;
    }

    private void removeFragment(Fragment<T> prev, Fragment<T> thisFragment) {
        if (thisFragment != null) {
            Optional<Fragment<T>> optNextFragment = thisFragment.getNextFragment();

            if (thisFragment.isEmpty() && !thisFragment.isWritable()) {
                // if all elements in the given fragment are null and if new elements cannot be appended
                if (optNextFragment.isPresent()) {
                    // this means that this fragment is not tail
                    rewireFragments(prev, optNextFragment.get());
                } else {
                    // we are dealing with tail, just create or get next fragment
                    Fragment<T> nextFragment = thisFragment.createOrGetNextFragment();
                    rewireFragments(prev, nextFragment);
                }
            }
        }
    }

    private void rewireFragments(Fragment<T> prev, Fragment<T> nextFragment) {
        if (prev != null) {
            prev.setNextFragment(nextFragment);
        } else {
            head = nextFragment;
        }
    }
}
