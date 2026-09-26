package com.ghostipedia.nebulaeae2.optimizer;

import java.util.function.BiConsumer;

public final class OptimizerTranslations {
    private OptimizerTranslations() {}

    public static void add(BiConsumer<String, String> add) {
        add.accept("gui.nebulaeae2.optimizer.title", "Pattern Optimizer");
        add.accept("gui.nebulaeae2.optimizer.open", "Optimize");
        add.accept("gui.nebulaeae2.optimizer.details", "Details");
        add.accept("gui.nebulaeae2.optimizer.enabled_short", "On");
        add.accept("gui.nebulaeae2.optimizer.disabled_short", "Off");
        add.accept("gui.nebulaeae2.optimizer.enabled", "Optimizer: On");
        add.accept("gui.nebulaeae2.optimizer.disabled", "Optimizer: Off");
        add.accept("gui.nebulaeae2.optimizer.toggle_tooltip", "Allow the optimizer to modify patterns at this location. Changes still require a preview and confirmation.");
        add.accept("gui.nebulaeae2.optimizer.select_location", "Include this pattern slot in the preview and application.");
        add.accept("gui.nebulaeae2.optimizer.slot", "Slot %s");
        add.accept("gui.nebulaeae2.optimizer.factor", "Whole-number factor");
        add.accept("gui.nebulaeae2.optimizer.multiply", "Multiply");
        add.accept("gui.nebulaeae2.optimizer.divide", "Divide");
        add.accept("gui.nebulaeae2.optimizer.restore", "Restore");
        add.accept("gui.nebulaeae2.optimizer.preview", "Preview");
        add.accept("gui.nebulaeae2.optimizer.back", "Back");
        add.accept("gui.nebulaeae2.optimizer.refresh", "Refresh");
        add.accept("gui.nebulaeae2.optimizer.apply", "Apply");
        add.accept("gui.nebulaeae2.optimizer.page", "%s / %s  (%s selected)");
        add.accept("gui.nebulaeae2.optimizer.detail_page", "Quantities: %s / %s");
        add.accept("gui.nebulaeae2.optimizer.input", "In");
        add.accept("gui.nebulaeae2.optimizer.output", "Out");
        add.accept("gui.nebulaeae2.optimizer.quantity", "%s %s: %s -> %s");
        add.accept("gui.nebulaeae2.optimizer.multiplier", "Applied multiplier: x%s -> x%s");
        add.accept("gui.nebulaeae2.optimizer.impact", "Runs %s -> %s | Excess %s");
        add.accept("gui.nebulaeae2.optimizer.impact_tooltip", "Executions in the recalculated crafting plan, and excess of the requested output. Pattern changes are permanent; they do not start the job.");
        add.accept("gui.nebulaeae2.optimizer.idle", "Loading patterns...");
        add.accept("gui.nebulaeae2.optimizer.selected", "Choose locations and preview a factor.");
        add.accept("gui.nebulaeae2.optimizer.stale", "Patterns or network changed. Refresh first.");
        add.accept("gui.nebulaeae2.optimizer.unavailable", "No connected network or crafting plan.");
        add.accept("gui.nebulaeae2.optimizer.empty", "No processing patterns in this view.");
        add.accept("gui.nebulaeae2.optimizer.calculating", "Recalculating with proposed patterns...");
        add.accept("gui.nebulaeae2.optimizer.none_selected", "Select at least one pattern location.");
        add.accept("gui.nebulaeae2.optimizer.unchanged", "This factor makes no changes.");
        add.accept("gui.nebulaeae2.optimizer.invalid", "A pattern cannot scale. Open its details.");
        add.accept("gui.nebulaeae2.optimizer.ready", "Preview ready. Apply changes permanently.");
        add.accept("gui.nebulaeae2.optimizer.missing", "Preview needs additional materials.");
        add.accept("gui.nebulaeae2.optimizer.calculation_failed", "Preview failed. Refresh and try again.");
        add.accept("gui.nebulaeae2.optimizer.applied", "Patterns updated. Crafting plan refreshed.");
        add.accept("gui.nebulaeae2.optimizer.invalid_factor", "Use a positive whole-number factor.");
        add.accept("gui.nebulaeae2.optimizer.non_exact", "Divisor must exactly divide applied scaling.");
        add.accept("gui.nebulaeae2.optimizer.overflow", "Encoded quantities would overflow.");
        add.accept("gui.nebulaeae2.optimizer.quantity_limit", "A slot exceeds the supported 999,999-unit limit.");
        add.accept("gui.nebulaeae2.optimizer.processing_only", "Only valid processing patterns can scale.");
        add.accept("gui.nebulaeae2.optimizer.unmarked", "Only optimizer-scaled patterns can reduce.");
    }
}
