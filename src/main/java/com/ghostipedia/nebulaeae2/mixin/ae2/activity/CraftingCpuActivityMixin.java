package com.ghostipedia.nebulaeae2.mixin.ae2.activity;

import com.ghostipedia.nebulaeae2.activity.ActivityArchive;
import com.ghostipedia.nebulaeae2.activity.ActivityDelivery;
import com.ghostipedia.nebulaeae2.activity.IActivityService;
import com.ghostipedia.nebulaeae2.crafting.api.ICraftingCpuReservation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingCpuLogic.class)
public abstract class CraftingCpuActivityMixin {
    @Shadow @Final private CraftingCPUCluster cluster;

    @Inject(method = "trySubmitJob", at = @At("RETURN"))
    private void nebulae$submitted(IGrid grid, ICraftingPlan plan, IActionSource source, ICraftingRequester requester,
            CallbackInfoReturnable<ICraftingSubmitResult> cir) {
        var state = ((ICraftingCpuReservation) this).nebulae$getJobState();
        if (cir.getReturnValue().successful() && state != null) {
            var service = grid.getService(IActivityService.class);
            service.archive().start(service.writableSegment(), state.reservationId(), plan, source,
                    cluster.getName() == null ? "" : cluster.getName().getString(), cluster.getLevel().registryAccess());
        }
    }

    @Inject(method = "tickCraftingLogic", at = @At("HEAD"))
    private void nebulae$update(CallbackInfo ci) {
        var state = ((ICraftingCpuReservation) this).nebulae$getJobState();
        if (state != null) nebulae$archive().update(state.reservationId(), state.elapsedNanos());
    }

    @WrapOperation(method = "insert", at = @At(value = "INVOKE",
            target = "Lappeng/crafting/CraftingLink;insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;)J"))
    private long nebulae$delivered(CraftingLink link, AEKey key, long amount, Actionable mode, Operation<Long> original) {
        long accepted = original.call(link, key, amount, mode);
        var state = ((ICraftingCpuReservation) this).nebulae$getJobState();
        if (mode == Actionable.MODULATE && state != null) {
            if (link.isStandalone()) ActivityDelivery.standalone(nebulae$archive(), state.reservationId(), amount);
            else nebulae$archive().delivered(state.reservationId(), accepted);
        }
        return accepted;
    }

    @Inject(method = "insert", at = @At("RETURN"))
    private void nebulae$consumed(AEKey key, long amount, Actionable mode, CallbackInfoReturnable<Long> cir) {
        if (mode == Actionable.MODULATE) ActivityDelivery.consumed(cir.getReturnValue());
    }

    @Inject(method = "finishJob", at = @At("HEAD"))
    private void nebulae$finished(boolean success, CallbackInfo ci) {
        var state = ((ICraftingCpuReservation) this).nebulae$getJobState();
        if (state != null) {
            state.tick(false, System.nanoTime());
            nebulae$archive().finish(state.reservationId(), success, state.elapsedNanos());
        }
    }

    @Unique
    private ActivityArchive nebulae$archive() { return ActivityArchive.get(cluster.getLevel().getServer()); }
}
