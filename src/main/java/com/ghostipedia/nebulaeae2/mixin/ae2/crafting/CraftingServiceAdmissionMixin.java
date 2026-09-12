package com.ghostipedia.nebulaeae2.mixin.ae2.crafting;

import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import com.ghostipedia.nebulaeae2.crafting.CraftingAdmissionFailure;
import com.ghostipedia.nebulaeae2.crafting.CraftingCpuSelection;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingService.class)
public abstract class CraftingServiceAdmissionMixin {

    @Shadow
    @Final
    private IGrid grid;

    @Inject(method = "submitJob", at = @At("HEAD"), cancellable = true)
    private void nebulae$selectAffordableCpu(ICraftingPlan plan, ICraftingRequester requester, ICraftingCPU target,
            boolean prioritizePower, IActionSource source, CallbackInfoReturnable<ICraftingSubmitResult> cir) {
        if (plan.simulation()) {
            return;
        }
        if (!grid.getService(IComputeService.class).canAdmitCrafting()) {
            cir.setReturnValue(CraftingAdmissionFailure.INSUFFICIENT_CWU);
            return;
        }
        if (target == null) {
            CraftingCPUCluster selected = CraftingCpuSelection.select(grid, plan, source, prioritizePower);
            if (selected != null) {
                cir.setReturnValue(selected.submitJob(grid, plan, source, requester));
            }
        }
    }
}
