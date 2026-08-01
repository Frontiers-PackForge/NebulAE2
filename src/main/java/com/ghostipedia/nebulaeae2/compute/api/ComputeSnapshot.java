package com.ghostipedia.nebulaeae2.compute.api;

public record ComputeSnapshot(
        long capacityCwut,
        long reservedCwut,
        long workBudgetCwut,
        long workUsedCwut,
        long debtCwu,
        int sourceCount,
        int trackedNodeCount,
        long throttledOperations) {

    public long availableCwut() {
        return Math.max(0, workBudgetCwut - workUsedCwut);
    }
}
