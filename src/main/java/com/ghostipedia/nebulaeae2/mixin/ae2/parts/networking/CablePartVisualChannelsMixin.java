package com.ghostipedia.nebulaeae2.mixin.ae2.parts.networking;

import com.ghostipedia.nebulaeae2.channel.ChannelOverloadPolicy;

import appeng.api.util.AECableType;
import appeng.parts.networking.CablePart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CablePart.class)
public abstract class CablePartVisualChannelsMixin {

    @Inject(method = "getVisualChannels(I)B", at = @At("RETURN"), cancellable = true, require = 1)
    private void nebulae$encodeOverloadBand(int usedChannels, CallbackInfoReturnable<Byte> cir) {
        AECableType cableType = ((CablePart) (Object) this).getCableConnectionType();
        int rating = switch (cableType) {
            case GLASS, COVERED, SMART -> ChannelOverloadPolicy.STANDARD_RATING;
            case DENSE_COVERED, DENSE_SMART -> ChannelOverloadPolicy.DENSE_RATING;
            case NONE -> 0;
        };
        int overloadBand = ChannelOverloadPolicy.overloadBand(usedChannels, rating);
        cir.setReturnValue((byte) ((cir.getReturnValue() & 0x3F) | overloadBand << 6));
    }
}
