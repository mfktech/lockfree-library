package com.mfk.lockfree.singleton;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * SingleRef is a thread-safe, non-blocking container of singleton which implements Singleton pattern using Atomic
 * classes.
 *
 * @param <T> the type of the contained object.
 */
public class SingleRef<T> {
    private final Supplier<T> supplier;
    private final AtomicBoolean lock = new AtomicBoolean(false);
    private volatile T instance;

    /**
     * Creates the singleton reference with the given supplier.
     * <p/>
     * The contained object will be instantiated using the {@code supplier} provided as the parameter. The Supplier
     * will be called lazily and exactly once to create the contained object and all subsequent calls to the {@code get}
     * method will return the same singleton objcet.
     *
     * @param supplier the supplier of the contained object which will be called by {@link #get()} method exactly once.
     */
    public SingleRef(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Gets the singleton contained object.
     * <p/>
     * The contained object will be instantiated using the {@code supplier} provided as the parameter
     * {@link #SingleRef(Supplier)}. The supplier will be called lazily and exactly once to create the contained
     * object and all subsequent calls to the {@code get} method will return the same singleton object.
     *
     * @return the singleton contained object.
     */
    public T get() {
        if (instance == null) {
            //noinspection StatementWithEmptyBody
            while (instance == null && !lock.compareAndSet(false, true)) {
                // parking area for other threads while the object is being instantiated, while at least one thread
                // will make it out during contention.
            }

            if (instance == null) {
                try {
                    instance = supplier.get();
                } finally {
                    // whether the Supplier threw an exception or the instance is created successfully, set the boolean
                    // to false to release the stuck threads in the while loop above.
                    lock.set(false);
                }
            }
        }

        return instance;
    }
}
