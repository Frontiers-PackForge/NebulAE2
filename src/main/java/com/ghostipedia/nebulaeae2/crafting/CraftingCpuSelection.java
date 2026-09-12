package com.ghostipedia.nebulaeae2.crafting;

import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import com.ghostipedia.nebulaeae2.crafting.api.IExtendedCraftingCpu;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public final class CraftingCpuSelection {

    private CraftingCpuSelection() {}

    @Nullable
    public static CraftingCPUCluster select(IGrid grid, ICraftingPlan plan, IActionSource source,
            boolean prioritizePower) {
        long available = grid.getService(IComputeService.class).snapshot().availableCraftingCwut();
        return grid.getCraftingService().getCpus().stream()
                .filter(CraftingCPUCluster.class::isInstance)
                .map(CraftingCPUCluster.class::cast)
                .filter(cpu -> cpu.isActive() && !cpu.isBusy() && !cpu.isDestroyed()
                        && cpu.getAvailableStorage() >= plan.bytes() && cpu.canBeAutoSelectedFor(source))
                .filter(cpu -> reservation(cpu, plan.bytes()) <= available)
                .min(comparator(source, prioritizePower))
                .orElse(null);
    }

    public static long reservation(CraftingCPUCluster cpu, long bytes) {
        CpuCapability hardware = ((IExtendedCraftingCpu) (Object) cpu).nebulae$getCapability();
        if (!CraftingComputeTuning.validCoreCounts(hardware.accelerationCores(), hardware.parallelCores())) {
            return Long.MAX_VALUE;
        }
        return CraftingComputeTuning.jobReservationCwut(bytes, hardware.accelerationCores(), hardware.parallelCores());
    }

    private static Comparator<CraftingCPUCluster> comparator(IActionSource source, boolean prioritizePower) {
        return (left, right) -> {
            int preferred = Boolean.compare(right.isPreferredFor(source), left.isPreferredFor(source));
            if (preferred != 0) {
                return preferred;
            }
            CpuCapability a = ((IExtendedCraftingCpu) (Object) left).nebulae$getCapability();
            CpuCapability b = ((IExtendedCraftingCpu) (Object) right).nebulae$getCapability();
            int speed = Integer.compare(
                    CraftingComputeTuning.executionsPerOpportunity(a.parallelCores())
                            * CraftingComputeTuning.dispatchIntervalTicks(b.accelerationCores()),
                    CraftingComputeTuning.executionsPerOpportunity(b.parallelCores())
                            * CraftingComputeTuning.dispatchIntervalTicks(a.accelerationCores()));
            if (speed != 0) {
                return prioritizePower ? -speed : speed;
            }
            int storage = Long.compare(left.getAvailableStorage(), right.getAvailableStorage());
            if (storage != 0) {
                return storage;
            }
            int dimension = left.getLevel().dimension().location().compareTo(right.getLevel().dimension().location());
            return dimension != 0 ? dimension : left.getBoundsMin().compareTo(right.getBoundsMin());
        };
    }
}
