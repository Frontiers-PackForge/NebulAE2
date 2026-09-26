package com.ghostipedia.nebulaeae2.mixin.ae2.crafting.optimizer;

import java.util.Collection;

import com.ghostipedia.nebulaeae2.optimizer.OptimizerSimulation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.me.service.CraftingService;

@Mixin(CraftingService.class)
public abstract class CraftingServiceOptimizerMixin {
    @Inject(method = "getCraftingFor", at = @At("RETURN"), cancellable = true)
    private void nebulae$previewPatterns(AEKey key, CallbackInfoReturnable<Collection<IPatternDetails>> callback) {
        callback.setReturnValue(OptimizerSimulation.patterns((ICraftingService) this, key, callback.getReturnValue()));
    }
}
