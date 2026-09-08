package com.ghostipedia.nebulaeae2.mixin.ae2.menu.crafting;

import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageMenu;
import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.crafting.CraftConfirmMenu;

@Mixin(CraftConfirmMenu.class)
public abstract class CraftConfirmMenuStockMixin implements StockUsageMenu {
    @Shadow private ICraftingPlan result;
    @Shadow private IGrid getGrid() { throw new AssertionError(); }
    @Shadow private IActionSource getActionSrc() { throw new AssertionError(); }
    @Unique @GuiSync(140) public StockUsageSnapshot nebulae$stock = StockUsageSnapshot.EMPTY;
    @Unique private ICraftingPlan nebulae$lastPlan;
    @Unique private long nebulae$nextSnapshot;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void nebulae$refreshStock(CallbackInfo callback) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (menu.isClientSide()) {
            return;
        }
        if (result == null) {
            nebulae$stock = StockUsageSnapshot.EMPTY;
            nebulae$lastPlan = null;
            return;
        }
        long now = menu.getPlayer().level().getGameTime();
        if (nebulae$lastPlan != result || now >= nebulae$nextSnapshot) {
            var grid = getGrid();
            nebulae$stock = grid == null ? StockUsageSnapshot.EMPTY
                    : StockUsageSnapshot.capture(grid, getActionSrc(), result, false);
            nebulae$lastPlan = result;
            nebulae$nextSnapshot = now + 20;
        }
    }

    @Override
    public StockUsageSnapshot nebulae$stockUsage() {
        return nebulae$stock;
    }
}
