package com.ghostipedia.nebulaeae2.crafting.stock;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.guisync.PacketWritable;

public record StockUsageSnapshot(Map<AEKey, StockUsageAmounts> entries, boolean initial) implements PacketWritable {
    public static final StockUsageSnapshot EMPTY = new StockUsageSnapshot(Map.of(), false);

    public StockUsageSnapshot {
        entries = Map.copyOf(entries);
    }

    public StockUsageSnapshot(RegistryFriendlyByteBuf buffer) {
        this(readEntries(buffer), buffer.readBoolean());
    }

    public static StockUsageSnapshot capture(IGrid grid, IActionSource source, ICraftingPlan plan, boolean initial) {
        var required = new HashMap<AEKey, long[]>();
        for (var entry : plan.usedItems()) {
            required.computeIfAbsent(entry.getKey(), key -> new long[2])[0] = Math.max(0, entry.getLongValue());
        }
        if (!initial) {
            for (var entry : plan.missingItems()) {
                required.computeIfAbsent(entry.getKey(), key -> new long[2])[1] = Math.max(0, entry.getLongValue());
            }
        }
        var result = new HashMap<AEKey, StockUsageAmounts>();
        var storage = grid.getStorageService().getInventory();
        for (var entry : required.entrySet()) {
            long used = entry.getValue()[0];
            long missing = entry.getValue()[1];
            if (used > 0 || missing > 0) {
                long available = Math.max(0, storage.extract(entry.getKey(), Long.MAX_VALUE, Actionable.SIMULATE, source));
                result.put(entry.getKey(), new StockUsageAmounts(used, missing, available));
            }
        }
        return new StockUsageSnapshot(result, initial);
    }

    private static Map<AEKey, StockUsageAmounts> readEntries(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > buffer.readableBytes() / 4) {
            throw new IllegalArgumentException("Invalid stock snapshot size");
        }
        var entries = new HashMap<AEKey, StockUsageAmounts>();
        for (int i = 0; i < count; i++) {
            var key = AEKey.readKey(buffer);
            entries.put(key, new StockUsageAmounts(buffer.readVarLong(), buffer.readVarLong(), buffer.readVarLong()));
        }
        return entries;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(entries.size());
        for (var entry : entries.entrySet()) {
            AEKey.writeKey(buffer, entry.getKey());
            buffer.writeVarLong(entry.getValue().used());
            buffer.writeVarLong(entry.getValue().missing());
            buffer.writeVarLong(entry.getValue().available());
        }
        buffer.writeBoolean(initial);
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        var data = new CompoundTag();
        if (!initial) {
            return data;
        }
        var list = new ListTag();
        for (var entry : entries.entrySet()) {
            var tag = GenericStack.writeTag(registries, new GenericStack(entry.getKey(), entry.getValue().used()));
            tag.putLong("available", entry.getValue().available());
            list.add(tag);
        }
        data.put("entries", list);
        return data;
    }

    public static StockUsageSnapshot load(CompoundTag data, HolderLookup.Provider registries) {
        if (!data.contains("entries", Tag.TAG_LIST)) {
            return EMPTY;
        }
        var entries = new HashMap<AEKey, StockUsageAmounts>();
        for (var element : data.getList("entries", Tag.TAG_COMPOUND)) {
            var tag = (CompoundTag) element;
            var stack = GenericStack.readTag(registries, tag);
            if (stack != null && stack.amount() > 0) {
                entries.put(stack.what(), new StockUsageAmounts(stack.amount(), 0, Math.max(0, tag.getLong("available"))));
            }
        }
        return new StockUsageSnapshot(entries, true);
    }
}
