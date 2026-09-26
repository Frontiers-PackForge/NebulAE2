package com.ghostipedia.nebulaeae2.sticky;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.List;

import appeng.api.stacks.GenericStack;

public interface StickyPartitionGuard {
    default void nebulae$protectStickyPartition(BooleanSupplier protectedPartition) {
        nebulae$protectStickyPartition(protectedPartition, () -> Integer.MAX_VALUE);
    }
    void nebulae$protectStickyPartition(BooleanSupplier protectedPartition, IntSupplier activeSlots);
    boolean nebulae$beginPartitionReplacement(List<GenericStack> replacement);
    void nebulae$endPartitionReplacement();
}
