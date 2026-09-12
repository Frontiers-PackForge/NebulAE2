package com.ghostipedia.nebulaeae2.crafting;

import java.util.function.BooleanSupplier;

public final class CraftingDispatchBudget {
    private long nextTick = Long.MIN_VALUE;
    private long dispatchTick = Long.MIN_VALUE;
    private long generation;
    private int remaining;
    private boolean open;

    public void beginTick() {
        generation++;
        remaining = 0;
        open = false;
    }

    public boolean open(long tick, boolean enabled, int intervalTicks, int executions) {
        if (intervalTicks < 1 || executions < 1) {
            throw new IllegalArgumentException("Invalid dispatch capability");
        }
        if (!enabled || tick < nextTick) {
            return false;
        }
        nextTick = tick > Long.MAX_VALUE - intervalTicks ? Long.MAX_VALUE : tick + intervalTicks;
        dispatchTick = tick;
        remaining = executions;
        open = true;
        return true;
    }

    public int remaining() {
        return remaining;
    }

    public boolean canDispatch(long tick) {
        return open && dispatchTick == tick && remaining > 0;
    }

    public boolean tryDispatch(long tick, BooleanSupplier providerPush) {
        if (!canDispatch(tick)) {
            return false;
        }
        long opportunity = generation;
        remaining--;
        boolean accepted = false;
        try {
            accepted = providerPush.getAsBoolean();
            return accepted;
        } finally {
            if (!accepted && open && generation == opportunity && dispatchTick == tick) {
                remaining++;
            }
        }
    }

    public void reset() {
        beginTick();
        nextTick = Long.MIN_VALUE;
        dispatchTick = Long.MIN_VALUE;
    }
}
