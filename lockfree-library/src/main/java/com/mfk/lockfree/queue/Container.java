package com.mfk.lockfree.queue;

import java.util.concurrent.atomic.AtomicReference;

public class Container<T> {
    private final AtomicReference<T> elementRef;

    public Container(final T element) {
        this.elementRef = new AtomicReference<>(element);
    }
}
