package com.ghostipedia.nebulaeae2.compute;

import com.ghostipedia.nebulaeae2.compute.api.ComputeSnapshot;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import com.ghostipedia.nebulaeae2.compute.api.IComputeSource;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageProvider;
import appeng.helpers.InterfaceLogicHost;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
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
    private long capacityCwut;
    private long infrastructureReservedCwut;
    private long reservedCwut;
    private long channelOverloadCwut;
    private long workBudgetCwut;
    private long workUsedCwut;
    private long recoveryBudgetCwut;
    private long recoveryUsedCwut;
    private long debtCwu;
    private long throttledOperations;
    private long recoveryOperations;
    private long serviceTick;
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
    private int telemetryCursor;
    private int telemetrySampleCount;
    private boolean workCycleStarted;
    private int sourceCount;
    private int ticksUntilReservationRefresh;
    private boolean reservationsDirty = true;

    public GridComputeService(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public void onServerStartTick() {
        refreshCapacity();
        refreshReservationsWhenNeeded();
        beginWorkCycle();
    }

    @Override
    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        nodes.add(gridNode);
        clearChannelOverloadReservation();
        reservationsDirty = true;
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        nodes.remove(gridNode);
        recoveryEligibleTicks.remove(gridNode);
        removeRecoveryWaiter(gridNode);
        clearChannelOverloadReservation();
        reservationsDirty = true;
    }

    @Override
    public long acquireUpTo(IGridNode node, long maximumCwut) {
        if (maximumCwut <= 0) {
            return 0;
        }
        if (!belongsToGrid(node)) {
            throttledOperations = saturatingAdd(throttledOperations, 1);
            return 0;
        }
        long availableCwut = Math.max(0, workBudgetCwut - workUsedCwut);
        long acquiredCwut = Math.min(maximumCwut, availableCwut);
        workUsedCwut = saturatingAdd(workUsedCwut, acquiredCwut);
        if (acquiredCwut < maximumCwut) {
            throttledOperations = saturatingAdd(throttledOperations, 1);
        }
        return acquiredCwut;
    }

    @Override
    public long acquireWholeUnitsUpTo(IGridNode node, long maximumUnits, long cwutPerUnit) {
        if (maximumUnits <= 0 || cwutPerUnit <= 0) {
            return 0;
        }
        if (!belongsToGrid(node)) {
            throttledOperations = saturatingAdd(throttledOperations, 1);
            return 0;
        }
        long availableCwut = Math.max(0, workBudgetCwut - workUsedCwut);
        long acquiredUnits = Math.min(maximumUnits, availableCwut / cwutPerUnit);
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
        if (cwu <= 0 || !belongsToGrid(node)) {
            return;
        }
        long immediatelyFunded = Math.min(cwu, Math.max(0, workBudgetCwut - workUsedCwut));
        workUsedCwut = saturatingAdd(workUsedCwut, immediatelyFunded);
        long unfunded = cwu - immediatelyFunded;
        debtCwu = Math.min(debtLimit(), saturatingAdd(debtCwu, unfunded));
    }

    @Override
    public ComputeSnapshot snapshot() {
        return new ComputeSnapshot(
                capacityCwut,
                reservedCwut,
                channelOverloadCwut,
                workBudgetCwut,
                workUsedCwut,
                recentWorkAverageCwut,
                recentWorkPeakCwut,
                recoveryBudgetCwut,
                recoveryUsedCwut,
                debtCwu,
                sourceCount,
                nodes.size(),
                recentThrottledOperations,
                recentRecoveryOperations);
    }

    @Override
    public void invalidateReservations() {
        reservationsDirty = true;
    }

    @Override
    public void updateChannelOverloadReservation(long cwut) {
        channelOverloadCwut = Math.max(0, cwut);
        updateTotalReservation();
    }

    @Override
    public void clearChannelOverloadReservation() {
        channelOverloadCwut = 0;
        updateTotalReservation();
    }

    private void refreshCapacity() {
        Map<UUID, Long> sourceCapacities = new HashMap<>();
        for (IGridNode node : nodes) {
            IComputeSource source = node.getService(IComputeSource.class);
            if (source == null || !node.meetsChannelRequirements()) {
                continue;
            }
            long availableCwut = Math.max(0, source.availableCwut());
            sourceCapacities.merge(source.sourceId(), availableCwut, Math::max);
        }
        capacityCwut = 0;
        for (long sourceCapacity : sourceCapacities.values()) {
            capacityCwut = saturatingAdd(capacityCwut, sourceCapacity);
        }
        sourceCount = sourceCapacities.size();
    }

    private void refreshReservationsWhenNeeded() {
        if (!reservationsDirty && ticksUntilReservationRefresh > 0) {
            ticksUntilReservationRefresh--;
            return;
        }
        infrastructureReservedCwut = calculateReservations();
        updateTotalReservation();
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

    private void beginWorkCycle() {
        if (workCycleStarted) {
            recordCompletedWorkCycle();
        } else {
            workCycleStarted = true;
        }
        serviceTick = saturatingAdd(serviceTick, 1);
        long discretionaryCwut = Math.max(0, capacityCwut - reservedCwut);
        long paidDebt = Math.min(discretionaryCwut, debtCwu);
        debtCwu -= paidDebt;
        workBudgetCwut = discretionaryCwut - paidDebt;
        workUsedCwut = 0;
        recoveryBudgetCwut = calculateRecoveryBudget();
        recoveryUsedCwut = 0;
        if (recoveryBudgetCwut == 0) {
            clearRecoveryWaiters();
        }
        throttledOperations = 0;
        recoveryOperations = 0;
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

        long recoveryAvailableCwut = recoveryBudgetCwut - recoveryUsedCwut;
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
            if (nodes.contains(waiter) && !expired) {
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
        if (capacityCwut <= 0 || workBudgetCwut >= ComputeTuning.BASE_CWU_COST) {
            return 0;
        }
        long scaledCwut = capacityCwut / ComputeTuning.RECOVERY_CAPACITY_DIVISOR;
        long roundedCwut = scaledCwut / ComputeTuning.BASE_CWU_COST * ComputeTuning.BASE_CWU_COST;
        return Math.max(ComputeTuning.BASE_CWU_COST, roundedCwut);
    }

    private void updateTotalReservation() {
        reservedCwut = saturatingAdd(infrastructureReservedCwut, channelOverloadCwut);
    }

    private boolean belongsToGrid(IGridNode node) {
        try {
            return node.getGrid() == grid;
        } catch (IllegalStateException ignored) {
            return false;
        }
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
}
