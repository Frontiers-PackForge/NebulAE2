package com.ghostipedia.nebulaeae2.optimizer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingCalculation;

public final class OptimizerSimulation {
    private static final ThreadLocal<Overrides> ACTIVE = new ThreadLocal<>();

    private OptimizerSimulation() {}

    public static Collection<IPatternDetails> patterns(ICraftingService service, AEKey key,
            Collection<IPatternDetails> normal) {
        var active = ACTIVE.get();
        return active != null && active.service == service ? active.patterns.getOrDefault(key, List.copyOf(normal)) : normal;
    }

    public static Future<ICraftingPlan> calculate(Level level, IGrid grid, ICraftingSimulationRequester requester,
            GenericStack output, Map<AEKey, List<IPatternDetails>> patterns) {
        var overrides = new Overrides(grid.getCraftingService(), Map.copyOf(patterns));
        CraftingCalculation calculation;
        ACTIVE.set(overrides);
        try {
            calculation = new CraftingCalculation(level, grid, requester, output, CalculationStrategy.REPORT_MISSING_ITEMS);
        } finally {
            ACTIVE.remove();
        }
        var future = new FutureTask<ICraftingPlan>(() -> {
            ACTIVE.set(overrides);
            try {
                return calculation.run();
            } finally {
                ACTIVE.remove();
            }
        });
        Thread.ofVirtual().name("Nebulae pattern optimizer preview").start(future);
        return future;
    }

    private record Overrides(ICraftingService service, Map<AEKey, List<IPatternDetails>> patterns) {}
}
