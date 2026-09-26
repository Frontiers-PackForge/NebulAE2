package com.ghostipedia.nebulaeae2.optimizer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;

import appeng.menu.guisync.PacketWritable;

public record OptimizerSnapshot(long revision, int page, int total, int selected, String status,
        long beforeExecutions, long afterExecutions, long excess, boolean previewReady, String operation, long factor, List<Row> rows)
        implements PacketWritable {
    public static final int PAGE_SIZE = 3;
    public static final OptimizerSnapshot EMPTY = new OptimizerSnapshot(0, 0, 0, 0, "idle", 0, 0, 0, false, "", 1, List.of());

    public OptimizerSnapshot {
        rows = List.copyOf(rows);
    }

    public OptimizerSnapshot(RegistryFriendlyByteBuf buffer) {
        this(buffer.readVarLong(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf(64),
                buffer.readVarLong(), buffer.readVarLong(), buffer.readVarLong(), buffer.readBoolean(),
                buffer.readUtf(16), buffer.readVarLong(), readRows(buffer));
    }

    private static List<Row> readRows(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > PAGE_SIZE) {
            throw new IllegalArgumentException("Invalid optimizer page");
        }
        var rows = new ArrayList<Row>(count);
        for (int i = 0; i < count; i++) {
            rows.add(new Row(buffer.readVarInt(), ComponentSerialization.STREAM_CODEC.decode(buffer),
                    buffer.readUtf(256), buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean(),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                    buffer.readUtf(64)));
        }
        return rows;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarLong(revision);
        buffer.writeVarInt(page);
        buffer.writeVarInt(total);
        buffer.writeVarInt(selected);
        buffer.writeUtf(status, 64);
        buffer.writeVarLong(beforeExecutions);
        buffer.writeVarLong(afterExecutions);
        buffer.writeVarLong(excess);
        buffer.writeBoolean(previewReady);
        buffer.writeUtf(operation, 16);
        buffer.writeVarLong(factor);
        buffer.writeVarInt(rows.size());
        for (var row : rows) {
            buffer.writeVarInt(row.index);
            ComponentSerialization.STREAM_CODEC.encode(buffer, row.name);
            buffer.writeUtf(row.location, 256);
            buffer.writeVarInt(row.slot);
            buffer.writeBoolean(row.allowed);
            buffer.writeBoolean(row.selected);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, row.before);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, row.after);
            buffer.writeUtf(row.error, 64);
        }
    }

    public record Row(int index, Component name, String location, int slot, boolean allowed, boolean selected,
            ItemStack before, ItemStack after, String error) {}
}
