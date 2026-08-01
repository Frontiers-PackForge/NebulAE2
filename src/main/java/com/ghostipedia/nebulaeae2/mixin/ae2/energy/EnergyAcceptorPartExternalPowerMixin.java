package com.ghostipedia.nebulaeae2.mixin.ae2.energy;

import appeng.parts.networking.EnergyAcceptorPart;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnergyAcceptorPart.class)
public abstract class EnergyAcceptorPartExternalPowerMixin {

    @Inject(method = "getEnergyStorage", at = @At("HEAD"), cancellable = true)
    private void nebulaeae2$disableExternalPower(CallbackInfoReturnable<IEnergyStorage> callback) {
        callback.setReturnValue(null);
    }
}
