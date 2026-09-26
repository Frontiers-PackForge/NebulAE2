package com.ghostipedia.nebulaeae2.optimizer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class OptimizerPatternOrder {
    private OptimizerPatternOrder() {}

    public static <T> List<T> sort(Collection<Option<T>> options) {
        return options.stream().sorted(Comparator.comparingInt((Option<T> option) -> option.priority()).reversed())
                .map(Option::pattern).distinct().toList();
    }

    public record Option<T>(T pattern, int priority) {}
}
