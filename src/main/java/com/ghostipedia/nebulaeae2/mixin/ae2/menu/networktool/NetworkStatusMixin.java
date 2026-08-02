package com.ghostipedia.nebulaeae2.mixin.ae2.menu.networktool;

import net.minecraft.network.RegistryFriendlyByteBuf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ghostipedia.nebulaeae2.compute.api.ComputeSnapshot;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import com.ghostipedia.nebulaeae2.compute.network.NetworkStatusComputeExtension;

import appeng.api.networking.IGrid;
import appeng.menu.me.networktool.NetworkStatus;

@Mixin(NetworkStatus.class)
public abstract class NetworkStatusMixin implements NetworkStatusComputeExtension {

    @Unique
    private ComputeSnapshot nebulae$computeSnapshot = EMPTY_SNAPSHOT;

    @Inject(method = "fromGrid", at = @At("RETURN"))
    private static void nebulae$captureComputeSnapshot(IGrid grid,
            CallbackInfoReturnable<NetworkStatus> callback) {
        var extension = (NetworkStatusComputeExtension) (Object) callback.getReturnValue();
        extension.nebulae$setComputeSnapshot(grid.getService(IComputeService.class).snapshot());
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void nebulae$readComputeSnapshot(RegistryFriendlyByteBuf data,
            CallbackInfoReturnable<NetworkStatus> callback) {
        var snapshot = new ComputeSnapshot(
                data.readLong(),
                data.readLong(),
                data.readLong(),
                data.readLong(),
                data.readLong(),
                data.readLong(),
                data.readLong(),
                data.readDouble(),
                data.readLong(),
                data.readLong(),
                data.readLong(),
                data.readLong(),
                data.readVarInt(),
                data.readVarInt(),
                data.readLong(),
                data.readLong(),
                data.readLong());
        var extension = (NetworkStatusComputeExtension) (Object) callback.getReturnValue();
        extension.nebulae$setComputeSnapshot(snapshot);
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void nebulae$writeComputeSnapshot(RegistryFriendlyByteBuf data, CallbackInfo callback) {
        data.writeLong(nebulae$computeSnapshot.capacityCwut());
        data.writeLong(nebulae$computeSnapshot.fundedCwut());
        data.writeLong(nebulae$computeSnapshot.reservedCwut());
        data.writeLong(nebulae$computeSnapshot.passiveShortfallCwut());
        data.writeLong(nebulae$computeSnapshot.channelOverloadCwut());
        data.writeLong(nebulae$computeSnapshot.workBudgetCwut());
        data.writeLong(nebulae$computeSnapshot.workUsedCwut());
        data.writeDouble(nebulae$computeSnapshot.recentWorkAverageCwut());
        data.writeLong(nebulae$computeSnapshot.recentWorkPeakCwut());
        data.writeLong(nebulae$computeSnapshot.recoveryBudgetCwut());
        data.writeLong(nebulae$computeSnapshot.recoveryUsedCwut());
        data.writeLong(nebulae$computeSnapshot.debtCwu());
        data.writeVarInt(nebulae$computeSnapshot.sourceCount());
        data.writeVarInt(nebulae$computeSnapshot.trackedNodeCount());
        data.writeLong(nebulae$computeSnapshot.channelDeviceCount());
        data.writeLong(nebulae$computeSnapshot.recentThrottledOperations());
        data.writeLong(nebulae$computeSnapshot.recentRecoveryOperations());
    }

    @Override
    public ComputeSnapshot nebulae$getComputeSnapshot() {
        return nebulae$computeSnapshot;
    }

    @Override
    public void nebulae$setComputeSnapshot(ComputeSnapshot snapshot) {
        nebulae$computeSnapshot = snapshot;
    }
}
