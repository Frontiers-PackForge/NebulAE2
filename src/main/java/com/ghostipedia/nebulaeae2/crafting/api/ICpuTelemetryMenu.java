package com.ghostipedia.nebulaeae2.crafting.api;
import com.ghostipedia.nebulaeae2.crafting.CpuTelemetry;

public interface ICpuTelemetryMenu {
    CpuTelemetry nebulae$getCpuTelemetry();

    default boolean nebulae$admissionFailed() {
        return false;
    }
}
