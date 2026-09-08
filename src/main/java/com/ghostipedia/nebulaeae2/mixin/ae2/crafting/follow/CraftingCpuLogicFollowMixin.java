package com.ghostipedia.nebulaeae2.mixin.ae2.crafting.follow;

import com.ghostipedia.nebulaeae2.crafting.api.IExtendedCraftingJob;
import com.ghostipedia.nebulaeae2.crafting.follow.CraftFinishedPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.features.IPlayerRegistry;
import appeng.core.network.clientbound.CraftingJobStatusPacket;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.me.cluster.implementations.CraftingCPUCluster;

@Mixin(CraftingCpuLogic.class)
public abstract class CraftingCpuLogicFollowMixin {
    @Shadow @Final private CraftingCPUCluster cluster;

    @Inject(method = "notifyJobOwner", at = @At("RETURN"))
    private void nebulae$notifyCompletion(ExecutingCraftingJob job, CraftingJobStatusPacket.Status status,
            CallbackInfo callback) {
        if (status != CraftingJobStatusPacket.Status.FINISHED) {
            return;
        }
        var accessor = (ExecutingCraftingJobFollowAccessor) job;
        var playerId = accessor.nebulae$playerId();
        if (playerId == null || accessor.nebulae$finalOutput() == null) {
            return;
        }
        var owner = IPlayerRegistry.getConnected(cluster.getLevel().getServer(), playerId);
        if (owner != null) {
            var state = ((IExtendedCraftingJob) job).nebulae$getState();
            PacketDistributor.sendToPlayer(owner, new CraftFinishedPayload(accessor.nebulae$link().getCraftingID(),
                    accessor.nebulae$finalOutput(), state.elapsedNanos(), state.followed()));
        }
    }
}
