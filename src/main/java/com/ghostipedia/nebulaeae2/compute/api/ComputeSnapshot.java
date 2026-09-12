package com.ghostipedia.nebulaeae2.compute.api;

import com.ghostipedia.nebulaeae2.compute.ComputeTuning;

public record ComputeSnapshot(
        long capacityCwut,
        long fundedCwut,
        long infrastructureReservedCwut,
        long craftingReservedCwut,
        long channelOverloadCwut,
        long channelDeviceCount) {

    public long reservedCwut() {
        return saturatingAdd(saturatingAdd(infrastructureReservedCwut, craftingReservedCwut), channelOverloadCwut);
    }

    public long shortfallCwut() {
        return Math.max(0, reservedCwut() - fundedCwut);
    }

    public long availableCraftingCwut() {
        return craftingPaused() ? 0 : Math.max(0, capacityCwut - reservedCwut());
    }

    public boolean craftingPaused() {
        return shortfallCwut() > 0;
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    public long channelDeviceReservationCwut() {
        return ComputeTuning.channelDeviceReservation(channelDeviceCount);
    }

    public long deviceScaleCwut() {
        return ComputeTuning.channelDeviceScaleReservation(channelDeviceCount);
    }
}
