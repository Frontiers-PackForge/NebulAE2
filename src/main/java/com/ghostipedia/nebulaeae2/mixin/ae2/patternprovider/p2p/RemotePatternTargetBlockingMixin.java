package com.ghostipedia.nebulaeae2.mixin.ae2.patternprovider.p2p;

import com.ghostipedia.nebulaeae2.blocking.StorageBlockingTarget;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatternProviderTarget.class)
public interface RemotePatternTargetBlockingMixin {
    @Inject(method = "wrapMeStorage", at = @At("HEAD"), cancellable = true)
    private static void nebulae$remoteBlocking(MEStorage storage, IActionSource source, CallbackInfoReturnable<PatternProviderTarget> callback) {
        callback.setReturnValue(new StorageBlockingTarget(storage, source));
    }
}
