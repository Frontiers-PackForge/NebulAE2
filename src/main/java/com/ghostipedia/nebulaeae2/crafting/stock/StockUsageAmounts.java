package com.ghostipedia.nebulaeae2.crafting.stock;

public record StockUsageAmounts(long used, long missing, long available) {
    public long required() {
        return missing > Long.MAX_VALUE - used ? Long.MAX_VALUE : used + missing;
    }

    public double percentage() {
        return available > 0 ? required() * 100.0 / available : Double.NaN;
    }
}
