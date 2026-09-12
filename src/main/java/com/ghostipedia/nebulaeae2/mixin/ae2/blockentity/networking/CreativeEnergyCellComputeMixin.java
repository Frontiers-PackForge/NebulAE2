package com.ghostipedia.nebulaeae2.mixin.ae2.blockentity.networking;

import com.ghostipedia.nebulaeae2.compute.CreativeComputeSource;
import com.ghostipedia.nebulaeae2.compute.api.IComputeSource;
import appeng.blockentity.networking.CreativeEnergyCellBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeEnergyCellBlockEntity.class)
public abstract class CreativeEnergyCellComputeMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void nebulae$addCreativeCompute(CallbackInfo callback) {
        ((CreativeEnergyCellBlockEntity) (Object) this).getMainNode()
                .addService(IComputeSource.class, new CreativeComputeSource());
    }
}
