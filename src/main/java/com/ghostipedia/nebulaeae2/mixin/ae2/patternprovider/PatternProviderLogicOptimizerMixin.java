package com.ghostipedia.nebulaeae2.mixin.ae2.patternprovider;

import com.ghostipedia.nebulaeae2.optimizer.OptimizationHost;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.helpers.patternprovider.PatternProviderLogic;

@Mixin(PatternProviderLogic.class)
public abstract class PatternProviderLogicOptimizerMixin implements OptimizationHost {
    @Unique private boolean nebulae$allowsOptimization = true;
    @Shadow public abstract void saveChanges();

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void nebulae$saveOptimization(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo callback) {
        tag.putBoolean("nebulaeOptimizationDisabled", !nebulae$allowsOptimization);
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void nebulae$loadOptimization(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo callback) {
        nebulae$allowsOptimization = !tag.getBoolean("nebulaeOptimizationDisabled");
    }

    @Override
    public boolean nebulae$allowsOptimization() {
        return nebulae$allowsOptimization;
    }

    @Override
    public void nebulae$setAllowsOptimization(boolean allowed) {
        nebulae$allowsOptimization = allowed;
        saveChanges();
    }
}
