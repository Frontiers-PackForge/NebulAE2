package com.ghostipedia.nebulaeae2.crafting.follow;

import java.util.Locale;

public final class CraftingFollowPolicy {
    private CraftingFollowPolicy() {}

    public static boolean followFromAmount(boolean global, boolean control) {
        return global || control;
    }

    public static boolean shouldNotify(boolean global, boolean followed, boolean storageScreen, boolean wireless) {
        return (global || followed) && !storageScreen && wireless;
    }

    public static String elapsed(long nanos) {
        long seconds = Math.max(0, nanos) / 1_000_000_000;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }
}
