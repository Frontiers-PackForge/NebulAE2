package com.ghostipedia.nebulaeae2.mixin.ae2.pathing;

import com.ghostipedia.nebulaeae2.channel.ChannelOverloadPolicy;

import appeng.me.GridNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GridNode.class)
public abstract class GridNodeSoftChannelMixin {

    @Redirect(
            method = "propagateChannelsUpwards",
            at = @At(value = "INVOKE", target = "Lappeng/me/GridNode;getMaxChannels()I"))
    private int nebulae$acceptAllocatedOverload(GridNode node) {
        return ChannelOverloadPolicy.allocationLimit(node.getMaxChannels());
    }
}
