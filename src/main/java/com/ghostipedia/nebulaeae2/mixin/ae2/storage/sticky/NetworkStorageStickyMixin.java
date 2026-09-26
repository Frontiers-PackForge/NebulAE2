package com.ghostipedia.nebulaeae2.mixin.ae2.storage.sticky;

import java.util.List;
import java.util.NavigableMap;

import com.ghostipedia.nebulaeae2.sticky.StickyRouting;
import com.ghostipedia.nebulaeae2.sticky.StickyStorage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NetworkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NetworkStorage.class, remap = false)
public abstract class NetworkStorageStickyMixin {
    @Shadow private boolean mountsInUse;
    @Shadow @Final private NavigableMap<Integer, List<MEStorage>> priorityInventory;
    @Shadow protected abstract boolean isQueuedForRemoval(MEStorage inventory);
    @Shadow protected abstract void flushQueuedOperations();

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void nebulae$routeSticky(AEKey key, long amount, Actionable mode, IActionSource source,
                                     CallbackInfoReturnable<Long> cir) {
        if (mountsInUse || amount <= 0) {
            return;
        }
        long inserted;
        mountsInUse = true;
        try {
            inserted = StickyRouting.insert(priorityInventory.values(), amount, this::isQueuedForRemoval,
                    storage -> storage instanceof StickyStorage sticky && sticky.nebulae$claimsSticky(key),
                    (storage, remaining) -> storage.insert(key, remaining, mode, source));
        } finally {
            mountsInUse = false;
            flushQueuedOperations();
        }
        if (inserted >= 0) {
            cir.setReturnValue(inserted);
        }
    }
}
