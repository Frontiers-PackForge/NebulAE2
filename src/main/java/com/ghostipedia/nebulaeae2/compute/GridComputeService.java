package com.ghostipedia.nebulaeae2.compute;

import com.ghostipedia.nebulaeae2.compute.api.ComputeSnapshot;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import com.ghostipedia.nebulaeae2.compute.api.IComputeSource;
import com.ghostipedia.nebulaeae2.crafting.CraftingReservationLedger;
import com.ghostipedia.nebulaeae2.crafting.CraftingComputeTuning;
import com.ghostipedia.nebulaeae2.crafting.api.ICraftingCpuReservation;
import com.ghostipedia.nebulaeae2.mixin.ae2.compute.EnergyServiceAccessor;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageProvider;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.helpers.InterfaceLogicHost;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.EnergyService;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class GridComputeService implements IComputeService, IGridServiceProvider {

    private static final ConcurrentMap<UUID, PendingAdmission> PENDING_ADMISSIONS = new ConcurrentHashMap<>();

    private final IGrid grid;
    private final Set<IGridNode> nodes = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<ComputeSourceAllocation> computeSources = new ArrayList<>();
    private final CraftingReservationLedger craftingReservations = new CraftingReservationLedger();
    private final UUID memberId = UUID.randomUUID();
    private UUID overlayLeaseId = UUID.randomUUID();
    private List<EnergyService> overlayIdentity = List.of();
    private List<GridComputeService> overlayMembers;
    private GridComputeService overlayAuthority;
    private long capacityCwut;
    private long fundedCwut;
    private long localInfrastructureReservedCwut;
    private long localChannelDeviceCount;
    private long localChannelOverloadCwut;
    private long infrastructureReservedCwut;
    private long channelDeviceCount;
    private long channelOverloadCwut;
    private long cycleServerTick = Long.MIN_VALUE;
    private long fundingServerTick;
    private int ticksUntilReservationRefresh;
    private boolean reservationsDirty = true;

    public GridComputeService(IGrid grid) {
        this.grid = grid;
        overlayMembers = List.of(this);
        overlayAuthority = this;
    }

    @Override
    public void onServerStartTick() {
        authority().refreshState();
    }

    private GridComputeService authority() {
        refreshOverlayMembership();
        return overlayAuthority;
    }

    private void refreshState() {
        long tick = currentServerTick();
        if (cycleServerTick != tick) {
            cycleServerTick = tick;
            fundingServerTick = tick;
            fundedCwut = 0;
            refreshCapacity();
            refreshReservationsWhenNeeded();
            commitCwut(totalReservedCwut());
        } else if (overlayMembers.stream().anyMatch(member -> member.reservationsDirty)) {
            refreshReservationsWhenNeeded();
            commitCwut(totalReservedCwut());
        }
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        nodes.add(gridNode);
        localChannelOverloadCwut = 0;
        invalidateReservations();
        overlayAuthority.cycleServerTick = Long.MIN_VALUE;
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        nodes.remove(gridNode);
        localChannelOverloadCwut = 0;
        invalidateReservations();
        overlayAuthority.cycleServerTick = Long.MIN_VALUE;
    }

    @Override
    public boolean tryReserveCrafting(IGridNode node, UUID reservationId, long cwut) {
        if (node.getLevel().getServer() == null || !node.getLevel().getServer().isSameThread()) {
            return false;
        }
        GridComputeService authority = authority();
        authority.refreshState();
        if (!authority.belongsToOverlay(node) || authority.fundedCwut < authority.totalReservedCwut()) {
            return false;
        }
        PendingAdmission admission = new PendingAdmission(node, cwut, authority);
        if (PENDING_ADMISSIONS.putIfAbsent(reservationId, admission) != null) {
            return false;
        }
        boolean accepted = false;
        try {
            accepted = authority.craftingReservations.tryReserve(reservationId, cwut, authority.capacityCwut,
                    saturatingAdd(authority.infrastructureReservedCwut, authority.channelOverloadCwut),
                    authority::commitCwut);
            if (accepted) {
                GridComputeService current = authority();
                current.refreshState();
                current.refreshCraftingReservations();
                accepted = current.belongsToOverlay(node) && current.fundedCwut >= current.totalReservedCwut();
            }
            return accepted;
        } finally {
            if (!accepted) {
                finishCraftingAdmission(reservationId);
            }
        }
    }

    @Override
    public void finishCraftingAdmission(UUID reservationId) {
        PendingAdmission pending = PENDING_ADMISSIONS.remove(reservationId);
        if (pending != null) {
            pending.owner().craftingReservations.finishAdmission(reservationId);
            pending.owner().refreshCraftingReservations();
        }
        GridComputeService authority = authority();
        authority.craftingReservations.finishAdmission(reservationId);
        authority.refreshCraftingReservations();
    }

    @Override
    public boolean canDispatchCrafting() {
        GridComputeService authority = authority();
        authority.refreshState();
        return authority.fundedCwut >= authority.totalReservedCwut();
    }

    @Override
    public boolean canAdmitCrafting() {
        return snapshot().availableCraftingCwut() >= CraftingComputeTuning.executionReservationCwut(0, 0);
    }

    @Override
    public ComputeSnapshot snapshot() {
        GridComputeService authority = authority();
        authority.refreshState();
        return new ComputeSnapshot(authority.capacityCwut, authority.fundedCwut,
                authority.infrastructureReservedCwut, authority.craftingReservations.totalCwut(),
                authority.channelOverloadCwut, authority.channelDeviceCount);
    }

    @Override
    public void invalidateReservations() {
        reservationsDirty = true;
    }

    @Override
    public void updateChannelOverloadReservation(long cwut) {
        long value = Math.max(0, cwut);
        if (localChannelOverloadCwut != value) {
            localChannelOverloadCwut = value;
            invalidateReservations();
        }
    }

    @Override
    public void clearChannelOverloadReservation() {
        updateChannelOverloadReservation(0);
    }

    private void refreshCapacity() {
        Map<UUID, ComputeSourceAllocation> sourceAllocations = new HashMap<>();
        for (GridComputeService member : overlayMembers()) {
            for (IGridNode node : member.nodes) {
                IComputeSource source = node.getService(IComputeSource.class);
                if (source == null || !node.meetsChannelRequirements()) {
                    continue;
                }
                long installedCwut = Math.max(0, source.installedCwut());
                sourceAllocations.compute(source.sourceId(), (sourceId, current) ->
                        current == null || installedCwut > current.capacityCwut() ?
                                new ComputeSourceAllocation(sourceId, node, source, installedCwut) : current);
            }
        }
        computeSources.clear();
        computeSources.addAll(sourceAllocations.values());
        computeSources.sort(Comparator.comparing(ComputeSourceAllocation::sourceId));
        capacityCwut = 0;
        for (ComputeSourceAllocation source : computeSources) {
            capacityCwut = saturatingAdd(capacityCwut, source.capacityCwut());
        }
    }


    private void refreshReservationsWhenNeeded() {
        infrastructureReservedCwut = 0;
        channelDeviceCount = 0;
        channelOverloadCwut = 0;
        for (GridComputeService member : overlayMembers()) {
            if (member.reservationsDirty || member.ticksUntilReservationRefresh <= 0) {
                LocalReservation local = member.calculateReservations();
                member.localInfrastructureReservedCwut = local.infrastructureCwut();
                member.localChannelDeviceCount = local.channelDeviceCount();
                member.reservationsDirty = false;
                member.ticksUntilReservationRefresh = ComputeTuning.RESERVATION_REFRESH_INTERVAL;
            }
            member.ticksUntilReservationRefresh--;
            infrastructureReservedCwut = saturatingAdd(infrastructureReservedCwut, member.localInfrastructureReservedCwut);
            channelDeviceCount = saturatingAdd(channelDeviceCount, member.localChannelDeviceCount);
            channelOverloadCwut = saturatingAdd(channelOverloadCwut, member.localChannelOverloadCwut);
        }
        infrastructureReservedCwut = saturatingAdd(infrastructureReservedCwut,
                ComputeTuning.channelDeviceReservation(channelDeviceCount));
        refreshCraftingReservations();
    }

    private void refreshCraftingReservations() {
        Map<UUID, Long> attached = new HashMap<>();
        Set<CraftingCPUCluster> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (GridComputeService member : overlayMembers()) {
            for (IGridNode node : member.nodes) {
                if (!(node.getOwner() instanceof CraftingBlockEntity block)) {
                    continue;
                }
                CraftingCPUCluster cpu = block.getCluster();
                if (cpu == null || cpu.isDestroyed() || !seen.add(cpu)) {
                    continue;
                }
                ICraftingCpuReservation reservation = (ICraftingCpuReservation) cpu.craftingLogic;
                var state = reservation.nebulae$getJobState();
                if (state != null) {
                    attached.merge(state.reservationId(), reservation.nebulae$getReservationCwut(), Math::max);
                }
            }
        }
        for (var pending : PENDING_ADMISSIONS.entrySet()) {
            if (belongsToOverlay(pending.getValue().node())) {
                attached.merge(pending.getKey(), pending.getValue().cwut(), Math::max);
            }
        }
        craftingReservations.replaceAttached(attached);
    }

    private LocalReservation calculateReservations() {
        long reservation = 0;
        long countedChannelDevices = 0;
        long physicalConnectionSides = 0;
        for (IGridNode node : nodes) {
            if (node.getService(IComputeSource.class) == null && node.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                countedChannelDevices = saturatingAdd(countedChannelDevices, 1);
            }
            reservation = saturatingAdd(reservation, calculateNodeReservation(node));
            physicalConnectionSides = saturatingAdd(physicalConnectionSides, node.getInWorldConnections().size());
        }
        long physicalConnections = physicalConnectionSides / 2 + physicalConnectionSides % 2;
        reservation = saturatingAdd(
                reservation,
                saturatingMultiply(
                        ceilDivide(physicalConnections, ComputeTuning.PHYSICAL_LINKS_PER_GROUP),
                        ComputeTuning.PHYSICAL_LINK_GROUP_RESERVATION));
        return new LocalReservation(
                saturatingAdd(reservation, calculateIndexReservation()),
                countedChannelDevices);
    }

    private long calculateNodeReservation(IGridNode node) {
        if (node.getService(IComputeSource.class) != null) {
            return 0;
        }
        long reservation = 0;
        if (node.getService(IStorageProvider.class) != null) {
            reservation = saturatingAdd(reservation, ComputeTuning.STORAGE_PROVIDER_RESERVATION);
        }
        if (node.getService(ICraftingProvider.class) != null) {
            reservation = saturatingAdd(reservation, ComputeTuning.CRAFTING_PROVIDER_RESERVATION);
        }
        if (node.getOwner() instanceof InterfaceLogicHost interfaceHost) {
            int configuredSlots = 0;
            var config = interfaceHost.getConfig();
            for (int slot = 0; slot < config.size(); slot++) {
                if (config.getKey(slot) != null) {
                    configuredSlots++;
                }
            }
            reservation = saturatingAdd(
                    reservation,
                    saturatingMultiply(
                            ceilDivide(configuredSlots, ComputeTuning.INTERFACE_STOCKING_SLOTS_PER_GROUP),
                            ComputeTuning.INTERFACE_STOCKING_GROUP_RESERVATION));
        }
        if (node.getOwner() instanceof WirelessAccessPointBlockEntity wirelessAccessPoint) {
            int boosters = wirelessAccessPoint.getInternalInventory().getStackInSlot(0).getCount();
            reservation = saturatingAdd(
                    reservation,
                    saturatingMultiply(boosters, ComputeTuning.WIRELESS_BOOSTER_RESERVATION));
        }
        return reservation;
    }

    private long calculateIndexReservation() {
        Set<Object> indexedTypes = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var entry : grid.getStorageService().getCachedInventory()) {
            if (entry.getLongValue() <= 0) {
                continue;
            }
            if (entry.getKey() instanceof AEItemKey itemKey) {
                indexedTypes.add(itemKey.getItem());
            } else if (entry.getKey() instanceof AEFluidKey fluidKey) {
                indexedTypes.add(fluidKey.getFluid());
            }
        }
        return saturatingMultiply(
                ceilDivide(indexedTypes.size(), ComputeTuning.INDEX_KEYS_PER_GROUP),
                ComputeTuning.INDEX_KEY_GROUP_RESERVATION);
    }


    private long totalReservedCwut() {
        return saturatingAdd(saturatingAdd(infrastructureReservedCwut, channelOverloadCwut),
                craftingReservations.totalCwut());
    }

    private long commitCwut(long targetTotalCwut) {
        long boundedTargetCwut = Math.max(fundedCwut, Math.min(capacityCwut, Math.max(0, targetTotalCwut)));
        long remainingCwut = boundedTargetCwut - fundedCwut;
        for (ComputeSourceAllocation allocation : computeSources) {
            long sourceTargetCwut = saturatingAdd(allocation.committedCwut(), remainingCwut);
            sourceTargetCwut = Math.min(allocation.capacityCwut(), sourceTargetCwut);
            if (containsNode(allocation.node()) && allocation.node().meetsChannelRequirements()) {
                long committedCwut = allocation.source().commitCwut(overlayLeaseId, fundingServerTick, sourceTargetCwut);
                committedCwut = Math.clamp(committedCwut, allocation.committedCwut(), sourceTargetCwut);
                long newlyCommittedCwut = committedCwut - allocation.committedCwut();
                allocation.setCommittedCwut(committedCwut);
                fundedCwut = saturatingAdd(fundedCwut, newlyCommittedCwut);
                remainingCwut -= newlyCommittedCwut;
            }
        }
        return fundedCwut;
    }

    private long currentServerTick() {
        for (GridComputeService member : overlayMembers()) {
            for (IGridNode node : member.nodes) {
                return node.getLevel().getServer().getTickCount();
            }
        }
        return 0;
    }

    private boolean belongsToOverlay(IGridNode node) {
        try {
            IGrid nodeGrid = node.getGrid();
            for (GridComputeService member : overlayMembers()) {
                if (member.grid == nodeGrid) {
                    return true;
                }
            }
            return false;
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    private boolean containsNode(IGridNode node) {
        for (GridComputeService member : overlayMembers()) {
            if (member.nodes.contains(node)) {
                return true;
            }
        }
        return false;
    }

    private List<GridComputeService> overlayMembers() {
        return overlayMembers;
    }

    private void refreshOverlayMembership() {
        EnergyService energyService = (EnergyService) grid.getEnergyService();
        List<EnergyService> connectedEnergyServices = ((EnergyServiceAccessor) energyService)
                .nebulae$getConnectedServices();
        if (overlayIdentity == connectedEnergyServices) {
            return;
        }

        List<GridComputeService> members = new ArrayList<>(connectedEnergyServices.size());
        for (EnergyService connectedEnergyService : connectedEnergyServices) {
            IGrid connectedGrid = ((EnergyServiceAccessor) connectedEnergyService).nebulae$getGrid();
            members.add((GridComputeService) connectedGrid.getService(IComputeService.class));
        }
        members.sort(Comparator.comparing(member -> member.memberId));
        List<GridComputeService> sharedMembers = List.copyOf(members);
        GridComputeService authority = sharedMembers.getFirst();
        Set<GridComputeService> previousAuthorities = Collections.newSetFromMap(new IdentityHashMap<>());
        for (GridComputeService member : sharedMembers) {
            previousAuthorities.add(member.overlayAuthority);
        }
        for (GridComputeService member : sharedMembers) {
            member.overlayIdentity = connectedEnergyServices;
            member.overlayMembers = sharedMembers;
            member.overlayAuthority = authority;
        }
        for (GridComputeService previousAuthority : previousAuthorities) {
            if (previousAuthority != authority && sharedMembers.contains(previousAuthority)) {
                previousAuthority.retireOverlayState();
            }
        }
        authority.resetOverlayState();
    }


    private void retireOverlayState() {
        computeSources.clear();
        craftingReservations.replaceAttached(Map.of());
    }

    private void resetOverlayState() {
        overlayLeaseId = UUID.randomUUID();
        capacityCwut = 0;
        fundedCwut = 0;
        infrastructureReservedCwut = 0;
        channelDeviceCount = 0;
        channelOverloadCwut = 0;
        fundingServerTick = 0;
        cycleServerTick = Long.MIN_VALUE;
        computeSources.clear();
        craftingReservations.replaceAttached(Map.of());
    }

    private static long saturatingAdd(long left, long right) {
        if (right > 0 && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    private static long saturatingMultiply(long left, long right) {
        if (left == 0 || right == 0) {
            return 0;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    private static long ceilDivide(long value, long divisor) {
        return value / divisor + (value % divisor == 0 ? 0 : 1);
    }

    private record LocalReservation(long infrastructureCwut, long channelDeviceCount) {}

    private record PendingAdmission(IGridNode node, long cwut, GridComputeService owner) {}

    private static final class ComputeSourceAllocation {

        private final UUID sourceId;
        private final IGridNode node;
        private final IComputeSource source;
        private final long capacityCwut;
        private long committedCwut;

        private ComputeSourceAllocation(
                UUID sourceId,
                IGridNode node,
                IComputeSource source,
                long capacityCwut) {
            this.sourceId = sourceId;
            this.node = node;
            this.source = source;
            this.capacityCwut = capacityCwut;
        }

        private UUID sourceId() {
            return sourceId;
        }

        private IGridNode node() {
            return node;
        }

        private IComputeSource source() {
            return source;
        }

        private long capacityCwut() {
            return capacityCwut;
        }

        private long committedCwut() {
            return committedCwut;
        }

        private void setCommittedCwut(long committedCwut) {
            this.committedCwut = committedCwut;
        }
    }
}
