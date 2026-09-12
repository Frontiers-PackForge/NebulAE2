package com.ghostipedia.nebulaeae2.mixin.merequester;

import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import appeng.api.networking.security.IActionHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.almostreliable.merequester.requester.status.RequestState", remap = false)
public abstract class RequestStateComputePreflightMixin {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void nebulae$deferPlanning(@Coerce Object requester, int slot, CallbackInfoReturnable<Object> callback) {
        if (requester instanceof IActionHost host) {
            var node = host.getActionableNode();
            if (node != null && !node.getGrid().getService(IComputeService.class).canAdmitCrafting()) {
                callback.setReturnValue(this);
            }
        }
    }
}
