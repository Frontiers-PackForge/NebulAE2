package com.ghostipedia.nebulaeae2.crafting;

public final class CraftingSubmissionContext {

    private static final ThreadLocal<CraftingJobState> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> FOLLOW = new ThreadLocal<>();

    private CraftingSubmissionContext() {}

    public static CraftingJobState current() {
        return CURRENT.get();
    }

    public static CraftingJobState setCurrent(CraftingJobState state) {
        CraftingJobState previous = CURRENT.get();
        if (state == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(state);
        }
        return previous;
    }

    public static boolean followed() {
        return Boolean.TRUE.equals(FOLLOW.get());
    }

    public static Boolean setFollowed(Boolean followed) {
        Boolean previous = FOLLOW.get();
        if (followed == null) {
            FOLLOW.remove();
        } else {
            FOLLOW.set(followed);
        }
        return previous;
    }
}
