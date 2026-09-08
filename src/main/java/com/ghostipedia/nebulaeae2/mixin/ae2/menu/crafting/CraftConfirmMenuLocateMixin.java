package com.ghostipedia.nebulaeae2.mixin.ae2.menu.crafting;

import com.ghostipedia.nebulaeae2.locating.ProviderLocateMenu;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftConfirmMenu.class)
public abstract class CraftConfirmMenuLocateMixin implements ProviderLocateMenu {
    @Shadow private IGrid getGrid() { throw new AssertionError(); }
    @Shadow public abstract CraftingPlanSummary getPlan();

    @Override
    public IGrid nebulae$locateGrid() {
        return getGrid();
    }

    @Override
    public boolean nebulae$canLocate(AEKey key) {
        var plan = getPlan();
        return plan != null && plan.getEntries().stream().anyMatch(entry -> entry.getWhat().equals(key));
    }
}
