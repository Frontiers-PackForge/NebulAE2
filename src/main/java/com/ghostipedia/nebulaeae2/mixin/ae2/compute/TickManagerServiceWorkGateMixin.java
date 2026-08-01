package com.ghostipedia.nebulaeae2.mixin.ae2.compute;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGrid;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.me.service.TickManagerService;
import appeng.me.service.helpers.TickTracker;
import com.ghostipedia.nebulaeae2.compute.ComputeTuning;
import com.ghostipedia.nebulaeae2.compute.NodeWorkloadClassifier;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TickManagerService.class)
public abstract class TickManagerServiceWorkGateMixin {

    @Inject(method = "unsafeTickingRequest", at = @At("HEAD"), cancellable = true)
    private void nebulaeae2$gateScheduledWork(
            TickTracker tracker, int elapsedTicks, CallbackInfoReturnable<TickRateModulation> callback) {
        IGridNode node = tracker.getNode();
        if (!NodeWorkloadClassifier.requiresScheduledWorkGrant(node)) {
            return;
        }

        IGrid grid;
        try {
            grid = node.getGrid();
        } catch (IllegalStateException ignored) {
            callback.setReturnValue(TickRateModulation.SLOWER);
            return;
        }
        if (!grid.getService(IComputeService.class).tryAcquire(node, ComputeTuning.SCHEDULED_WORK_CWU)) {
            callback.setReturnValue(TickRateModulation.SLOWER);
        }
    }
}
