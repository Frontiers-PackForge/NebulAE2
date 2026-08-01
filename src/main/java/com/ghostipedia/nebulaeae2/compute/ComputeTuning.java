package com.ghostipedia.nebulaeae2.compute;

public final class ComputeTuning {

    public static final long CHANNEL_DEVICE_RESERVATION = 1;
    public static final long STORAGE_PROVIDER_RESERVATION = 1;
    public static final long CRAFTING_PROVIDER_RESERVATION = 1;
    public static final long INTERFACE_STOCKING_SLOTS_PER_CWU = 3;
    public static final long INDEX_KEYS_PER_CWU = 64;
    public static final long PHYSICAL_LINKS_PER_CWU = 16;
    public static final int RESERVATION_REFRESH_INTERVAL = 20;
    public static final int MAX_DEBT_TICKS = 200;
    public static final long MINIMUM_DEBT_LIMIT = 256;

    private ComputeTuning() {}
}
