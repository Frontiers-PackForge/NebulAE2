package com.ghostipedia.nebulaeae2.mixin.ae2.crafting.stock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;

@Mixin(CraftingCpuLogic.class)
public interface CraftingCpuLogicStockAccessor {
    @Accessor("job")
    ExecutingCraftingJob nebulae$stockJob();
}
