package com.suda.domain.meal.service;

public final class QueryCounter {

    private static final ThreadLocal<Integer> QUERY_COUNT = ThreadLocal.withInitial(() -> 0);

    private QueryCounter() {
    }

    public static void reset() {
        QUERY_COUNT.set(0);
    }

    public static void increment() {
        QUERY_COUNT.set(QUERY_COUNT.get() + 1);
    }

    public static int getCount() {
        return QUERY_COUNT.get();
    }
}
