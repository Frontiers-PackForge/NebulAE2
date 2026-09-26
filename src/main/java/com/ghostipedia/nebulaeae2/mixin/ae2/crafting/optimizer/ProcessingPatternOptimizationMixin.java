package com.ghostipedia.nebulaeae2.mixin.ae2.crafting.optimizer;

import java.util.List;

import com.ghostipedia.nebulaeae2.optimizer.PatternOptimization;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;

@Mixin(AEProcessingPattern.class)
public abstract class ProcessingPatternOptimizationMixin {
    @Inject(method = "encode", at = @At("HEAD"))
    private static void nebulae$invalidateEditedOptimization(ItemStack stack, List<GenericStack> inputs,
            List<GenericStack> outputs, CallbackInfo callback) {
        var provenance = stack.get(PatternOptimization.PROVENANCE);
        if (provenance != null && (!provenance.expected().sparseInputs().equals(inputs)
                || !provenance.expected().sparseOutputs().equals(outputs))) {
            stack.remove(PatternOptimization.PROVENANCE);
        }
    }
}
