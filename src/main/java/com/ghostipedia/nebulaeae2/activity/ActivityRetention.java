package com.ghostipedia.nebulaeae2.activity;

public final class ActivityRetention {
    public static final long DAY = 86_400_000L;
    public static final long[] WINDOWS = {60_000, 1_800_000, 3_600_000, 21_600_000, 43_200_000, DAY};

    private ActivityRetention() {}

    public static boolean expired(boolean automated, long finishedAt, long now) {
        return finishedAt > 0 && now >= finishedAt && now - finishedAt >= (automated ? 7 : 30) * DAY;
    }

    public static long coverage(long now, long started, long window) {
        return Math.max(1, Math.min(window, Math.max(0, now - started)));
    }
}
