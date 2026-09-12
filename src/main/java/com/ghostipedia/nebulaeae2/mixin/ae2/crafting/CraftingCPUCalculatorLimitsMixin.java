package com.ghostipedia.nebulaeae2.mixin.ae2.crafting;

import com.ghostipedia.nebulaeae2.crafting.CraftingComputeTuning;
import com.ghostipedia.nebulaeae2.crafting.api.ICraftingCpuComponent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import appeng.me.cluster.implementations.CraftingCPUCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingCPUCalculator.class)
public class CraftingCPUCalculatorLimitsMixin {
    @Inject(method = "verifyInternalStructure", at = @At("RETURN"), cancellable = true)
    private void nebulae$checkCoreLimits(ServerLevel level, BlockPos min, BlockPos max,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }
        int accelerationCores = 0;
        int parallelCores = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(pos).getBlock() instanceof ICraftingCpuComponent component) {
                accelerationCores += component.accelerationCores();
                parallelCores += component.parallelCores();
                if (!CraftingComputeTuning.validCoreCounts(accelerationCores, parallelCores)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }
}
