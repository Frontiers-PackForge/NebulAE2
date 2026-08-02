package com.ghostipedia.nebulaeae2.mixin.ae2.config;

import appeng.api.networking.pathing.ChannelMode;
import appeng.core.AEConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AEConfig.class)
public abstract class AEConfigChannelModeMixin {

    @Inject(method = "getChannelMode", at = @At("HEAD"), cancellable = true)
    private void nebulae$useFrontiersChannelMode(CallbackInfoReturnable<ChannelMode> callback) {
        callback.setReturnValue(ChannelMode.X2);
    }

    @Inject(method = "setChannelModel", at = @At("HEAD"), cancellable = true)
    private void nebulae$ignoreChannelModeChanges(ChannelMode mode, CallbackInfo callback) {
        callback.cancel();
    }
}
