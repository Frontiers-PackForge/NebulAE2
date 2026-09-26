package com.ghostipedia.nebulaeae2.activity;

import java.math.BigInteger;
import java.util.UUID;

import appeng.api.stacks.AEKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class ActivityViewData {
    private ActivityViewData() {}

    public static boolean matches(CompoundTag data, int tab, UUID selected) {
        if (!data.contains("tab", Tag.TAG_INT) || data.getInt("tab") != tab) return false;
        if (selected == null) return true;
        var detail = data.getCompound("detail");
        return detail.hasUUID("id") && selected.equals(detail.getUUID("id"));
    }

    public static ListTag rows(CompoundTag data, int tab, UUID selected) {
        if (!matches(data, tab, selected)) return new ListTag();
        return selected == null ? data.getList("rows", Tag.TAG_COMPOUND)
                : data.getCompound("detail").getList("materials", Tag.TAG_COMPOUND);
    }

    public static FlowRow flowRow(CompoundTag row, HolderLookup.Provider registries) {
        if (!row.contains("in", Tag.TAG_STRING) || !row.contains("out", Tag.TAG_STRING)
                || row.getCompound("key").isEmpty()) return null;
        try {
            var incoming = new BigInteger(row.getString("in"));
            var outgoing = new BigInteger(row.getString("out"));
            if (incoming.signum() < 0 || outgoing.signum() < 0) return null;
            var key = AEKey.fromTagGeneric(registries, row.getCompound("key"));
            return key == null ? null : new FlowRow(key, incoming, outgoing);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public record FlowRow(AEKey key, BigInteger incoming, BigInteger outgoing) {}
}
