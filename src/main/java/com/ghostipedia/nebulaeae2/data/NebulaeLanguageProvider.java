package com.ghostipedia.nebulaeae2.data;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import net.minecraft.data.PackOutput;

public final class NebulaeLanguageProvider extends RegistrateLangProvider {
    public NebulaeLanguageProvider(PackOutput output) {
        super(new LanguageRegistrate(), output);
    }

    @Override
    protected void addTranslations() {
        add("item.nebulaeae2.pattern_p2p_tunnel", "Pattern Provider P2P Tunnel");
        add("tooltip.nebulaeae2.pattern_p2p.processing", "Round Robin your pattern providers!");
        add("tooltip.nebulaeae2.pattern_p2p.join_input", "Extra inputs may be assigned by holding a attuned memory card in your offhand");
        add("config.jade.plugin_nebulaeae2.controller_compute", "Nebulae Controller Compute");
        add("gui.nebulaeae2.controller_compute.title", "Network Computation Overview");
        add("gui.nebulaeae2.controller_compute.shortfall", "Shortfall: %s CWU/t");
        add("gui.nebulaeae2.controller_compute.capacity", "Compute Load");
        add("gui.nebulaeae2.controller_compute.channel_overhead", "Excess Channel Tax");
        add("gui.nebulaeae2.controller_compute.device_scale", "Devices | Scale Overhead");
        add("gui.nebulaeae2.controller_compute.channels", "Network Channels");
        add("gui.nebulaeae2.controller_compute.power_usage", "EU Demand");
        add("gui.nebulaeae2.controller_compute.value.rate_pair", "%s / %s CWU/t");
        add("gui.nebulaeae2.controller_compute.value.rate", "%s CWU/t");
        add("gui.nebulaeae2.controller_compute.value.count_pair", "%s / %s");
        add("gui.nebulaeae2.controller_compute.value.device_scale", "%s / %s CWU/t");
        add("gui.nebulaeae2.controller_compute.value.eu_rate", "%s EU/t");
        add("tooltip.nebulaeae2.compute.base_reservation", "Base CWU: %s CWU/t before grid scaling");
        add("tooltip.nebulaeae2.compute.hold_shift", "Hold Shift for CWU details.");
        add("tooltip.nebulaeae2.compute.storage_provider", "Type: Storage Provider: +%s CWU/t");
        add("tooltip.nebulaeae2.compute.crafting_provider", "Type: Crafting Provider: +%s CWU/t");
        add("tooltip.nebulaeae2.compute.device_scaling_base", "Device Scaling: %s CWU/t each for the first %s devices");
        add("tooltip.nebulaeae2.compute.device_scaling_bands", "+%s CWU/t per device for every added %s devices (additive)");
        add("tooltip.nebulaeae2.compute.wireless_booster", "Wireless Booster: +%s CWU/t for each card installed");
        add("tooltip.nebulaeae2.compute.interface_stocking", "Stocking: +%s CWU/t per %s configured slots, rounded up");
        add("tooltip.nebulaeae2.compute.storage_index", "Storage Costs: +%s CWU/t per %s visible item/fluid types");
        add("tooltip.nebulaeae2.compute.storage_index_detail", "Grid-wide and rounded up");
        add("tooltip.nebulaeae2.compute.channel_rating", "Soft Channel Limit: %s channels per routed bottleneck");
        add("tooltip.nebulaeae2.compute.channel_tax", "Overload Tax: starts at %s CWU/t per channel, amount stacks every %s channels");
        add("tooltip.nebulaeae2.compute.physical_links", "Network Upkeep: +%s CWU/t per %s physical grid segments, rounded up");
        add("tooltip.nebulaeae2.controller.compute_load", "Compute Load: %s / %s CWU/t");
        add("tooltip.nebulaeae2.controller.passive_shortfall", "Compute Shortfall: %s CWU/t");
        add("tooltip.nebulaeae2.controller.channel_overhead", "Excess Channel Tax: %s CWU/t (Included in Compute Load)");
        add("tooltip.nebulaeae2.controller.channel_devices", "Channelled Devices: %s");
        add("tooltip.nebulaeae2.controller.device_scale", "Device Scale Cost: %s CWU/t (Included in Compute Load)");
        add("tooltip.nebulaeae2.controller.channels", "Network Channels: %s");
        add("tooltip.nebulaeae2.controller.eu_demand", "EU Demand: %s EU/t");
        add("tooltip.nebulaeae2.cable.grid_compute", "Global CWU: %s / %s CWU/t");
        add("tooltip.nebulaeae2.cable.channel_devices", "Total Connected Devices: %s");
        add("tooltip.nebulaeae2.cable.device_scale", "Device Scale Cost: %s CWU/t");
        add("tooltip.nebulaeae2.cable.channel_overhead", "Channel Overload Tax: %s CWU/t (Included in Global CWU)");
        add("gui.nebulaeae2.controller_compute.infrastructure", "Infrastructure");
        add("gui.nebulaeae2.controller_compute.crafting_reserved", "Crafting reserved");
        add("gui.nebulaeae2.controller_compute.crafting_available", "Available for crafting");
        add("gui.nebulaeae2.controller_compute.funded", "Funded Compute");
        add("gui.nebulaeae2.controller_compute.dispatch", "Crafting Dispatch");
        add("gui.nebulaeae2.controller_compute.paused", "Insufficient CWU");
        add("gui.nebulaeae2.controller_compute.ready", "Ready");
        add("tooltip.nebulaeae2.controller.infrastructure", "Infrastructure: %s CWU/t");
        add("tooltip.nebulaeae2.controller.crafting_reserved", "Crafting reservations: %s CWU/t");
        add("tooltip.nebulaeae2.controller.crafting_available", "Available for new crafts: %s CWU/t");
        add("tooltip.nebulaeae2.controller.crafting_paused", "Crafting paused: INSUFFICIENT CWU! (%s CWU/t short)");
        add("gui.nebulaeae2.cpu.button", "CWU");
        add("gui.nebulaeae2.cpu.no_quote", "No eligible CPU");
        add("gui.nebulaeae2.cpu.stats", "Crafting Unit Stats:");
        add("gui.nebulaeae2.cpu.ticks", "%s ticks");
        add("gui.nebulaeae2.cpu.interval", "Dispatch Frequency: %s");
        add("gui.nebulaeae2.cpu.executions", "Jobs per Dispatch: %s");
        add("gui.nebulaeae2.cpu.execution_cost", "Hardware execution: %s CWU/t");
        add("gui.nebulaeae2.cpu.maximum_cost", "Max CWU/t Allocation: %s");
        add("gui.nebulaeae2.cpu.job_cost", "Current CWU/t Allocation: %s");
        add("gui.nebulaeae2.cpu.available", "Available for new crafts: %s CWU/t");
        add("gui.nebulaeae2.cpu.paused", "Crafting paused: INSUFFICIENT CWU!");
        add("gui.nebulaeae2.cpu.quote", "Job: %s / Available: %s CWU/t");
        add("gui.nebulaeae2.cpu.insufficient", "Insufficient CWU for this craft, check main controller");
        add("gui.nebulaeae2.blocking.off", "Blocking: Off");
        add("gui.nebulaeae2.blocking.off.description", "Send inputs regardless of destination contents.");
        add("gui.nebulaeae2.blocking.any_contents.description", "Wait while the destination contains any items or fluids.");
        add("gui.nebulaeae2.blocking.pattern_inputs.description", "Wait while the destination contains matching pattern inputs.");
        add("gui.nebulaeae2.blocking.non_input_contents.description", "Wait while the destination contains items or fluids that do not match pattern inputs.");
        add("gui.nebulaeae2.blocking.circuit_exemption", "Programmed circuits never block insertions");
        add("gui.nebulaeae2.blocking.any_contents", "Blocking: On, Block on any contents");
        add("gui.nebulaeae2.blocking.pattern_inputs", "Blocking: On, Block on matching pattern inputs");
        add("gui.nebulaeae2.blocking.non_input_contents", "Blocking: On, Block if the contents are not an ingredient within this pattern providers patterns.");
        add("gui.nebulaeae2.blocking.scope", "Input Ingredient Checks are done provider wide, for every pattern. This means the check is per-providerm, and providers sharing inputs may be blind without special blocking modes.");
        add("gui.nebulaeae2.blocking.circuit", "Programmed circuits are exempt. Other unencoded catalysts count as non-input contents.");
        add("gui.nebulaeae2.blocking.symbol.off", "0");
        add("gui.nebulaeae2.blocking.symbol.any_contents", "A");
        add("gui.nebulaeae2.blocking.symbol.pattern_inputs", "I");
        add("gui.nebulaeae2.blocking.symbol.non_input_contents", "N");
        add("gui.nebulaeae2.pattern_scaling.multiply", "Multiply Pattern");
        add("gui.nebulaeae2.pattern_scaling.divide", "Divide Pattern");
        add("gui.nebulaeae2.pattern_scaling.multiply_hint", "Multiply all processing inputs & outputs.");
        add("gui.nebulaeae2.pattern_scaling.divide_hint", "Divide all processing inputs & outputs.");
        add("gui.nebulaeae2.pattern_scaling.click", "Click : %sx");
        add("gui.nebulaeae2.pattern_scaling.shift_click", "Shift Click : %sx");
        add("gui.nebulaeae2.pattern_scaling.ctrl_click", "CTRL Click : %sx");
        add("gui.nebulaeae2.pattern_scaling.non_exact", "Cannot divide this pattern exactly. No amounts were changed.");
        add("gui.nebulaeae2.pattern_scaling.too_large", "The scaled pattern exceeds the amount limit. No amounts were changed.");
        add("gui.nebulaeae2.pattern_scaling.invalid_operation", "Invalid pattern scaling operation. No amounts were changed.");
        add("gui.nebulaeae2.pattern_scaling.invalid_amount", "This pattern contains an invalid amount. No amounts were changed.");
        add("tooltip.nebulaeae2.pattern_author", "Encoded by: %s");
        add("gui.nebulaeae2.crafting.start_follow", "Start with Follow");
        add("gui.nebulaeae2.crafting.finished", "Crafting of %s * [%s] finished in %s.");
        add("gui.nebulaeae2.locating.none", "No active provider for this resource was found in this dimension.");
        add("gui.nebulaeae2.locating.hint", "Shift-click to locate pattern providers.");
        add("gui.nebulaeae2.locating.found", "Highlighted %s provider(s) for 20 seconds. Nearest: %s, %s, %s.");
        add("gui.nebulaeae2.stock.demand", "Stock: %s");
        add("gui.nebulaeae2.stock.initial", "Initial: %s");
        add("gui.nebulaeae2.stock.none", "No stock");
        add("gui.nebulaeae2.stock.unavailable", "Initial stock snapshot unavailable for this job.");
        add("gui.nebulaeae2.stock.initial_details", "Initial stock used: %s / %s available at admission.");
        add("gui.nebulaeae2.stock.plan_details", "Stock demand: %s / %s withdrawable at snapshot.");
        add("gui.nebulaeae2.stock.roles", "Planned withdrawal: %s; missing stock: %s. Crafting outputs excluded.");
        add("gui.nebulaeae2.stock.refresh", "Availability is refreshed every second for this plan.");
    }

    private static final class LanguageRegistrate extends Registrate {
        private LanguageRegistrate() {
            super(NebulaeAE2.MODID);
        }
    }
}
