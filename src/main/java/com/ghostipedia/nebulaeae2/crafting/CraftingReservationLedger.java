package com.ghostipedia.nebulaeae2.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongUnaryOperator;

public final class CraftingReservationLedger {

    private final Map<UUID, Long> attached = new HashMap<>();
    private final Map<UUID, Long> pending = new HashMap<>();

    public synchronized void replaceAttached(Map<UUID, Long> reservations) {
        attached.clear();
        reservations.forEach((id, cost) -> {
            if (cost < 0) {
                throw new IllegalArgumentException("Negative crafting reservation");
            }
            attached.put(id, cost);
        });
    }

    public synchronized boolean tryReserve(UUID id, long cost, long capacity, long infrastructure,
            LongUnaryOperator fund) {
        if (cost <= 0 || attached.containsKey(id) || pending.containsKey(id)) {
            return false;
        }
        long existing = add(infrastructure, totalCwut());
        if (existing > capacity || cost > capacity - existing) {
            return false;
        }
        pending.put(id, cost);
        boolean accepted = false;
        try {
            long target = existing + cost;
            accepted = fund.applyAsLong(target) >= target;
            return accepted;
        } finally {
            if (!accepted) {
                pending.remove(id);
            }
        }
    }

    public synchronized void finishAdmission(UUID id) {
        pending.remove(id);
    }

    public synchronized long totalCwut() {
        long total = 0;
        for (long cost : attached.values()) {
            total = add(total, cost);
        }
        for (var claim : pending.entrySet()) {
            if (!attached.containsKey(claim.getKey())) {
                total = add(total, claim.getValue());
            }
        }
        return total;
    }

    private static long add(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }
}
