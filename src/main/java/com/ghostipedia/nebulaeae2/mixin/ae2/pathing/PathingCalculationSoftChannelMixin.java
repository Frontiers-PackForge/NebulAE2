package com.ghostipedia.nebulaeae2.mixin.ae2.pathing;

import com.ghostipedia.nebulaeae2.channel.ChannelOverloadPolicy;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;

import appeng.api.networking.IGrid;
import appeng.me.GridNode;
import appeng.me.pathfinding.PathingCalculation;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathingCalculation.class)
public abstract class PathingCalculationSoftChannelMixin {

    @Shadow
    @Final
    private IGrid grid;

    @Unique
    private long nebulae$channelOverloadCwut;

    @Inject(method = "compute", at = @At("HEAD"))
    private void nebulae$resetChannelOverload(CallbackInfo callback) {
        nebulae$channelOverloadCwut = 0;
    }

    @Redirect(
            method = "tryUseChannel",
            at = @At(value = "INVOKE", target = "Lappeng/me/GridNode;getMaxChannels()I"))
    private int nebulae$allowControlledGridOverload(GridNode node) {
        return ChannelOverloadPolicy.allocationLimit(node.getMaxChannels());
    }

    @Redirect(
            method = "tryUseChannel",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/Reference2IntOpenHashMap;addTo(Ljava/lang/Object;I)I",
                    remap = false))
    private int nebulae$recordChannelOverload(
            Reference2IntOpenHashMap<GridNode> channelBottlenecks,
            Object key,
            int increment) {
        GridNode node = (GridNode) key;
        int previousChannels = channelBottlenecks.addTo(node, increment);
        long incrementalCwut = ChannelOverloadPolicy.incrementalCwut(
                previousChannels + increment,
                node.getMaxChannels());
        nebulae$channelOverloadCwut = saturatingAdd(nebulae$channelOverloadCwut, incrementalCwut);
        return previousChannels;
    }

    @Inject(method = "compute", at = @At("RETURN"))
    private void nebulae$publishChannelOverload(CallbackInfo callback) {
        grid.getService(IComputeService.class).updateChannelOverloadReservation(nebulae$channelOverloadCwut);
    }

    @Unique
    private static long saturatingAdd(long left, long right) {
        if (right > 0 && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}
