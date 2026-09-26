package com.ghostipedia.nebulaeae2.stockpile;

public final class StockpileRefillRules {
    private StockpileRefillRules() {}

    public static boolean shouldRefill(boolean refilling, long stored, long target, long gap) {
        if (target <= 0 || stored >= target) return false;
        long boundedGap = Math.clamp(gap, 0, target);
        return refilling || boundedGap == 0 || stored <= target - boundedGap;
    }
}
