package com.ghostipedia.nebulaeae2.mixin.ae2.menu.crafting;

import com.ghostipedia.nebulaeae2.locating.ProviderLocateMenu;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.menu.me.crafting.CraftingCPUMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftingCPUMenu.class)
public abstract class CraftingCPUMenuLocateMixin implements ProviderLocateMenu {
    @Shadow private CraftingCPUCluster cpu;
    @Shadow abstract IGrid getGrid();

    @Override
    public IGrid nebulae$locateGrid() {
        return getGrid();
    }

    @Override
    public boolean nebulae$canLocate(AEKey key) {
        if (cpu == null || cpu.isDestroyed()) {
            return false;
        }
        var contents = new KeyCounter();
        cpu.craftingLogic.getAllItems(contents);
        return contents.get(key) > 0;
    }
}
