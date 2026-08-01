package com.ghostipedia.nebulaeae2.mixin.ae2.pathing;

import com.ghostipedia.nebulaeae2.compute.api.IComputeService;

import appeng.api.networking.IGrid;
import appeng.api.networking.pathing.ChannelMode;
import appeng.me.Grid;
import appeng.me.service.PathingService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathingService.class)
public abstract class PathingServiceSoftChannelMixin {

    @Shadow
    @Final
    private Grid grid;

    @Shadow
    private boolean booting;

    @Shadow
    private ChannelMode channelMode;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void nebulae$initializeChannelMode(IGrid grid, CallbackInfo callback) {
        channelMode = ChannelMode.X2;
    }

    @Inject(method = "repath", at = @At("RETURN"))
    private void nebulae$prepareRepath(CallbackInfo callback) {
        channelMode = ChannelMode.X2;
        grid.getService(IComputeService.class).clearChannelOverloadReservation();
    }

    @Inject(method = "postBootingStatusChange", at = @At("HEAD"))
    private void nebulae$clearAtBootStart(CallbackInfo callback) {
        if (booting) {
            grid.getService(IComputeService.class).clearChannelOverloadReservation();
        }
    }

    @Inject(method = "getChannelMode", at = @At("HEAD"), cancellable = true)
    private void nebulae$useSoftChannelRatings(CallbackInfoReturnable<ChannelMode> callback) {
        callback.setReturnValue(ChannelMode.X2);
    }
}
