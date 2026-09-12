package com.ghostipedia.nebulaeae2.integration.igtooltip;

import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import appeng.api.implementations.parts.ICablePart;
import appeng.api.integrations.igtooltip.PartTooltips;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;

final class CableComputeTooltipProvider {
    private static final int PRIORITY = 900;
    private static final String DATA = "NebulaeCableCompute";
    private static boolean commonRegistered;
    private static boolean clientRegistered;

    private CableComputeTooltipProvider() {}

    static synchronized void registerCommon() {
        if (!commonRegistered) {
            PartTooltips.addServerData(ICablePart.class, CableComputeTooltipProvider::provideServerData, PRIORITY);
            commonRegistered = true;
        }
    }

    static synchronized void registerClient() {
        if (!clientRegistered) {
            PartTooltips.addBody(ICablePart.class, CableComputeTooltipProvider::buildTooltip, PRIORITY);
            clientRegistered = true;
        }
    }

    private static void provideServerData(Player player, ICablePart cable, CompoundTag serverData) {
        var node = cable.getGridNode();
        if (node != null) {
            serverData.put(DATA, GridComputeTooltipData.write(node.getGrid().getService(IComputeService.class).snapshot()));
        }
    }

    private static void buildTooltip(ICablePart cable, TooltipContext context, TooltipBuilder tooltip) {
        if (context.serverData().contains(DATA, Tag.TAG_COMPOUND)) {
            GridComputeTooltipData.append(context.serverData().getCompound(DATA), tooltip, Screen.hasShiftDown());
        }
    }
}
