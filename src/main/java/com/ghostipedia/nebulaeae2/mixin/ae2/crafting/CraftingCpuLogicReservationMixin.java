package com.ghostipedia.nebulaeae2.mixin.ae2.crafting;

import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import com.ghostipedia.nebulaeae2.crafting.CpuCapability;
import com.ghostipedia.nebulaeae2.crafting.CraftingAdmissionFailure;
import com.ghostipedia.nebulaeae2.crafting.CraftingComputeTuning;
import com.ghostipedia.nebulaeae2.crafting.CraftingCpuSelection;
import com.ghostipedia.nebulaeae2.crafting.CraftingDispatchBudget;
import com.ghostipedia.nebulaeae2.crafting.CraftingJobState;
import com.ghostipedia.nebulaeae2.crafting.CraftingSubmissionContext;
import com.ghostipedia.nebulaeae2.crafting.api.ICraftingCpuReservation;
import com.ghostipedia.nebulaeae2.crafting.api.IExtendedCraftingCpu;
import com.ghostipedia.nebulaeae2.crafting.api.IExtendedCraftingJob;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(CraftingCpuLogic.class)
public abstract class CraftingCpuLogicReservationMixin implements ICraftingCpuReservation {

    @Shadow
    @Final
    private CraftingCPUCluster cluster;

    @Shadow
    private ExecutingCraftingJob job;

    @Shadow
    @Final
    private int[] usedOps;

    @Shadow
    public abstract boolean isJobSuspended();

    @Unique
    private boolean nebulae$admitting;

    @Unique
    private final CraftingDispatchBudget nebulae$dispatchBudget = new CraftingDispatchBudget();

    @WrapMethod(method = "trySubmitJob")
    private ICraftingSubmitResult nebulae$reserveBeforeAccepting(IGrid grid, ICraftingPlan plan, IActionSource source,
            @Nullable ICraftingRequester requester, Operation<ICraftingSubmitResult> original) {
        if (nebulae$admitting) {
            return CraftingSubmitResult.CPU_BUSY;
        }
        nebulae$admitting = true;
        try {
            if (job != null || !cluster.isActive() || cluster.getAvailableStorage() < plan.bytes()) {
                return original.call(grid, plan, source, requester);
            }
            if (cluster.getGrid() != grid || cluster.getNode() == null) {
                return CraftingSubmitResult.CPU_OFFLINE;
            }
            IComputeService compute = grid.getService(IComputeService.class);
            CraftingJobState submitted = new CraftingJobState(plan.bytes(), CraftingSubmissionContext.followed());
            if (!compute.tryReserveCrafting(cluster.getNode(), submitted.reservationId(),
                    CraftingCpuSelection.reservation(cluster, plan.bytes()))) {
                return CraftingAdmissionFailure.INSUFFICIENT_CWU;
            }
            CraftingJobState previous = CraftingSubmissionContext.setCurrent(submitted);
            try {
                if (cluster.getGrid() != grid || !cluster.isActive()) {
                    return CraftingSubmitResult.CPU_OFFLINE;
                }
                return original.call(grid, plan, source, requester);
            } finally {
                CraftingSubmissionContext.setCurrent(previous);
                compute.finishCraftingAdmission(submitted.reservationId());
            }
        } finally {
            nebulae$admitting = false;
        }
    }

    @Inject(method = "tickCraftingLogic", at = @At("HEAD"))
    private void nebulae$advanceDuration(IEnergyService energy, CraftingService service, CallbackInfo ci) {
        Arrays.fill(usedOps, 0);
        nebulae$dispatchBudget.beginTick();
        CraftingJobState state = nebulae$getJobState();
        if (state != null) {
            state.tick(cluster.isActive() && !isJobSuspended() && !nebulae$isComputePaused(), System.nanoTime());
        }
    }

    @Inject(method = "tickCraftingLogic", at = @At(value = "INVOKE",
            target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;getCoProcessors()I"), cancellable = true)
    private void nebulae$openDispatchOpportunity(IEnergyService energy, CraftingService service, CallbackInfo ci) {
        long tick = cluster.getLevel().getGameTime();
        CpuCapability hardware = ((IExtendedCraftingCpu) (Object) cluster).nebulae$getCapability();
        if (!CraftingComputeTuning.validCoreCounts(hardware.accelerationCores(), hardware.parallelCores())
                || !nebulae$dispatchBudget.open(tick, !nebulae$isComputePaused(),
                        CraftingComputeTuning.dispatchIntervalTicks(hardware.accelerationCores()),
                        CraftingComputeTuning.executionsPerOpportunity(hardware.parallelCores()))) {
            ci.cancel();
        }
    }

    @Redirect(method = "tickCraftingLogic", at = @At(value = "INVOKE",
            target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;getCoProcessors()I"))
    private int nebulae$completeExecutionAllowance(CraftingCPUCluster cpu) {
        return Math.max(0, nebulae$dispatchBudget.remaining() - 1);
    }

    @Inject(method = "executeCrafting", at = @At("HEAD"), cancellable = true)
    private void nebulae$guardDirectDispatch(int maximum, CraftingService service, IEnergyService energy, Level level,
            CallbackInfoReturnable<Integer> cir) {
        if (!nebulae$dispatchBudget.canDispatch(level.getGameTime())) {
            cir.setReturnValue(0);
        }
    }

    @WrapOperation(method = "executeCrafting", at = @At(value = "INVOKE",
            target = "Lappeng/api/networking/crafting/ICraftingProvider;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z"))
    private boolean nebulae$boundWholeExecutions(ICraftingProvider provider, IPatternDetails pattern, KeyCounter[] inputs,
            Operation<Boolean> original) {
        return nebulae$dispatchBudget.tryDispatch(cluster.getLevel().getGameTime(),
                () -> original.call(provider, pattern, inputs));
    }

    @Inject(method = "finishJob", at = @At("HEAD"))
    private void nebulae$stopDuration(boolean success, CallbackInfo ci) {
        CraftingJobState state = nebulae$getJobState();
        if (state != null) {
            state.tick(false, System.nanoTime());
        }
    }

    @Inject(method = {"finishJob", "readFromNBT"}, at = @At("RETURN"))
    private void nebulae$refreshJobReservation(CallbackInfo ci) {
        nebulae$dispatchBudget.reset();
        if (cluster.getGrid() != null) {
            cluster.getGrid().getService(IComputeService.class).invalidateReservations();
        }
    }

    @Inject(method = "writeToNBT", at = @At("HEAD"))
    private void nebulae$saveRunningDuration(CompoundTag data, HolderLookup.Provider registries, CallbackInfo ci) {
        CraftingJobState state = nebulae$getJobState();
        if (state != null) {
            state.tick(cluster.isActive() && !isJobSuspended() && !nebulae$isComputePaused(), System.nanoTime());
        }
    }

    @Override
    public CraftingJobState nebulae$getJobState() {
        return job == null ? null : ((IExtendedCraftingJob) job).nebulae$getState();
    }

    @Override
    public long nebulae$getReservationCwut() {
        CraftingJobState state = nebulae$getJobState();
        return state == null ? 0 : CraftingCpuSelection.reservation(cluster, state.planBytes());
    }

    @Override
    public boolean nebulae$isComputePaused() {
        return job != null && (cluster.getGrid() == null
                || !cluster.getGrid().getService(IComputeService.class).canDispatchCrafting());
    }
}
