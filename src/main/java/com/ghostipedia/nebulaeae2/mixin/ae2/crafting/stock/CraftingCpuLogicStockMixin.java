package com.ghostipedia.nebulaeae2.mixin.ae2.crafting.stock;

import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageSnapshot;
import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageSubmission;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.inv.ListCraftingInventory;

@Mixin(CraftingCpuLogic.class)
public abstract class CraftingCpuLogicStockMixin {
    @WrapMethod(method = "trySubmitJob")
    private ICraftingSubmitResult nebulae$scopeInitialStock(IGrid grid, ICraftingPlan plan, IActionSource source,
            ICraftingRequester requester, Operation<ICraftingSubmitResult> original) {
        var previous = StockUsageSubmission.set(new StockUsageSubmission(plan));
        try {
            return original.call(grid, plan, source, requester);
        } finally {
            StockUsageSubmission.set(previous);
        }
    }

    @WrapOperation(method = "trySubmitJob", at = @At(value = "INVOKE",
            target = "Lappeng/crafting/execution/CraftingCpuHelper;tryExtractInitialItems(Lappeng/api/networking/crafting/ICraftingPlan;Lappeng/api/networking/IGrid;Lappeng/crafting/inv/ListCraftingInventory;Lappeng/api/networking/security/IActionSource;)Lappeng/api/stacks/GenericStack;"))
    private GenericStack nebulae$captureInitialStock(ICraftingPlan plan, IGrid grid, ListCraftingInventory inventory,
            IActionSource source, Operation<GenericStack> original) {
        var snapshot = StockUsageSnapshot.capture(grid, source, plan, true);
        var missing = original.call(plan, grid, inventory, source);
        if (missing == null) {
            StockUsageSubmission.captured(plan, snapshot);
        }
        return missing;
    }
}
