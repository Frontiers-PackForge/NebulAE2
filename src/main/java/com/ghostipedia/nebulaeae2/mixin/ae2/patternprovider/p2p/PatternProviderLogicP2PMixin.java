package com.ghostipedia.nebulaeae2.mixin.ae2.patternprovider.p2p;

import java.util.List;
import java.util.UUID;

import com.ghostipedia.nebulaeae2.blocking.ProviderBlockingSettings;
import com.ghostipedia.nebulaeae2.blocking.StorageBlockingTarget;
import com.ghostipedia.nebulaeae2.p2p.PatternProviderRoute;
import com.ghostipedia.nebulaeae2.p2p.PatternProviderRouteHost;
import com.ghostipedia.nebulaeae2.p2p.PatternP2PTunnelPart;
import com.ghostipedia.nebulaeae2.p2p.PatternRouteAvailability;
import com.ghostipedia.nebulaeae2.p2p.PatternTunnelAddress;
import com.ghostipedia.nebulaeae2.p2p.PatternTunnelRouting;
import com.ghostipedia.nebulaeae2.p2p.PendingPatternRoutes;
import com.ghostipedia.nebulaeae2.p2p.RoutedPatternTarget;

import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.IConfigManager;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.parts.AEBasePart;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatternProviderLogic.class)
public abstract class PatternProviderLogicP2PMixin implements PatternProviderRouteHost {
    @Shadow @Final private PatternProviderLogicHost host;
    @Shadow @Final private IManagedGridNode mainNode;
    @Shadow @Final private IActionSource actionSource;
    @Shadow @Final private IConfigManager configManager;
    @Shadow @Final private List<GenericStack> sendList;
    @Unique private UUID nebulae$providerIdentity = UUID.randomUUID();
    @Unique private PatternProviderRoute nebulae$route;

    @Override
    public UUID nebulae$providerIdentity() {
        return nebulae$providerIdentity;
    }

    @Override
    public PatternProviderRoute nebulae$pendingRoute() {
        return nebulae$route;
    }

    @Override
    public void nebulae$setRoute(PatternProviderRoute route) {
        nebulae$route = route;
        host.saveChanges();
    }

    @Inject(method = "findAdapter", at = @At("HEAD"), cancellable = true)
    private void nebulae$remoteAdapter(Direction side, CallbackInfoReturnable<PatternProviderTarget> callback) {
        var level = host.getBlockEntity().getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        if (nebulae$route != null) {
            callback.setReturnValue(nebulae$resolvePending());
            return;
        }
        var address = new PatternTunnelAddress(level.dimension(), host.getBlockEntity().getBlockPos().relative(side), side.getOpposite());
        var input = PatternTunnelRouting.tunnelAt(address, level.getServer());
        if (input != null) {
            Direction providerSide = host instanceof AEBasePart part ? part.getSide() : null;
            var providerAddress = new PatternTunnelAddress(level.dimension(), host.getBlockEntity().getBlockPos(), providerSide);
            callback.setReturnValue(new RoutedPatternTarget(input, this, providerAddress,
                    configManager.getSetting(ProviderBlockingSettings.MODE), actionSource));
        }
    }

    @Inject(method = "adapterAcceptsAll", at = @At("HEAD"), cancellable = true)
    private void nebulae$selectRemote(PatternProviderTarget target, KeyCounter[] ingredients, CallbackInfoReturnable<Boolean> callback) {
        if (target instanceof RoutedPatternTarget routed) {
            callback.setReturnValue(routed.prepare(ingredients));
        }
    }

    @Unique
    private PatternProviderTarget nebulae$resolvePending() {
        var server = host.getBlockEntity().getLevel().getServer();
        var route = nebulae$route;
        if (!route.recovering()) {
            var input = PatternTunnelRouting.tunnelAt(route.input(), server);
            var output = PatternTunnelRouting.tunnelAt(route.output(), server);
            var inputState = nebulae$endpointState(route.input().loaded(server), input, route.inputIdentity());
            var outputState = nebulae$endpointState(route.output().loaded(server), output, route.outputIdentity());
            boolean connected = input != null && output != null && input.getMainNode().getGrid() == output.getMainNode().getGrid();
            var availability = PatternRouteAvailability.resolve(inputState, outputState, connected);
            if (availability == PatternRouteAvailability.RECOVER) {
                nebulae$setRoute(route.recover());
                PendingPatternRoutes.get(server).release(route.owner());
            } else if (availability == PatternRouteAvailability.WAIT) {
                return null;
            }
        }
        if (nebulae$route.recovering()) {
            var grid = mainNode.getGrid();
            return grid == null || !mainNode.isActive() ? null : new StorageBlockingTarget(grid.getStorageService().getInventory(), actionSource);
        }
        PendingPatternRoutes.get(server).claim(nebulae$route);
        var destination = route.destination();
        return destination.loaded(server)
                ? PatternProviderTarget.get(destination.level(server), destination.position(), null, destination.side(), actionSource)
                : null;
    }

    @Unique
    private PatternRouteAvailability.Endpoint nebulae$endpointState(boolean loaded, PatternP2PTunnelPart part, UUID identity) {
        if (!loaded) {
            return PatternRouteAvailability.Endpoint.UNLOADED;
        }
        if (part == null || !part.identity().equals(identity)) {
            return PatternRouteAvailability.Endpoint.REMOVED;
        }
        return part.getMainNode().isActive() ? PatternRouteAvailability.Endpoint.ACTIVE : PatternRouteAvailability.Endpoint.INACTIVE;
    }

    @Inject(method = "sendStacksOut", at = @At("RETURN"))
    private void nebulae$finishDelivery(CallbackInfoReturnable<Boolean> callback) {
        if (nebulae$route != null) {
            if (sendList.isEmpty()) {
                nebulae$release();
            } else if (callback.getReturnValueZ()) {
                host.saveChanges();
            }
        }
    }

    @Unique
    private void nebulae$release() {
        var level = host.getBlockEntity().getLevel();
        if (level != null && !level.isClientSide()) {
            PendingPatternRoutes.get(level.getServer()).release(nebulae$providerIdentity);
        }
        nebulae$setRoute(null);
    }

    @Inject(method = "clearContent", at = @At("TAIL"))
    private void nebulae$clearRoute(CallbackInfo callback) {
        if (nebulae$route != null) {
            nebulae$release();
        }
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void nebulae$writeRoute(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo callback) {
        tag.putUUID("nebulaePatternProviderIdentity", nebulae$providerIdentity);
        if (nebulae$route != null) {
            tag.put("nebulaePatternRoute", nebulae$route.save());
        }
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void nebulae$readRoute(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo callback) {
        if (tag.hasUUID("nebulaePatternProviderIdentity")) {
            nebulae$providerIdentity = tag.getUUID("nebulaePatternProviderIdentity");
        }
        nebulae$route = tag.contains("nebulaePatternRoute") && !sendList.isEmpty()
                ? PatternProviderRoute.load(tag.getCompound("nebulaePatternRoute")) : null;
    }
}
