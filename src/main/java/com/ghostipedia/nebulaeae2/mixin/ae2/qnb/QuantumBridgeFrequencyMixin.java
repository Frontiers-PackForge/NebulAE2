package com.ghostipedia.nebulaeae2.mixin.ae2.qnb;

import java.util.UUID;

import net.minecraft.world.item.ItemStack;

import appeng.api.ids.AEComponents;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QuantumBridgeBlockEntity.class)
public abstract class QuantumBridgeFrequencyMixin {

    @Inject(method = "assignFrequency", at = @At("HEAD"), cancellable = true)
    private static void nebulae$assignRandomFrequency(ItemStack stack, CallbackInfo callback) {
        long frequency;
        do {
            UUID uuid = UUID.randomUUID();
            frequency = (uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits()) & Long.MAX_VALUE;
        } while (frequency == 0);
        stack.set(AEComponents.ENTANGLED_SINGULARITY_ID, frequency);
        callback.cancel();
    }
}
