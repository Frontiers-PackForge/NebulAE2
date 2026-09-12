package com.ghostipedia.nebulaeae2.mixin.ae2.crafting;

import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingCpuLogic.class)
public interface CraftingCpuLogicAccessor {

    @Accessor("cluster")
    CraftingCPUCluster nebulae$getCluster();
}
