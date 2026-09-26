package com.ghostipedia.nebulaeae2.mixin.ae2.storage.sticky;

import com.ghostipedia.nebulaeae2.sticky.StickyStorageConfiguration;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.me.storage.MEInventoryHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MEInventoryHandler.class, remap = false)
public abstract class InventoryHandlerStickyMixin {
    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void nebulae$rejectInvalidBeforeVoid(AEKey key, long amount, Actionable mode, IActionSource source,
                                                 CallbackInfoReturnable<Long> cir) {
        if (((StickyStorageConfiguration) this).nebulae$isStickyInvalid()) {
            cir.setReturnValue(0L);
        }
    }
}
