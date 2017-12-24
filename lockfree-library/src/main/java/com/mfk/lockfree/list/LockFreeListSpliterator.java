package com.mfk.lockfree.list;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.LongStream;

public class LockFreeListSpliterator<T> implements Spliterator<T> {
    private final long max;
    private Fragment<T> head;
    private long min;
    private Fragment<T> fragment;
    private long currFragment;
    private int currElement = 0;

    public LockFreeListSpliterator(Fragment<T> head, long min, long max) {
        this.min = min;
        this.max = max;
        this.currFragment = min;
        this.fragment = head;
        this.head = head;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        Optional<T> advance = Optional.empty();

        while (!advance.isPresent()) {
            if (currElement < fragment.getMaxSize()) {
                advance = fragment.get(currElement++);
            } else if (currFragment < max) {
                Optional<Fragment<T>> nextFragment = fragment.getNextFragment();
                if (!nextFragment.isPresent()) break;
                this.fragment = nextFragment.get();
                currFragment++;
                currElement = 0;
                advance = fragment.get(currElement++);
            } else {
                break;
            }
        }

        if (advance.isPresent()) {
            action.accept(advance.get());
            return true;
        } else return false;
    }

    @Override
    public Spliterator<T> trySplit() {
        if (max == min) return null;

        long mid = (max - min) / 2;
        long newMin = min;
        long newMax = mid + min;
        Fragment<T> newHead = head;

        Optional<Fragment<T>> optFragment = LongStream.rangeClosed(newMin, newMax)
                .mapToObj(l -> this.fragment.getNextFragment())
                .peek(System.out::println)
                .flatMap(Optional::stream)
                .peek(System.out::println)
                .reduce((a, b) -> b);

        if (optFragment.isPresent()) {
            this.min = newMax + 1;
            this.head = optFragment.get();
            this.fragment = this.head;
            return new LockFreeListSpliterator<>(newHead, newMin, newMax);
        }

        return null;
    }

    @Override
    public long estimateSize() {
        return (max - min) + 1;
    }

    @Override
    public int characteristics() {
        return Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
    }
}
