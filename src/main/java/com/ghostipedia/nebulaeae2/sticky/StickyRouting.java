package com.ghostipedia.nebulaeae2.sticky;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.function.ToLongBiFunction;

public final class StickyRouting {
    private StickyRouting() {}

    public static <T> long insert(Iterable<? extends Iterable<T>> priorityGroups, long amount,
                                 Predicate<T> removed, Predicate<T> claims, ToLongBiFunction<T, Long> insert) {
        var destinations = new ArrayList<T>();
        for (var group : priorityGroups) {
            for (var storage : group) {
                if (!removed.test(storage) && claims.test(storage)) {
                    destinations.add(storage);
                }
            }
        }
        if (destinations.isEmpty()) {
            return -1;
        }
        long remaining = amount;
        for (var storage : destinations) {
            if (remaining <= 0) {
                break;
            }
            if (!removed.test(storage)) {
                remaining -= insert.applyAsLong(storage, remaining);
            }
        }
        return amount - remaining;
    }
}
