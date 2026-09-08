package com.ghostipedia.nebulaeae2.crafting.stock;

import appeng.api.networking.crafting.ICraftingPlan;

public final class StockUsageSubmission {
    private static final ThreadLocal<StockUsageSubmission> CURRENT = new ThreadLocal<>();
    private final ICraftingPlan plan;
    private StockUsageSnapshot snapshot = StockUsageSnapshot.EMPTY;

    public StockUsageSubmission(ICraftingPlan plan) {
        this.plan = plan;
    }

    public static StockUsageSubmission set(StockUsageSubmission submission) {
        var previous = CURRENT.get();
        if (submission == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(submission);
        }
        return previous;
    }

    public static void captured(ICraftingPlan plan, StockUsageSnapshot snapshot) {
        var current = CURRENT.get();
        if (current != null && current.plan == plan) {
            current.snapshot = snapshot;
        }
    }

    public static StockUsageSnapshot consume(ICraftingPlan plan) {
        var current = CURRENT.get();
        if (current == null || current.plan != plan) {
            return StockUsageSnapshot.EMPTY;
        }
        var result = current.snapshot;
        current.snapshot = StockUsageSnapshot.EMPTY;
        return result;
    }
}
