package com.mfk.lockfree.util;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Utils {
    private Utils() {
    }

    public static <T> T get(CompletableFuture<T> f) {
        try {
            return f.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static IntStream intr(int range) {
        return IntStream.range(0, range);
    }
}
