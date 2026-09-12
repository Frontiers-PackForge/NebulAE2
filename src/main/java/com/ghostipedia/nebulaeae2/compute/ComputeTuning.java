package com.ghostipedia.nebulaeae2.compute;

public final class ComputeTuning {

    public static final long BASE_CWU_COST = 5;
    public static final long CHANNEL_DEVICE_RESERVATION = BASE_CWU_COST;
    public static final long CHANNEL_DEVICE_SCALE_GROUP = 64;
    public static final long STORAGE_PROVIDER_RESERVATION = BASE_CWU_COST;
    public static final long CRAFTING_PROVIDER_RESERVATION = BASE_CWU_COST;
    public static final long WIRELESS_BOOSTER_RESERVATION = 25;
    public static final long INTERFACE_STOCKING_SLOTS_PER_GROUP = 3;
    public static final long INTERFACE_STOCKING_GROUP_RESERVATION = BASE_CWU_COST;
    public static final long INDEX_KEYS_PER_GROUP = 64;
    public static final long INDEX_KEY_GROUP_RESERVATION = BASE_CWU_COST;
    public static final long PHYSICAL_LINKS_PER_GROUP = 16;
    public static final long PHYSICAL_LINK_GROUP_RESERVATION = BASE_CWU_COST;
    public static final int RESERVATION_REFRESH_INTERVAL = 20;

    private ComputeTuning() {}

    public static long channelDeviceReservation(long deviceCount) {
        if (deviceCount <= 0) {
            return 0;
        }
        long completeGroups = deviceCount / CHANNEL_DEVICE_SCALE_GROUP;
        long remainingDevices = deviceCount % CHANNEL_DEVICE_SCALE_GROUP;
        long marginalTier = completeGroups + 1;
        long triangularGroups = saturatingMultiply(completeGroups, marginalTier) / 2;
        long completeGroupUnits = saturatingMultiply(CHANNEL_DEVICE_SCALE_GROUP, triangularGroups);
        long remainingUnits = saturatingMultiply(remainingDevices, marginalTier);
        return saturatingMultiply(BASE_CWU_COST, saturatingAdd(completeGroupUnits, remainingUnits));
    }

    public static long channelDeviceScaleReservation(long deviceCount) {
        long baseReservation = saturatingMultiply(Math.max(0, deviceCount), CHANNEL_DEVICE_RESERVATION);
        return Math.max(0, channelDeviceReservation(deviceCount) - baseReservation);
    }

    private static long saturatingAdd(long left, long right) {
        if (right > 0 && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
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
