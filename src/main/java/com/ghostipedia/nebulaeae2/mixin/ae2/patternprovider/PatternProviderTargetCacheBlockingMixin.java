package com.ghostipedia.nebulaeae2.mixin.ae2.patternprovider;

import com.ghostipedia.nebulaeae2.blocking.StorageBlockingTarget;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "appeng.helpers.patternprovider.PatternProviderTargetCache")
public abstract class PatternProviderTargetCacheBlockingMixin {
    @Shadow @Final private IActionSource src;

    @Inject(method = "wrapMeStorage", at = @At("HEAD"), cancellable = true)
    private void nebulae$wrapStorage(MEStorage storage, CallbackInfoReturnable<PatternProviderTarget> callback) {
        callback.setReturnValue(new StorageBlockingTarget(storage, src));
    }
}
