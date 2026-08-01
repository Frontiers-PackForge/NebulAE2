package com.ghostipedia.nebulaeae2.mixin.ae2.energy;

import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.blockentity.networking.EnergyAcceptorBlockEntity;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AEBasePoweredBlockEntity.class)
public abstract class AEBasePoweredBlockEntityExternalPowerMixin {

    @Inject(method = "getEnergyStorage", at = @At("HEAD"), cancellable = true)
    private void nebulaeae2$restrictGridPowerIngress(
            Direction side, CallbackInfoReturnable<IEnergyStorage> callback) {
        Object blockEntity = this;
        if (blockEntity instanceof ControllerBlockEntity || blockEntity instanceof EnergyAcceptorBlockEntity) {
            callback.setReturnValue(null);
        }
    }
}
