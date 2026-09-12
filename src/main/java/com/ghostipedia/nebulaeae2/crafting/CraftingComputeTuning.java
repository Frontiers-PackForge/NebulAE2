package com.ghostipedia.nebulaeae2.crafting;

public final class CraftingComputeTuning {

    public static final int BASE_INTERVAL_TICKS = 10;
    public static final int MAX_ACCELERATION_CORES = BASE_INTERVAL_TICKS - 1;
    public static final int MAX_PARALLEL_CORES = 63;
    public static final long BYTES_PER_RESERVATION_UNIT = 1024;
    public static final long BYTE_RESERVATION_CWUT = 5;
    public static final long EXECUTION_RESERVATION_FACTOR = 100;

    private CraftingComputeTuning() {}

    public static boolean validCoreCounts(int accelerationCores, int parallelCores) {
        return accelerationCores >= 0 && accelerationCores <= MAX_ACCELERATION_CORES
                && parallelCores >= 0 && parallelCores <= MAX_PARALLEL_CORES;
    }

    public static int dispatchIntervalTicks(int accelerationCores) {
        if (!validCoreCounts(accelerationCores, 0)) {
            throw new IllegalArgumentException("Invalid acceleration core count");
        }
        return BASE_INTERVAL_TICKS - accelerationCores;
    }

    public static int executionsPerOpportunity(int parallelCores) {
        if (!validCoreCounts(0, parallelCores)) {
            throw new IllegalArgumentException("Invalid parallel core count");
        }
        return 1 + parallelCores;
    }

    public static long executionReservationCwut(int accelerationCores, int parallelCores) {
        return ceilDivide(EXECUTION_RESERVATION_FACTOR * executionsPerOpportunity(parallelCores),
                dispatchIntervalTicks(accelerationCores));
    }

    public static long jobReservationCwut(long bytes, int accelerationCores, int parallelCores) {
        if (bytes < 0) {
            throw new IllegalArgumentException("Negative crafting bytes");
        }
        return Math.addExact(Math.multiplyExact(BYTE_RESERVATION_CWUT, ceilDivide(bytes, BYTES_PER_RESERVATION_UNIT)),
                executionReservationCwut(accelerationCores, parallelCores));
    }

    private static long ceilDivide(long value, long divisor) {
        return value / divisor + (value % divisor == 0 ? 0 : 1);
    }
}
