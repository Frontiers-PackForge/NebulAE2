package com.ghostipedia.nebulaeae2.crafting;

import java.util.Map;
import java.util.HashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import appeng.menu.guisync.PacketWritable;

public record CpuListTelemetry(Map<Integer, CpuTelemetry> cpus) implements PacketWritable {
    public CpuListTelemetry {
        cpus = Map.copyOf(cpus);
    }

    public CpuListTelemetry(RegistryFriendlyByteBuf data) {
        this(read(data));
    }

    private static Map<Integer, CpuTelemetry> read(RegistryFriendlyByteBuf data) {
        int size = data.readVarInt();
        if (size < 0 || size > 65536) {
            throw new IllegalArgumentException("Invalid CPU telemetry list size");
        }
        Map<Integer, CpuTelemetry> cpus = new HashMap<>();
        for (int i = 0; i < size; i++) {
            cpus.put(data.readVarInt(), new CpuTelemetry(data));
        }
        return cpus;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        data.writeVarInt(cpus.size());
        cpus.forEach((serial, cpu) -> {
            data.writeVarInt(serial);
            cpu.writeToPacket(data);
        });
    }
}
