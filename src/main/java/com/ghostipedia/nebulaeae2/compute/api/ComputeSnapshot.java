package com.ghostipedia.nebulaeae2.compute.api;

import com.ghostipedia.nebulaeae2.compute.ComputeTuning;

public record ComputeSnapshot(
        long capacityCwut,
        long fundedCwut,
        long reservedCwut,
        long passiveShortfallCwut,
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
        long channelDeviceCount,
        long recentThrottledOperations,
        long recentRecoveryOperations) {

    public long channelDeviceReservationCwut() {
        return ComputeTuning.channelDeviceReservation(channelDeviceCount);
    }

    public long deviceScaleCwut() {
        return ComputeTuning.channelDeviceScaleReservation(channelDeviceCount);
    }
}
