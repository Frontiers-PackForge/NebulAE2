package com.ghostipedia.nebulaeae2.crafting;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class CraftingJobState {

    private final UUID reservationId;
    private final long planBytes;
    private boolean followed;
    private long elapsedNanos;
    private long lastRunningNanos;

    public CraftingJobState(long planBytes, boolean followed) {
        this(UUID.randomUUID(), Math.max(0, planBytes), followed, 0);
    }

    private CraftingJobState(UUID reservationId, long planBytes, boolean followed, long elapsedNanos) {
        this.reservationId = reservationId;
        this.planBytes = planBytes;
        this.followed = followed;
        this.elapsedNanos = elapsedNanos;
    }

    public UUID reservationId() {
        return reservationId;
    }

    public long planBytes() {
        return planBytes;
    }

    public boolean followed() {
        return followed;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }

    public void tick(boolean running, long nowNanos) {
        if (lastRunningNanos != 0) {
            long delta = Math.max(0, nowNanos - lastRunningNanos);
            elapsedNanos = delta > Long.MAX_VALUE - elapsedNanos ? Long.MAX_VALUE : elapsedNanos + delta;
        }
        lastRunningNanos = running ? nowNanos : 0;
    }

    public long elapsedNanos() {
        return elapsedNanos;
    }

    public CompoundTag save() {
        CompoundTag data = new CompoundTag();
        data.putUUID("reservationId", reservationId);
        data.putLong("planBytes", planBytes);
        data.putBoolean("followed", followed);
        data.putLong("elapsedNanos", elapsedNanos);
        return data;
    }

    public static CraftingJobState load(CompoundTag data, long legacyBytes) {
        return new CraftingJobState(data.hasUUID("reservationId") ? data.getUUID("reservationId") : UUID.randomUUID(),
                Math.max(0, data.contains("planBytes") ? data.getLong("planBytes") : legacyBytes),
                data.getBoolean("followed"), Math.max(0, data.getLong("elapsedNanos")));
    }
}
