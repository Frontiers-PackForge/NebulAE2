package com.ghostipedia.nebulaeae2.optimizer;

public final class OptimizerArithmetic {
    private OptimizerArithmetic() {}

    public static long multiply(long amount, long multiplier) {
        if (amount <= 0 || multiplier <= 0) {
            throw new IllegalArgumentException("invalid_factor");
        }
        try {
            return Math.multiplyExact(amount, multiplier);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("overflow");
        }
    }

    public static long divide(long amount, long divisor) {
        if (amount <= 0 || divisor <= 0 || amount % divisor != 0) {
            throw new IllegalArgumentException("non_exact");
        }
        return amount / divisor;
    }

    public static long reducedMultiplier(long cumulative, long divisor) {
        return divide(cumulative, divisor);
    }
}
