package com.ghostipedia.nebulaeae2.p2p;

import java.util.List;
import java.util.function.Predicate;

public final class PatternOutputSelector<T> {
    private T previous;

    public T select(List<T> ordered, Predicate<T> accepts) {
        if (ordered.isEmpty()) {
            return null;
        }
        int start = previous == null ? 0 : Math.floorMod(ordered.indexOf(previous) + 1, ordered.size());
        for (int offset = 0; offset < ordered.size(); offset++) {
            T candidate = ordered.get((start + offset) % ordered.size());
            if (accepts.test(candidate)) {
                previous = candidate;
                return candidate;
            }
        }
        return null;
    }
}
