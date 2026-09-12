package com.ghostipedia.nebulaeae2.mixin.ae2.crafting;

import com.ghostipedia.nebulaeae2.crafting.CpuCapability;
import com.ghostipedia.nebulaeae2.crafting.api.ICraftingCpuComponent;
import com.ghostipedia.nebulaeae2.crafting.api.IExtendedCraftingCpu;

import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingCPUCluster.class)
public abstract class CraftingCPUClusterCapabilityMixin implements IExtendedCraftingCpu {
    @Shadow
    private long storage;

    @Unique
    private int nebulae$accelerationCores;

    @Unique
    private int nebulae$parallelCores;

    @Inject(method = "addBlockEntity", at = @At("TAIL"))
    private void nebulae$countComponents(CraftingBlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity.getBlockState().getBlock() instanceof ICraftingCpuComponent component) {
            nebulae$accelerationCores += component.accelerationCores();
            nebulae$parallelCores += component.parallelCores();
        }
    }

    @Redirect(method = "addBlockEntity", at = @At(value = "INVOKE", target = "Lappeng/blockentity/crafting/CraftingBlockEntity;getAcceleratorThreads()I"))
    private int nebulae$retireNativeThreads(CraftingBlockEntity blockEntity) {
        return 0;
    }

    @Override
    public CpuCapability nebulae$getCapability() {
        return new CpuCapability(nebulae$accelerationCores, nebulae$parallelCores, storage);
    }
}
