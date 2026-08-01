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

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GridComputeService implements IComputeService, IGridServiceProvider {

    private final IGrid grid;
    private final Set<IGridNode> nodes = Collections.newSetFromMap(new IdentityHashMap<>());
    private long capacityCwut;
    private long reservedCwut;
    private long workBudgetCwut;
    private long workUsedCwut;
    private long debtCwu;
    private long throttledOperations;
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
        reservationsDirty = true;
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        nodes.remove(gridNode);
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
                workBudgetCwut,
                workUsedCwut,
                debtCwu,
                sourceCount,
                nodes.size(),
                throttledOperations);
    }

    @Override
    public void invalidateReservations() {
        reservationsDirty = true;
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
        reservedCwut = calculateReservations();
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
                ceilDivide(physicalConnections, ComputeTuning.PHYSICAL_LINKS_PER_CWU));
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
                    ceilDivide(configuredSlots, ComputeTuning.INTERFACE_STOCKING_SLOTS_PER_CWU));
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
        return ceilDivide(indexedTypes.size(), ComputeTuning.INDEX_KEYS_PER_CWU);
    }

    private void beginWorkCycle() {
        long discretionaryCwut = Math.max(0, capacityCwut - reservedCwut);
        long paidDebt = Math.min(discretionaryCwut, debtCwu);
        debtCwu -= paidDebt;
        workBudgetCwut = discretionaryCwut - paidDebt;
        workUsedCwut = 0;
        throttledOperations = 0;
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
