package com.ghostipedia.nebulaeae2.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;

@EmiEntrypoint
public final class NebulaeEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addGenericScreenBoundsProvider(screen -> {
            if (screen instanceof CraftConfirmScreen || screen instanceof CraftingCPUScreen<?>) {
                var craftingScreen = (AEBaseScreen<?>) screen;
                return new Bounds(craftingScreen.getGuiLeft(), craftingScreen.getGuiTop(),
                        craftingScreen.getXSize(), craftingScreen.getYSize());
            }
            return null;
        });
    }
}
