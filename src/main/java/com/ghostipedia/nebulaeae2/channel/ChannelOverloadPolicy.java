package com.ghostipedia.nebulaeae2.channel;

import com.ghostipedia.nebulaeae2.compute.ComputeTuning;

public final class ChannelOverloadPolicy {

    public static final int STANDARD_RATING = 16;
    public static final int DENSE_RATING = 64;

    private ChannelOverloadPolicy() {}

    public static int overloadBand(int usedChannels, int rating) {
        if (rating <= 0 || usedChannels <= rating) {
            return 0;
        }
        if ((long) usedChannels <= 2L * rating) {
            return 1;
        }
        if ((long) usedChannels <= 3L * rating) {
            return 2;
        }
        return 3;
    }

    public static long incrementalCwut(int allocatedChannels, int rating) {
        if (rating <= 0 || allocatedChannels <= rating) {
            return 0;
        }
        long overloadIndex = (long) allocatedChannels - rating - 1;
        long costMultiplier = 1 + overloadIndex / rating;
        return saturatingMultiply(ComputeTuning.BASE_CWU_COST, costMultiplier);
    }

    public static int allocationLimit(int rating) {
        return rating <= 0 ? 0 : Integer.MAX_VALUE;
    }

    private static long saturatingMultiply(long left, long right) {
        if (left == 0 || right == 0) {
            return 0;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }
}
