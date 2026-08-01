package com.ghostipedia.nebulaeae2.compute;

public final class ComputeTuning {

    public static final long BASE_CWU_COST = 5;
    public static final long CHANNEL_DEVICE_RESERVATION = BASE_CWU_COST;
    public static final long STORAGE_PROVIDER_RESERVATION = BASE_CWU_COST;
    public static final long CRAFTING_PROVIDER_RESERVATION = BASE_CWU_COST;
    public static final long INTERFACE_STOCKING_SLOTS_PER_GROUP = 3;
    public static final long INTERFACE_STOCKING_GROUP_RESERVATION = BASE_CWU_COST;
    public static final long INDEX_KEYS_PER_GROUP = 64;
    public static final long INDEX_KEY_GROUP_RESERVATION = BASE_CWU_COST;
    public static final long PHYSICAL_LINKS_PER_GROUP = 16;
    public static final long PHYSICAL_LINK_GROUP_RESERVATION = BASE_CWU_COST;
    public static final long SCHEDULED_WORK_CWU = BASE_CWU_COST;
    public static final long CRAFTING_PATTERN_DISPATCH_CWU = BASE_CWU_COST;
    public static final int RESERVATION_REFRESH_INTERVAL = 20;
    public static final int RECOVERY_CAPACITY_DIVISOR = 64;
    public static final int RECOVERY_NODE_COOLDOWN_TICKS = 20;
    public static final int RECOVERY_WAITER_EXPIRY_TICKS = 120;
    public static final int TELEMETRY_WINDOW_TICKS = 20;
    public static final int MAX_DEBT_TICKS = 200;
    public static final long MINIMUM_DEBT_LIMIT = 256L * BASE_CWU_COST;

    private ComputeTuning() {}
}
