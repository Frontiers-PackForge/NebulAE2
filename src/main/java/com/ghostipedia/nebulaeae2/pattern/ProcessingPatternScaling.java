package com.ghostipedia.nebulaeae2.pattern;

public final class ProcessingPatternScaling {
    private ProcessingPatternScaling() {}

    public static int factor(boolean shift, boolean control) {
        return control ? 8 : shift ? 4 : 2;
    }

    public static long scale(long amount, int operation, long maximum) {
        if (operation != 2 && operation != 4 && operation != 8
                && operation != -2 && operation != -4 && operation != -8) {
            throw new IllegalArgumentException("invalid_operation");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("invalid_amount");
        }
        long result;
        if (operation < 0) {
            if (amount % -operation != 0) {
                throw new IllegalArgumentException("non_exact");
            }
            result = amount / -operation;
        } else {
            try {
                result = Math.multiplyExact(amount, operation);
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("too_large");
            }
        }
        if (result <= 0 || result > maximum) {
            throw new IllegalArgumentException("too_large");
        }
        return result;
    }
}
