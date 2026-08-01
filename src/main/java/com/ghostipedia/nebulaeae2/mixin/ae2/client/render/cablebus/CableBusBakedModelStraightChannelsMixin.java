package com.ghostipedia.nebulaeae2.mixin.ae2.client.render.cablebus;

import com.llamalad7.mixinextras.sugar.Local;

import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableBusRenderState;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CableBusBakedModel.class)
public abstract class CableBusBakedModelStraightChannelsMixin {

    @ModifyArgs(
            method = "addCableQuads(Lappeng/client/render/cablebus/CableBusRenderState;Ljava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CableBuilder;addStraightSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V"),
            require = 1)
    private void nebulae$packStraightSmartChannels(Args invocation,
                                                   @Local(argsOnly = true) CableBusRenderState renderState) {
        nebulae$packChannels(invocation, renderState);
    }

    @ModifyArgs(
            method = "addCableQuads(Lappeng/client/render/cablebus/CableBusRenderState;Ljava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/render/cablebus/CableBuilder;addStraightDenseSmartConnection(Lnet/minecraft/core/Direction;Lappeng/api/util/AEColor;ILjava/util/List;)V"),
            require = 1)
    private void nebulae$packStraightDenseSmartChannels(Args invocation,
                                                        @Local(argsOnly = true) CableBusRenderState renderState) {
        nebulae$packChannels(invocation, renderState);
    }

    @Unique
    private static void nebulae$packChannels(Args invocation, CableBusRenderState renderState) {
        Direction facing = invocation.get(0);
        int channels = invocation.get(2);
        int oppositeChannels = renderState.getChannelsOnSide().getOrDefault(facing.getOpposite(), 0);
        invocation.set(2, channels & 0xFF | (oppositeChannels & 0xFF) << 8);
    }
}
