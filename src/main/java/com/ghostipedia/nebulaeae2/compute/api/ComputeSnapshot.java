package com.ghostipedia.nebulaeae2.compute.api;

public record ComputeSnapshot(
        long capacityCwut,
        long reservedCwut,
        long channelOverloadCwut,
        long workBudgetCwut,
        long workUsedCwut,
        double recentWorkAverageCwut,
        long recentWorkPeakCwut,
        long recoveryBudgetCwut,
        long recoveryUsedCwut,
        long debtCwu,
        int sourceCount,
        int trackedNodeCount,
        long recentThrottledOperations,
        long recentRecoveryOperations) {

    public long workCeilingCwut() {
        return Math.max(0, capacityCwut - reservedCwut);
    }

    public long availableCwut() {
        return Math.max(0, workBudgetCwut - workUsedCwut);
    }
}
