package com.ghostipedia.nebulaeae2.crafting.api;

import com.ghostipedia.nebulaeae2.crafting.CraftingJobState;

import org.jetbrains.annotations.Nullable;

public interface ICraftingCpuReservation {

    @Nullable
    CraftingJobState nebulae$getJobState();

    long nebulae$getReservationCwut();

    boolean nebulae$isComputePaused();
}
