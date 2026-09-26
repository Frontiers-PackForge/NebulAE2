package com.ghostipedia.nebulaeae2.mixin.ae2.storage.sticky;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import com.ghostipedia.nebulaeae2.sticky.StickyStorage;
import com.ghostipedia.nebulaeae2.sticky.StickyStorageConfiguration;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.me.storage.DelegatingMEInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DelegatingMEInventory.class, remap = false)
public abstract class DelegatingInventoryStickyMixin implements StickyStorage, StickyStorageConfiguration {
    @Shadow protected abstract MEStorage getDelegate();
    @Unique private Predicate<AEKey> nebulae$stickyClaims;
    @Unique private BooleanSupplier nebulae$stickyInvalid;

    @Override
    public void nebulae$configureSticky(Predicate<AEKey> claims, BooleanSupplier invalid) {
        nebulae$stickyClaims = claims;
        nebulae$stickyInvalid = invalid;
    }

    @Override
    public boolean nebulae$isStickyInvalid() {
        return nebulae$stickyInvalid != null && nebulae$stickyInvalid.getAsBoolean();
    }

    @Override
    public boolean nebulae$claimsSticky(AEKey key) {
        if (nebulae$stickyClaims != null) {
            return nebulae$stickyClaims.test(key);
        }
        return getDelegate() instanceof StickyStorage sticky && sticky.nebulae$claimsSticky(key);
    }

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void nebulae$rejectInvalid(AEKey key, long amount, Actionable mode, IActionSource source,
                                       CallbackInfoReturnable<Long> cir) {
        if (nebulae$isStickyInvalid()) {
            cir.setReturnValue(0L);
        }
    }
}
