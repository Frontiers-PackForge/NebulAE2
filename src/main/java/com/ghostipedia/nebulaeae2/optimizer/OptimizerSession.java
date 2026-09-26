package com.ghostipedia.nebulaeae2.optimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.ghostipedia.nebulaeae2.mixin.ae2.menu.optimizer.CraftConfirmOptimizerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.ids.AEComponents;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.IPatternAccessTermMenuHost;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.me.helpers.PlayerSource;
import appeng.me.service.CraftingService;
import appeng.menu.AEBaseMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.parts.AEBasePart;

public final class OptimizerSession {
    private final AEBaseMenu menu;
    private final List<Candidate> candidates = new ArrayList<>();
    private final Set<Integer> selected = new HashSet<>();
    private final Map<Integer, ItemStack> changes = new HashMap<>();
    private final Map<Integer, String> errors = new HashMap<>();
    private IGrid grid;
    private ICraftingPlan originalPlan;
    private Future<ICraftingPlan> simulation;
    private ICraftingPlan proposedPlan;
    private long revision;
    private int page;
    private String status = "idle";
    private boolean previewReady;
    private long nextPreviewTick;
    private String previewOperation = "";
    private long previewFactor = 1;

    public OptimizerSession(AEBaseMenu menu) {
        this.menu = menu;
    }

    public void action(OptimizerRequest request) {
        if (request == null || request.action() == null || request.operation() == null
                || menu.getPlayer().containerMenu != menu || !menu.stillValid(menu.getPlayer())) {
            return;
        }
        if (!request.operation().equals("multiply") && !request.operation().equals("divide")
                && !request.operation().equals("restore")) {
            return;
        }
        if (request.action().equals("refresh")) {
            refresh();
            return;
        }
        if (grid == null || currentGrid() != grid || request.revision() != revision) {
            invalidate("stale");
            return;
        }
        switch (request.action()) {
            case "page" -> page = Math.clamp(request.index(), 0,
                    Math.max(0, (candidates.size() - 1) / OptimizerSnapshot.PAGE_SIZE));
            case "select" -> {
                int index = request.index();
                if (index >= 0 && index < candidates.size() && candidates.get(index).settings.nebulae$allowsOptimization()) {
                    if (!selected.remove(index)) {
                        selected.add(index);
                    }
                    invalidate("selected");
                }
            }
            case "toggle" -> {
                if (request.index() >= 0 && request.index() < candidates.size()) {
                    var candidate = candidates.get(request.index());
                    if (candidate.valid(grid)) {
                        candidate.settings.nebulae$setAllowsOptimization(!candidate.settings.nebulae$allowsOptimization());
                        selected.removeIf(index -> !candidates.get(index).settings.nebulae$allowsOptimization());
                        invalidate("selected");
                    }
                }
            }
            case "preview" -> preview(request.operation(), request.factor());
            case "apply" -> {
                if (request.operation().equals(previewOperation) && request.factor() == previewFactor) {
                    apply();
                } else {
                    invalidate("stale");
                }
            }
            default -> { }
        }
    }

    private IGrid currentGrid() {
        IGridNode node = null;
        if (menu.getTarget() instanceof IPatternAccessTermMenuHost host) {
            node = host.getGridNode();
        } else if (menu.getTarget() instanceof IActionHost host) {
            node = host.getActionableNode();
        }
        return node != null && node.isActive() ? node.getGrid() : null;
    }

    private void refresh() {
        invalidate("selected");
        candidates.clear();
        selected.clear();
        grid = currentGrid();
        page = 0;
        originalPlan = menu instanceof CraftConfirmOptimizerAccessor accessor ? accessor.nebulae$optimizerPlan() : null;
        if (grid == null || menu instanceof CraftConfirmMenu && originalPlan == null) {
            status = "unavailable";
            return;
        }
        Set<PatternContainer> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var node : grid.getNodes()) {
            if (!node.isActive() || !(node.getOwner() instanceof PatternContainer container) || !visited.add(container)) {
                continue;
            }
            OptimizationHost settings = container instanceof PatternProviderLogicHost host
                    ? (OptimizationHost) host.getLogic() : container instanceof OptimizationHost extension ? extension : null;
            if (settings == null) {
                continue;
            }
            var inventory = container.getTerminalPatternInventory();
            for (int slot = 0; slot < inventory.size(); slot++) {
                var stack = inventory.getStackInSlot(slot);
                if (!stack.has(AEComponents.ENCODED_PROCESSING_PATTERN)) {
                    continue;
                }
                var pattern = PatternDetailsHelper.decodePattern(stack, menu.getPlayer().level());
                if (pattern == null || originalPlan != null && !originalPlan.patternTimes().containsKey(pattern)) {
                    continue;
                }
                candidates.add(new Candidate(node, container, settings, slot, stack.copy(), pattern, location(node)));
            }
        }
        candidates.sort(Comparator.comparing((Candidate c) -> c.location).thenComparingInt(c -> c.slot));
        if (originalPlan != null) {
            for (int i = 0; i < candidates.size(); i++) {
                if (candidates.get(i).settings.nebulae$allowsOptimization()) {
                    selected.add(i);
                }
            }
        }
        if (candidates.isEmpty()) {
            status = "empty";
        }
    }

    private static String location(IGridNode node) {
        Object owner = node.getOwner();
        BlockPos pos = owner instanceof BlockEntity block ? block.getBlockPos()
                : owner instanceof AEBasePart part ? part.getBlockEntity().getBlockPos() : BlockPos.ZERO;
        String side = owner instanceof AEBasePart part ? " / " + part.getSide().getSerializedName() : "";
        return node.getLevel().dimension().location() + " " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + side;
    }

    private void invalidate(String reason) {
        close();
        changes.clear();
        errors.clear();
        proposedPlan = null;
        previewReady = false;
        revision++;
        status = reason;
    }

    private void preview(String operation, long factor) {
        long now = menu.getPlayer().level().getGameTime();
        if (now < nextPreviewTick) {
            return;
        }
        nextPreviewTick = now + 10;
        invalidate("calculating");
        previewOperation = operation;
        previewFactor = factor;
        if (selected.isEmpty()) {
            status = "none_selected";
            return;
        }
        for (int index : selected) {
            var candidate = candidates.get(index);
            if (!candidate.valid(grid) || !candidate.settings.nebulae$allowsOptimization()) {
                invalidate("stale");
                return;
            }
            try {
                var changed = PatternOptimization.transform(candidate.before, operation, factor);
                if (!ItemStack.matches(candidate.before, changed)) {
                    changes.put(index, changed);
                }
            } catch (IllegalArgumentException exception) {
                errors.put(index, exception.getMessage());
            }
        }
        if (!errors.isEmpty() || changes.isEmpty()) {
            status = errors.isEmpty() ? "unchanged" : "invalid";
            return;
        }
        if (originalPlan == null) {
            previewReady = true;
            status = "ready";
            return;
        }
        var accessor = (CraftConfirmOptimizerAccessor) menu;
        var output = new GenericStack(accessor.nebulae$optimizerRequestedKey(), accessor.nebulae$optimizerRequestedAmount());
        var host = (IActionHost) menu.getTarget();
        var source = new PlayerSource(menu.getPlayer(), host);
        simulation = OptimizerSimulation.calculate(menu.getPlayer().level(), grid, () -> source, output, substitutions());
    }

    private Map<AEKey, List<IPatternDetails>> substitutions() {
        var affectedKeys = new HashSet<AEKey>();
        for (var change : changes.entrySet()) {
            var original = candidates.get(change.getKey()).pattern;
            affectedKeys.add(original.getPrimaryOutput().what());
        }
        var result = new HashMap<AEKey, List<IPatternDetails>>();
        var service = (CraftingService) grid.getCraftingService();
        for (var key : affectedKeys) {
            var options = new ArrayList<OptimizerPatternOrder.Option<IPatternDetails>>();
            for (var original : service.getCraftingFor(key)) {
                for (var provider : service.getProviders(original)) {
                    boolean found = false;
                    for (int index = 0; index < candidates.size(); index++) {
                        var candidate = candidates.get(index);
                        if (candidate.node.getService(ICraftingProvider.class) != provider || !candidate.pattern.equals(original)) {
                            continue;
                        }
                        found = true;
                        var changed = changes.get(index);
                        var pattern = changed == null ? original
                                : PatternDetailsHelper.decodePattern(changed, menu.getPlayer().level());
                        if (pattern == null) {
                            throw new IllegalArgumentException("processing_only");
                        }
                        options.add(new OptimizerPatternOrder.Option<>(pattern, provider.getPatternPriority()));
                    }
                    if (!found) {
                        options.add(new OptimizerPatternOrder.Option<>(original, provider.getPatternPriority()));
                    }
                }
            }
            result.put(key, OptimizerPatternOrder.sort(options));
        }
        return result;
    }

    public void tick() {
        if (grid != null && currentGrid() != grid) {
            invalidate("stale");
            grid = null;
        }
        if (simulation != null && simulation.isDone()) {
            try {
                proposedPlan = simulation.get();
                previewReady = true;
                status = proposedPlan.simulation() ? "missing" : "ready";
            } catch (Exception exception) {
                status = "calculation_failed";
                previewReady = false;
            }
            simulation = null;
        }
    }

    private void apply() {
        if (!previewReady || changes.isEmpty()) {
            return;
        }
        for (int index : changes.keySet()) {
            var candidate = candidates.get(index);
            if (!candidate.valid(grid) || !candidate.settings.nebulae$allowsOptimization()) {
                invalidate("stale");
                return;
            }
        }
        for (var change : changes.entrySet()) {
            var candidate = candidates.get(change.getKey());
            candidate.container.getTerminalPatternInventory().setItemDirect(candidate.slot, change.getValue().copy());
        }
        refresh();
        status = "applied";
        if (menu instanceof CraftConfirmMenu confirm) {
            confirm.setAutoStart(false);
            confirm.replan();
        }
    }

    public OptimizerSnapshot snapshot() {
        int start = page * OptimizerSnapshot.PAGE_SIZE;
        var rows = new ArrayList<OptimizerSnapshot.Row>();
        for (int index = start; index < Math.min(candidates.size(), start + OptimizerSnapshot.PAGE_SIZE); index++) {
            var candidate = candidates.get(index);
            rows.add(new OptimizerSnapshot.Row(index, candidate.container.getTerminalGroup().name(), candidate.location,
                    candidate.slot, candidate.settings.nebulae$allowsOptimization(), selected.contains(index),
                    candidate.before, changes.getOrDefault(index, ItemStack.EMPTY), errors.getOrDefault(index, "")));
        }
        return new OptimizerSnapshot(revision, page, candidates.size(), selected.size(), status,
                executions(originalPlan), executions(proposedPlan), excess(proposedPlan), previewReady,
                previewOperation, previewFactor, rows);
    }

    private static long executions(ICraftingPlan plan) {
        if (plan == null) {
            return 0;
        }
        long count = 0;
        for (long times : plan.patternTimes().values()) {
            count = saturatedAdd(count, times);
        }
        return count;
    }

    private static long excess(ICraftingPlan plan) {
        if (plan == null) {
            return 0;
        }
        long produced = 0;
        for (var pattern : plan.patternTimes().entrySet()) {
            for (var output : pattern.getKey().getOutputs()) {
                if (output.what().equals(plan.finalOutput().what())) {
                    try {
                        produced = saturatedAdd(produced, Math.multiplyExact(output.amount(), pattern.getValue()));
                    } catch (ArithmeticException exception) {
                        return Long.MAX_VALUE;
                    }
                }
            }
        }
        return Math.max(0, produced - plan.finalOutput().amount());
    }

    private static long saturatedAdd(long a, long b) {
        return a > Long.MAX_VALUE - b ? Long.MAX_VALUE : a + b;
    }

    public void close() {
        if (simulation != null) {
            simulation.cancel(true);
            simulation = null;
        }
    }

    private record Candidate(IGridNode node, PatternContainer container, OptimizationHost settings, int slot,
            ItemStack before, IPatternDetails pattern, String location) {
        boolean valid(IGrid grid) {
            return node.isActive() && node.getGrid() == grid && container.getGrid() == grid
                    && ItemStack.matches(before, container.getTerminalPatternInventory().getStackInSlot(slot));
        }
    }
}
