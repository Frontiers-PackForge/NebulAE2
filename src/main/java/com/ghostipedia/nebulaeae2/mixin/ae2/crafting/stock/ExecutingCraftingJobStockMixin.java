package com.ghostipedia.nebulaeae2.mixin.ae2.crafting.stock;

import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageJob;
import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageSnapshot;
import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageSubmission;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;

@Mixin(ExecutingCraftingJob.class)
public abstract class ExecutingCraftingJobStockMixin implements StockUsageJob {
    @Unique private StockUsageSnapshot nebulae$initialStock = StockUsageSnapshot.EMPTY;

    @Inject(method = "<init>(Lappeng/api/networking/crafting/ICraftingPlan;Lappeng/crafting/execution/ExecutingCraftingJob$CraftingDifferenceListener;Lappeng/crafting/CraftingLink;Ljava/lang/Integer;)V", at = @At("RETURN"))
    private void nebulae$captureStock(ICraftingPlan plan, @Coerce Object listener, CraftingLink link, Integer playerId,
            CallbackInfo callback) {
        nebulae$initialStock = StockUsageSubmission.consume(plan);
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lappeng/crafting/execution/ExecutingCraftingJob$CraftingDifferenceListener;Lappeng/crafting/execution/CraftingCpuLogic;)V", at = @At("RETURN"))
    private void nebulae$loadStock(CompoundTag data, HolderLookup.Provider registries, @Coerce Object listener,
            CraftingCpuLogic cpu, CallbackInfo callback) {
        nebulae$initialStock = StockUsageSnapshot.load(data.getCompound("nebulaeStock"), registries);
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void nebulae$saveStock(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> callback) {
        callback.getReturnValue().put("nebulaeStock", nebulae$initialStock.save(registries));
    }

    @Override
    public StockUsageSnapshot nebulae$initialStock() {
        return nebulae$initialStock;
    }
}
