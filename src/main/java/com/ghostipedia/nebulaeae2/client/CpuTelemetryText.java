package com.ghostipedia.nebulaeae2.client;

import java.util.ArrayList;
import java.util.List;
import com.ghostipedia.nebulaeae2.crafting.CpuTelemetry;
import com.ghostipedia.nebulaeae2.crafting.CraftingComputeTuning;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class CpuTelemetryText {
    private CpuTelemetryText() {}

    public static List<Component> lines(CpuTelemetry data) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.nebulaeae2.cpu.stats").withStyle(ChatFormatting.GOLD));
        if (!data.present()) {
            lines.add(Component.translatable("gui.nebulaeae2.cpu.no_quote").withStyle(ChatFormatting.WHITE));
            return lines;
        }
        lines.add(stat("maximum_cost", Component.literal(Long.toString(data.maximumReservation()))));
        lines.add(stat("job_cost", Component.literal(Long.toString(data.reservation()))));
        lines.add(stat("interval", Component.translatable("gui.nebulaeae2.cpu.ticks",
                CraftingComputeTuning.dispatchIntervalTicks(data.acceleration()))));
        lines.add(stat("executions", Component.literal(Integer.toString(
                CraftingComputeTuning.executionsPerOpportunity(data.parallel())))));
        return lines;
    }

    public static Component joined(CpuTelemetry data) {
        var result = Component.empty();
        for (var line : lines(data)) {
            if (!result.getSiblings().isEmpty()) {
                result.append("\n");
            }
            result.append(line);
        }
        return result;
    }

    private static Component stat(String key, Component value) {
        return Component.translatable("gui.nebulaeae2.cpu." + key,
                value.copy().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.WHITE);
    }
}
