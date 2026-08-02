package com.ghostipedia.nebulaeae2.compute;

import com.ghostipedia.nebulaeae2.compute.api.ComputeSnapshot;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import com.ghostipedia.nebulaeae2.compute.api.IComputeSource;
import com.ghostipedia.nebulaeae2.mixin.ae2.compute.EnergyServiceAccessor;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageProvider;
import appeng.helpers.InterfaceLogicHost;
import appeng.me.service.EnergyService;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GridComputeService implements IComputeService, IGridServiceProvider {

    private final IGrid grid;
    private final Set<IGridNode> nodes = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<IGridNode, Long> recoveryEligibleTicks = new IdentityHashMap<>();
    private final Deque<IGridNode> recoveryWaiters = new ArrayDeque<>();
    private final Set<IGridNode> recoveryWaiterSet = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<IGridNode, Long> recoveryLastRequestTicks = new IdentityHashMap<>();
    private final List<ComputeSourceAllocation> computeSources = new ArrayList<>();
    private final UUID memberId = UUID.randomUUID();
    private UUID overlayLeaseId = UUID.randomUUID();
    private List<EnergyService> overlayIdentity = List.of();
    private List<GridComputeService> overlayMembers;
    private GridComputeService overlayAuthority;
    private long capacityCwut;
    private long fundedCwut;
    private long localInfrastructureReservedCwut;
    private long localChannelOverloadCwut;
    private long infrastructureReservedCwut;
    private long reservedCwut;
    private long passiveShortfallCwut;
    private long powerFundingShortfallCwut;
    private long channelOverloadCwut;
    private long workBudgetCwut;
    private long workUsedCwut;
    private long recoveryBudgetCwut;
    private long recoveryUsedCwut;
    private long localDebtCwu;
    private long debtCwu;
    private long paidDebtThisTickCwut;
    private long throttledOperations;
    private long recoveryOperations;
    private long serviceTick;
    private long cycleServerTick = Long.MIN_VALUE;
    private final long[] telemetryWorkSamples = new long[ComputeTuning.TELEMETRY_WINDOW_TICKS];
    private final long[] telemetryThrottleSamples = new long[ComputeTuning.TELEMETRY_WINDOW_TICKS];
    private final long[] telemetryRecoverySamples = new long[ComputeTuning.TELEMETRY_WINDOW_TICKS];
    private long telemetryWorkCwu;
    private long telemetryThrottledOperations;
    private long telemetryRecoveryOperations;
    private double recentWorkAverageCwut;
    private long recentWorkPeakCwut;
    private long recentThrottledOperations;
    private long recentRecoveryOperations;
    private long fundingServerTick;
    private int telemetryCursor;
    private int telemetrySampleCount;
    private boolean workCycleStarted;
    private boolean debtFundingShortfall;
    private int sourceCount;
    private int ticksUntilReservationRefresh;
    private boolean reservationsDirty = true;

    public GridComputeService(IGrid grid) {
        this.grid = grid;
        overlayMembers = List.of(this);
        overlayAuthority = this;
    }

    @Override
    public void onServerStartTick() {
        refreshOverlayMembership();
        GridComputeService authority = overlayAuthority;
        authority.beginServerTick(authority.currentServerTick());
    }

    private void beginServerTick(long serverTick) {
        if (cycleServerTick == serverTick) {
            return;
        }
        cycleServerTick = serverTick;
        refreshCapacity();
        refreshReservationsWhenNeeded();
        beginWorkCycle(serverTick);
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        nodes.add(gridNode);
        localChannelOverloadCwut = 0;
        reservationsDirty = true;
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        nodes.remove(gridNode);
        overlayAuthority.recoveryEligibleTicks.remove(gridNode);
        overlayAuthority.removeRecoveryWaiter(gridNode);
        localChannelOverloadCwut = 0;
        reservationsDirty = true;
    }

    @Override
    public long acquireUpTo(IGridNode node, long maximumCwut) {
        GridComputeService authority = overlayAuthority;
        if (authority != this) {
            return authority.acquireUpTo(node, maximumCwut);
        }
        beginServerTick(currentServerTick());
        if (maximumCwut <= 0) {
            return 0;
        }
        if (!belongsToOverlay(node)) {
            throttledOperations = saturatingAdd(throttledOperations, 1);
            return 0;
        }
        long acquiredCwut = acquireFundedCwutUpTo(maximumCwut);
        workUsedCwut = saturatingAdd(workUsedCwut, acquiredCwut);
        if (acquiredCwut < maximumCwut) {
            throttledOperations = saturatingAdd(throttledOperations, 1);
        }
        return acquiredCwut;
    }

    @Override
    public long acquireWholeUnitsUpTo(IGridNode node, long maximumUnits, long cwutPerUnit) {
        GridComputeService authority = overlayAuthority;
        if (authority != this) {
            return authority.acquireWholeUnitsUpTo(node, maximumUnits, cwutPerUnit);
        }
        beginServerTick(currentServerTick());
        if (maximumUnits <= 0 || cwutPerUnit <= 0) {
            return 0;
        }
        if (!belongsToOverlay(node)) {
            throttledOperations = saturatingAdd(throttledOperations, 1);
            return 0;
        }
        long availableBudgetCwut = Math.max(0, workBudgetCwut - workUsedCwut - recoveryUsedCwut);
        long requestedUnits = Math.min(maximumUnits, availableBudgetCwut / cwutPerUnit);
        long requestedCwut = saturatingMultiply(requestedUnits, cwutPerUnit);
        long fundedWorkCwut = fundDynamicCwutUpTo(requestedCwut);
        long acquiredUnits = fundedWorkCwut / cwutPerUnit;
        workUsedCwut = saturatingAdd(workUsedCwut, saturatingMultiply(acquiredUnits, cwutPerUnit));
        if (acquiredUnits == maximumUnits) {
            removeRecoveryWaiter(node);
        }
        long recoveryUnits = acquireRecoveryUnit(node, maximumUnits - acquiredUnits, cwutPerUnit);
        long totalAcquiredUnits = saturatingAdd(acquiredUnits, recoveryUnits);
        if (totalAcquiredUnits < maximumUnits) {
            throttledOperations = saturatingAdd(throttledOperations, 1);
        }
        return totalAcquiredUnits;
    }

    @Override
    public void chargeSynchronousDebt(IGridNode node, long cwu) {
        GridComputeService authority = overlayAuthority;
        if (authority != this) {
            authority.chargeSynchronousDebt(node, cwu);
            return;
        }
        beginServerTick(currentServerTick());
        if (cwu <= 0 || !belongsToOverlay(node)) {
            return;
        }
        long immediatelyFunded = acquireFundedCwutUpTo(cwu);
        workUsedCwut = saturatingAdd(workUsedCwut, immediatelyFunded);
        long unfunded = cwu - immediatelyFunded;
        long admittedDebtCwu = Math.min(unfunded, Math.max(0, debtLimit() - aggregateDebtCwu()));
        GridComputeService debtor = memberFor(node);
        if (debtor != null) {
            debtor.localDebtCwu = saturatingAdd(debtor.localDebtCwu, admittedDebtCwu);
        }
        debtCwu = aggregateDebtCwu();
    }

    @Override
    public ComputeSnapshot snapshot() {
        GridComputeService authority = overlayAuthority;
        if (authority != this) {
            return authority.snapshot();
        }
        beginServerTick(currentServerTick());
        return new ComputeSnapshot(
                capacityCwut,
                fundedCwut,
                reservedCwut,
                passiveShortfallCwut,
                channelOverloadCwut,
                workBudgetCwut,
                workUsedCwut,
                recentWorkAverageCwut,
                recentWorkPeakCwut,
                recoveryBudgetCwut,
                recoveryUsedCwut,
                debtCwu,
                sourceCount,
                trackedNodeCount(),
                recentThrottledOperations,
                recentRecoveryOperations);
    }

    @Override
    public void invalidateReservations() {
        reservationsDirty = true;
    }

    @Override
    public void updateChannelOverloadReservation(long cwut) {
        localChannelOverloadCwut = Math.max(0, cwut);
    }

    @Override
    public void clearChannelOverloadReservation() {
        localChannelOverloadCwut = 0;
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
        sourceCount = computeSources.size();
    }

    private void refreshReservationsWhenNeeded() {
        for (GridComputeService member : overlayMembers()) {
            member.refreshLocalReservationsWhenNeeded();
        }
        updateTotalReservation();
    }

    private void refreshLocalReservationsWhenNeeded() {
        if (!reservationsDirty && ticksUntilReservationRefresh > 0) {
            ticksUntilReservationRefresh--;
            return;
        }
        localInfrastructureReservedCwut = calculateReservations();
        reservationsDirty = false;
        ticksUntilReservationRefresh = ComputeTuning.RESERVATION_REFRESH_INTERVAL - 1;
    }

    private long calculateReservations() {
        long reservation = 0;
        long physicalConnectionSides = 0;
        for (IGridNode node : nodes) {
            reservation = saturatingAdd(reservation, calculateNodeReservation(node));
            physicalConnectionSides = saturatingAdd(physicalConnectionSides, node.getInWorldConnections().size());
        }
        long physicalConnections = physicalConnectionSides / 2 + physicalConnectionSides % 2;
        reservation = saturatingAdd(
                reservation,
                saturatingMultiply(
                        ceilDivide(physicalConnections, ComputeTuning.PHYSICAL_LINKS_PER_GROUP),
                        ComputeTuning.PHYSICAL_LINK_GROUP_RESERVATION));
        return saturatingAdd(reservation, calculateIndexReservation());
    }

    private long calculateNodeReservation(IGridNode node) {
        if (node.getService(IComputeSource.class) != null) {
            return 0;
        }
        long reservation = node.hasFlag(GridFlags.REQUIRE_CHANNEL) ?
                ComputeTuning.CHANNEL_DEVICE_RESERVATION : 0;
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

    private void beginWorkCycle(long serverTick) {
        if (workCycleStarted) {
            recordCompletedWorkCycle();
        } else {
            workCycleStarted = true;
        }
        serviceTick = saturatingAdd(serviceTick, 1);
        fundingServerTick = serverTick;
        fundedCwut = 0;
        fundedCwut = commitCwut(Math.min(capacityCwut, reservedCwut));
        updateFundingShortfalls();
        paidDebtThisTickCwut = 0;
        debtFundingShortfall = false;
        debtCwu = aggregateDebtCwu();
        long discretionaryCwut = Math.max(0, capacityCwut - reservedCwut);
        if (powerFundingShortfallCwut == 0) {
            long requestedDebtCwut = Math.min(discretionaryCwut, debtCwu);
            long debtTargetCwut = saturatingAdd(reservedCwut, requestedDebtCwut);
            long debtFundedCwut = Math.max(0, commitCwut(debtTargetCwut) - reservedCwut);
            paidDebtThisTickCwut = Math.min(requestedDebtCwut, debtFundedCwut);
            repayDebt(paidDebtThisTickCwut);
            debtCwu = aggregateDebtCwu();
            debtFundingShortfall = paidDebtThisTickCwut < requestedDebtCwut;
        }
        workBudgetCwut = powerFundingShortfallCwut == 0 && !debtFundingShortfall ?
                discretionaryCwut - paidDebtThisTickCwut : 0;
        workUsedCwut = 0;
        recoveryBudgetCwut = calculateRecoveryBudget();
        recoveryUsedCwut = 0;
        if (recoveryBudgetCwut == 0) {
            clearRecoveryWaiters();
        }
        throttledOperations = 0;
        recoveryOperations = 0;
    }

    private long acquireFundedCwutUpTo(long maximumCwut) {
        long availableBudgetCwut = Math.max(0, workBudgetCwut - workUsedCwut - recoveryUsedCwut);
        return fundDynamicCwutUpTo(Math.min(maximumCwut, availableBudgetCwut));
    }

    private long fundDynamicCwutUpTo(long requestedCwut) {
        if (requestedCwut <= 0) {
            return 0;
        }
        long baseDemandCwut = saturatingAdd(reservedCwut, paidDebtThisTickCwut);
        long allocatedWorkCwut = saturatingAdd(workUsedCwut, recoveryUsedCwut);
        long targetCwut = saturatingAdd(baseDemandCwut, allocatedWorkCwut);
        targetCwut = saturatingAdd(targetCwut, requestedCwut);
        commitCwut(targetCwut);
        long fundedWorkCwut = Math.max(0, fundedCwut - Math.min(capacityCwut, baseDemandCwut));
        long availableFundedCwut = Math.max(0, fundedWorkCwut - allocatedWorkCwut);
        return Math.min(requestedCwut, availableFundedCwut);
    }

    private long commitCwut(long targetTotalCwut) {
        long boundedTargetCwut = Math.max(fundedCwut, Math.min(capacityCwut, Math.max(0, targetTotalCwut)));
        long remainingCwut = boundedTargetCwut - fundedCwut;
        for (ComputeSourceAllocation allocation : computeSources) {
            long sourceTargetCwut = saturatingAdd(allocation.committedCwut(), remainingCwut);
            sourceTargetCwut = Math.min(allocation.capacityCwut(), sourceTargetCwut);
            if (containsNode(allocation.node()) && allocation.node().meetsChannelRequirements()) {
                long committedCwut = allocation.source().commitCwut(
                        overlayLeaseId,
                        fundingServerTick,
                        sourceTargetCwut);
                committedCwut = Math.clamp(committedCwut, allocation.committedCwut(), sourceTargetCwut);
                long newlyCommittedCwut = committedCwut - allocation.committedCwut();
                allocation.setCommittedCwut(committedCwut);
                fundedCwut = saturatingAdd(fundedCwut, newlyCommittedCwut);
                remainingCwut -= newlyCommittedCwut;
            }
        }
        updateFundingShortfalls();
        return fundedCwut;
    }

    private void updateFundingShortfalls() {
        long powerFundingTargetCwut = Math.min(capacityCwut, reservedCwut);
        powerFundingShortfallCwut = Math.max(0, powerFundingTargetCwut - fundedCwut);
        passiveShortfallCwut = Math.max(0, reservedCwut - fundedCwut);
    }

    private long currentServerTick() {
        if (computeSources.isEmpty()) {
            for (GridComputeService member : overlayMembers()) {
                for (IGridNode node : member.nodes) {
                    return node.getLevel().getServer().getTickCount();
                }
            }
            return serviceTick;
        }
        return computeSources.getFirst().node().getLevel().getServer().getTickCount();
    }

    private void recordCompletedWorkCycle() {
        long dynamicWorkCwut = saturatingAdd(workUsedCwut, recoveryUsedCwut);
        if (telemetrySampleCount == ComputeTuning.TELEMETRY_WINDOW_TICKS) {
            telemetryWorkCwu -= telemetryWorkSamples[telemetryCursor];
            telemetryThrottledOperations -= telemetryThrottleSamples[telemetryCursor];
            telemetryRecoveryOperations -= telemetryRecoverySamples[telemetryCursor];
        } else {
            telemetrySampleCount++;
        }

        telemetryWorkSamples[telemetryCursor] = dynamicWorkCwut;
        telemetryThrottleSamples[telemetryCursor] = throttledOperations;
        telemetryRecoverySamples[telemetryCursor] = recoveryOperations;
        telemetryWorkCwu = saturatingAdd(telemetryWorkCwu, dynamicWorkCwut);
        telemetryThrottledOperations = saturatingAdd(telemetryThrottledOperations, throttledOperations);
        telemetryRecoveryOperations = saturatingAdd(telemetryRecoveryOperations, recoveryOperations);
        telemetryCursor++;
        if (telemetryCursor == ComputeTuning.TELEMETRY_WINDOW_TICKS) {
            telemetryCursor = 0;
        }

        recentWorkAverageCwut = (double) telemetryWorkCwu / telemetrySampleCount;
        recentWorkPeakCwut = 0;
        for (long sample : telemetryWorkSamples) {
            recentWorkPeakCwut = Math.max(recentWorkPeakCwut, sample);
        }
        recentThrottledOperations = telemetryThrottledOperations;
        recentRecoveryOperations = telemetryRecoveryOperations;
    }

    private long acquireRecoveryUnit(IGridNode node, long requestedUnits, long cwutPerUnit) {
        if (requestedUnits <= 0 || recoveryBudgetCwut <= 0) {
            return 0;
        }
        long eligibleTick = recoveryEligibleTicks.getOrDefault(node, 0L);
        if (eligibleTick > serviceTick || cwutPerUnit > recoveryBudgetCwut) {
            return 0;
        }

        enqueueRecoveryWaiter(node);
        pruneStaleRecoveryWaiters();
        if (recoveryWaiters.peekFirst() != node) {
            return 0;
        }
        if (recoveryBudgetCwut <= recoveryUsedCwut) {
            return 0;
        }

        long recoveryAvailableCwut = Math.min(
                recoveryBudgetCwut - recoveryUsedCwut,
                Math.max(0, fundedCwut - recoveryUsedCwut));
        if (cwutPerUnit > recoveryAvailableCwut) {
            return 0;
        }
        recoveryWaiters.removeFirst();
        recoveryWaiterSet.remove(node);
        recoveryLastRequestTicks.remove(node);
        recoveryUsedCwut = saturatingAdd(recoveryUsedCwut, cwutPerUnit);
        recoveryOperations = saturatingAdd(recoveryOperations, 1);
        recoveryEligibleTicks.put(node, saturatingAdd(serviceTick, ComputeTuning.RECOVERY_NODE_COOLDOWN_TICKS));
        return 1;
    }

    private void enqueueRecoveryWaiter(IGridNode node) {
        recoveryLastRequestTicks.put(node, serviceTick);
        if (recoveryWaiterSet.add(node)) {
            recoveryWaiters.addLast(node);
        }
    }

    private void pruneStaleRecoveryWaiters() {
        while (!recoveryWaiters.isEmpty()) {
            IGridNode waiter = recoveryWaiters.peekFirst();
            long lastRequestTick = recoveryLastRequestTicks.getOrDefault(waiter, Long.MIN_VALUE);
            boolean expired = lastRequestTick == Long.MIN_VALUE
                    || serviceTick - lastRequestTick > ComputeTuning.RECOVERY_WAITER_EXPIRY_TICKS;
            if (containsNode(waiter) && !expired) {
                return;
            }
            recoveryWaiters.removeFirst();
            recoveryWaiterSet.remove(waiter);
            recoveryLastRequestTicks.remove(waiter);
        }
    }

    private void removeRecoveryWaiter(IGridNode node) {
        if (recoveryWaiterSet.remove(node)) {
            recoveryWaiters.removeIf(waiter -> waiter == node);
        }
        recoveryLastRequestTicks.remove(node);
    }

    private void clearRecoveryWaiters() {
        recoveryWaiters.clear();
        recoveryWaiterSet.clear();
        recoveryLastRequestTicks.clear();
        recoveryEligibleTicks.clear();
    }

    private long calculateRecoveryBudget() {
        if (debtFundingShortfall || fundedCwut < ComputeTuning.BASE_CWU_COST) {
            return 0;
        }
        long scaledCwut = fundedCwut / ComputeTuning.RECOVERY_CAPACITY_DIVISOR;
        long roundedCwut = scaledCwut / ComputeTuning.BASE_CWU_COST * ComputeTuning.BASE_CWU_COST;
        long fundedWholeUnitsCwut = fundedCwut / ComputeTuning.BASE_CWU_COST * ComputeTuning.BASE_CWU_COST;
        return Math.min(fundedWholeUnitsCwut, Math.max(ComputeTuning.BASE_CWU_COST, roundedCwut));
    }

    private void updateTotalReservation() {
        infrastructureReservedCwut = 0;
        channelOverloadCwut = 0;
        for (GridComputeService member : overlayMembers()) {
            infrastructureReservedCwut = saturatingAdd(
                    infrastructureReservedCwut,
                    member.localInfrastructureReservedCwut);
            channelOverloadCwut = saturatingAdd(channelOverloadCwut, member.localChannelOverloadCwut);
        }
        reservedCwut = saturatingAdd(infrastructureReservedCwut, channelOverloadCwut);
        updateFundingShortfalls();
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

    private GridComputeService memberFor(IGridNode node) {
        try {
            IGrid nodeGrid = node.getGrid();
            for (GridComputeService member : overlayMembers()) {
                if (member.grid == nodeGrid) {
                    return member;
                }
            }
        } catch (IllegalStateException ignored) {
            return null;
        }
        return null;
    }

    private long aggregateDebtCwu() {
        long aggregate = 0;
        for (GridComputeService member : overlayMembers()) {
            aggregate = saturatingAdd(aggregate, member.localDebtCwu);
        }
        return aggregate;
    }

    private void repayDebt(long paymentCwu) {
        List<GridComputeService> members = overlayMembers();
        if (paymentCwu <= 0 || members.isEmpty()) {
            return;
        }
        int start = Math.floorMod(serviceTick, members.size());
        long remainingCwu = paymentCwu;
        for (int offset = 0; offset < members.size() && remainingCwu > 0; offset++) {
            GridComputeService member = members.get((start + offset) % members.size());
            long repaidCwu = Math.min(member.localDebtCwu, remainingCwu);
            member.localDebtCwu -= repaidCwu;
            remainingCwu -= repaidCwu;
        }
    }

    private int trackedNodeCount() {
        long count = 0;
        for (GridComputeService member : overlayMembers()) {
            count = Math.min(Integer.MAX_VALUE, count + member.nodes.size());
        }
        return (int) count;
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
        clearRecoveryWaiters();
    }

    private void resetOverlayState() {
        overlayLeaseId = UUID.randomUUID();
        capacityCwut = 0;
        fundedCwut = 0;
        infrastructureReservedCwut = 0;
        reservedCwut = 0;
        passiveShortfallCwut = 0;
        powerFundingShortfallCwut = 0;
        channelOverloadCwut = 0;
        workBudgetCwut = 0;
        workUsedCwut = 0;
        recoveryBudgetCwut = 0;
        recoveryUsedCwut = 0;
        debtCwu = aggregateDebtCwu();
        paidDebtThisTickCwut = 0;
        throttledOperations = 0;
        recoveryOperations = 0;
        recentWorkAverageCwut = 0;
        recentWorkPeakCwut = 0;
        recentThrottledOperations = 0;
        recentRecoveryOperations = 0;
        telemetryWorkCwu = 0;
        telemetryThrottledOperations = 0;
        telemetryRecoveryOperations = 0;
        telemetryCursor = 0;
        telemetrySampleCount = 0;
        fundingServerTick = 0;
        sourceCount = 0;
        cycleServerTick = Long.MIN_VALUE;
        workCycleStarted = false;
        debtFundingShortfall = false;
        computeSources.clear();
        clearRecoveryWaiters();
        Arrays.fill(telemetryWorkSamples, 0);
        Arrays.fill(telemetryThrottleSamples, 0);
        Arrays.fill(telemetryRecoverySamples, 0);
    }

    private long debtLimit() {
        long scaledLimit = saturatingMultiply(Math.max(1, capacityCwut), ComputeTuning.MAX_DEBT_TICKS);
        return Math.max(ComputeTuning.MINIMUM_DEBT_LIMIT, scaledLimit);
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
