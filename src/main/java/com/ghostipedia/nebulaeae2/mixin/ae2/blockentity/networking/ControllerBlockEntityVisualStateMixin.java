package com.ghostipedia.nebulaeae2.mixin.ae2.blockentity.networking;

import com.ghostipedia.nebulaeae2.controller.ControllerVisualStateSync;

import appeng.blockentity.networking.ControllerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControllerBlockEntity.class)
public abstract class ControllerBlockEntityVisualStateMixin {

    @Inject(method = "onReady", at = @At("RETURN"))
    private void nebulae$scheduleVisualStateReconciliation(CallbackInfo callback) {
        ControllerBlockEntity controller = (ControllerBlockEntity) (Object) this;
        ControllerVisualStateSync.scheduleInitialReconciliation(controller);
    }
}
