package com.ghostipedia.nebulaeae2.mixin.ae2.activity;

import com.ghostipedia.nebulaeae2.activity.ActivityDelivery;
import com.ghostipedia.nebulaeae2.activity.ActivityStorageBinding;
import com.ghostipedia.nebulaeae2.activity.IActivityService;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.me.storage.NetworkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NetworkStorage.class)
public abstract class NetworkStorageActivityMixin implements ActivityStorageBinding {
    @Unique private IActivityService nebulae$activity;

    @Override
    public void nebulae$bindActivity(IActivityService service) { nebulae$activity = service; }

    @WrapMethod(method = "insert")
    private long nebulae$incoming(AEKey key, long amount, Actionable mode, IActionSource source, Operation<Long> original) {
        if (mode != Actionable.MODULATE) return original.call(key, amount, mode, source);
        try (var delivery = ActivityDelivery.begin()) {
            long accepted = original.call(key, amount, mode, source);
            delivery.accepted(accepted);
            nebulae$record(key, accepted, true);
            return accepted;
        }
    }

    @WrapMethod(method = "extract")
    private long nebulae$outgoing(AEKey key, long amount, Actionable mode, IActionSource source, Operation<Long> original) {
        long accepted = original.call(key, amount, mode, source);
        if (mode == Actionable.MODULATE) nebulae$record(key, accepted, false);
        return accepted;
    }

    @Unique
    private void nebulae$record(AEKey key, long amount, boolean incoming) {
        if (amount > 0 && nebulae$activity != null && nebulae$activity.archive() != null) {
            nebulae$activity.archive().recordFlow(nebulae$activity.writableSegment(), key, amount, incoming);
        }
    }
}
