package com.ghostipedia.nebulaeae2.mixin.ae2.crafting;

import com.ghostipedia.nebulaeae2.crafting.api.ICraftingCpuReservation;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingCPUCluster.class)
public abstract class CraftingCPUClusterDurationMixin {

    @Shadow
    @Final
    public CraftingCpuLogic craftingLogic;

    @ModifyExpressionValue(method = "getJobStatus", at = @At(value = "INVOKE",
            target = "Lappeng/crafting/execution/ElapsedTimeTracker;getElapsedTime()J"))
    private long nebulae$persistedRunningDuration(long nativeDuration) {
        var state = ((ICraftingCpuReservation) craftingLogic).nebulae$getJobState();
        return state == null ? nativeDuration : state.elapsedNanos();
    }
}
