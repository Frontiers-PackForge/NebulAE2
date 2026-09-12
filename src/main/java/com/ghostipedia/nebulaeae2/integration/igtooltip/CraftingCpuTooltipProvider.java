package com.ghostipedia.nebulaeae2.integration.igtooltip;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.ghostipedia.nebulaeae2.crafting.CpuTelemetry;
import com.ghostipedia.nebulaeae2.client.CpuTelemetryText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;

final class CraftingCpuTooltipProvider implements BodyProvider<CraftingBlockEntity>, ServerDataProvider<CraftingBlockEntity> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NebulaeAE2.MODID, "crafting_cpu");
    private static final String DATA = "NebulaeCraftingCpu";

    static void registerCommon(CommonRegistration registration) {
        registration.addBlockEntityData(ID, CraftingBlockEntity.class, new CraftingCpuTooltipProvider());
    }

    static void registerClient(ClientRegistration registration) {
        registration.addBlockEntityBody(CraftingBlockEntity.class, AbstractCraftingUnitBlock.class, ID,
                new CraftingCpuTooltipProvider());
    }

    @Override
    public void provideServerData(Player player, CraftingBlockEntity blockEntity, CompoundTag serverData) {
        var cpu = CpuTelemetry.capture(blockEntity.getCluster());
        if (!cpu.present()) {
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putInt("Acceleration", cpu.acceleration());
        data.putInt("Parallel", cpu.parallel());
        data.putLong("Storage", cpu.storage());
        data.putLong("Reservation", cpu.reservation());
        data.putLong("Available", cpu.available());
        data.putBoolean("Paused", cpu.paused());
        serverData.put(DATA, data);
    }

    @Override
    public void buildTooltip(CraftingBlockEntity blockEntity, TooltipContext context, TooltipBuilder tooltip) {
        if (!context.serverData().contains(DATA, Tag.TAG_COMPOUND)) {
            return;
        }
        var data = context.serverData().getCompound(DATA);
        var cpu = new CpuTelemetry(data.getInt("Acceleration"), data.getInt("Parallel"), data.getLong("Storage"),
                data.getLong("Reservation"), data.getLong("Available"), data.getBoolean("Paused"), true);
        CpuTelemetryText.lines(cpu).forEach(tooltip::addLine);
    }
}
