package com.ghostipedia.nebulaeae2.mixin.ae2.menu.crafting;

import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageJob;
import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageMenu;
import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageSnapshot;
import com.ghostipedia.nebulaeae2.mixin.ae2.crafting.stock.CraftingCpuLogicStockAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.crafting.CraftingCPUMenu;

@Mixin(CraftingCPUMenu.class)
public abstract class CraftingCPUMenuStockMixin implements StockUsageMenu {
    @Shadow private CraftingCPUCluster cpu;
    @Unique @GuiSync(140) public StockUsageSnapshot nebulae$stock = StockUsageSnapshot.EMPTY;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void nebulae$syncInitialStock(CallbackInfo callback) {
        var menu = (CraftingCPUMenu) (Object) this;
        if (!menu.isClientSide()) {
            var job = cpu == null ? null : ((CraftingCpuLogicStockAccessor) cpu.craftingLogic).nebulae$stockJob();
            nebulae$stock = job == null ? StockUsageSnapshot.EMPTY : ((StockUsageJob) job).nebulae$initialStock();
        }
    }

    @Override
    public StockUsageSnapshot nebulae$stockUsage() {
        return nebulae$stock;
    }
}
