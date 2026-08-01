package com.ghostipedia.nebulaeae2.mixin.ae2.crafting;

import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.ghostipedia.nebulaeae2.compute.ComputeTuning;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CraftingCpuLogic.class)
public abstract class CraftingCpuLogicComputeMixin {

    @Shadow
    @Final
    private CraftingCPUCluster cluster;

    @ModifyVariable(method = "executeCrafting", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int nebulaeae2$limitPatternDispatches(int maximumPatterns) {
        var grid = cluster.getGrid();
        var node = cluster.getNode();
        if (grid == null || node == null) {
            return 0;
        }
        long allowedPatterns = grid.getService(IComputeService.class).acquireWholeUnitsUpTo(
                node,
                maximumPatterns,
                ComputeTuning.CRAFTING_PATTERN_DISPATCH_CWU);
        return (int) Math.min(Integer.MAX_VALUE, allowedPatterns);
    }
}
