package com.ghostipedia.nebulaeae2.mixin.ae2.crafting;

import com.ghostipedia.nebulaeae2.crafting.CraftingJobState;
import com.ghostipedia.nebulaeae2.crafting.CraftingSubmissionContext;
import com.ghostipedia.nebulaeae2.crafting.api.IExtendedCraftingJob;

import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExecutingCraftingJob.class)
public abstract class ExecutingCraftingJobStateMixin implements IExtendedCraftingJob {

    @Unique
    private CraftingJobState nebulae$state;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void nebulae$initialize(CallbackInfo ci) {
        if (nebulae$state == null) {
            CraftingJobState submitted = CraftingSubmissionContext.current();
            nebulae$state = submitted == null ? new CraftingJobState(0, false) : submitted;
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lappeng/crafting/execution/ExecutingCraftingJob$CraftingDifferenceListener;Lappeng/crafting/execution/CraftingCpuLogic;)V", at = @At("RETURN"))
    private void nebulae$read(CompoundTag data, HolderLookup.Provider registries, @Coerce Object listener,
            CraftingCpuLogic cpu, CallbackInfo ci) {
        nebulae$state = CraftingJobState.load(data.getCompound("nebulae"),
                ((CraftingCpuLogicAccessor) cpu).nebulae$getCluster().getAvailableStorage());
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void nebulae$write(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().put("nebulae", nebulae$state.save());
    }

    @Override
    public CraftingJobState nebulae$getState() {
        return nebulae$state;
    }
}
