package com.ghostipedia.nebulaeae2.integration.igtooltip;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import appeng.api.config.PowerUnit;
import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.TooltipProvider;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.block.networking.ControllerBlock;
import appeng.blockentity.networking.ControllerBlockEntity;
import static com.ghostipedia.nebulaeae2.integration.igtooltip.ComputeTooltipFormatting.value;

public final class ControllerComputeTooltipProvider implements TooltipProvider,
        BodyProvider<ControllerBlockEntity>, ServerDataProvider<ControllerBlockEntity> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NebulaeAE2.MODID, "controller_compute");
    private static final String DATA = "NebulaeControllerCompute";

    @Override
    public void registerCommon(CommonRegistration registration) {
        CableComputeTooltipProvider.registerCommon();
        CraftingCpuTooltipProvider.registerCommon(registration);
        registration.addBlockEntityData(ID, ControllerBlockEntity.class, this);
    }

    @Override
    public void registerClient(ClientRegistration registration) {
        CableComputeTooltipProvider.registerClient();
        CraftingCpuTooltipProvider.registerClient(registration);
        registration.addBlockEntityBody(ControllerBlockEntity.class, ControllerBlock.class, ID, this);
    }

    @Override
    public void provideServerData(Player player, ControllerBlockEntity controller, CompoundTag serverData) {
        var grid = controller.getMainNode().getGrid();
        if (grid == null) {
            return;
        }
        CompoundTag data = GridComputeTooltipData.write(grid.getService(IComputeService.class).snapshot());
        data.putInt("UsedChannels", grid.getPathingService().getUsedChannels());
        data.putDouble("EuDemand", Math.max(0, PowerUnit.AE.convertTo(PowerUnit.FE,
                grid.getEnergyService().getAvgPowerUsage()) / FeCompat.ratio(false)));
        serverData.put(DATA, data);
    }

    @Override
    public void buildTooltip(ControllerBlockEntity controller, TooltipContext context, TooltipBuilder tooltip) {
        if (!context.serverData().contains(DATA, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag data = context.serverData().getCompound(DATA);
        GridComputeTooltipData.append(data, tooltip, Screen.hasShiftDown());
        tooltip.addLine(Component.translatable("tooltip.nebulaeae2.controller.channels",
                value(data.getInt("UsedChannels"), ChatFormatting.AQUA)));
        tooltip.addLine(Component.translatable("tooltip.nebulaeae2.controller.eu_demand",
                value(data.getDouble("EuDemand"), ChatFormatting.YELLOW)));
    }
}
