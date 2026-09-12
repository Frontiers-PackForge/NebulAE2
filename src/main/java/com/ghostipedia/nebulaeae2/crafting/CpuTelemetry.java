package com.ghostipedia.nebulaeae2.crafting;

import com.ghostipedia.nebulaeae2.crafting.api.IExtendedCraftingCpu;
import com.ghostipedia.nebulaeae2.crafting.api.ICraftingCpuReservation;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import net.minecraft.network.RegistryFriendlyByteBuf;
import appeng.menu.guisync.PacketWritable;
import appeng.me.cluster.implementations.CraftingCPUCluster;

public record CpuTelemetry(int acceleration, int parallel, long storage, long reservation,
        long available, boolean paused, boolean present) implements PacketWritable {
    public static final CpuTelemetry EMPTY = new CpuTelemetry(0, 0, 0, 0, 0, false, false);

    public CpuTelemetry(RegistryFriendlyByteBuf data) {
        this(data.readVarInt(), data.readVarInt(), data.readLong(), data.readLong(), data.readLong(),
                data.readBoolean(), data.readBoolean());
    }

    public static CpuTelemetry capture(CraftingCPUCluster cpu) {
        if (cpu == null) {
            return EMPTY;
        }
        var capability = ((IExtendedCraftingCpu) (Object) cpu).nebulae$getCapability();
        var job = (ICraftingCpuReservation) (Object) cpu.craftingLogic;
        var grid = cpu.getGrid();
        var snapshot = grid == null ? null : grid.getService(IComputeService.class).snapshot();
        return new CpuTelemetry(capability.accelerationCores(), capability.parallelCores(), capability.storageBytes(),
                job.nebulae$getReservationCwut(), snapshot == null ? 0 : snapshot.availableCraftingCwut(),
                job.nebulae$isComputePaused(), true);
    }

    public long executionReservation() {
        return CraftingComputeTuning.executionReservationCwut(acceleration, parallel);
    }

    public long maximumReservation() {
        return CraftingComputeTuning.jobReservationCwut(storage, acceleration, parallel);
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        data.writeVarInt(acceleration);
        data.writeVarInt(parallel);
        data.writeLong(storage);
        data.writeLong(reservation);
        data.writeLong(available);
        data.writeBoolean(paused);
        data.writeBoolean(present);
    }
}
