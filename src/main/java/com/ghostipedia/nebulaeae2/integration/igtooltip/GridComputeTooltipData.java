package com.ghostipedia.nebulaeae2.integration.igtooltip;

import com.ghostipedia.nebulaeae2.compute.api.ComputeSnapshot;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import static com.ghostipedia.nebulaeae2.integration.igtooltip.ComputeTooltipFormatting.value;

final class GridComputeTooltipData {
    private GridComputeTooltipData() {}

    static CompoundTag write(ComputeSnapshot snapshot) {
        CompoundTag data = new CompoundTag();
        data.putLong("Capacity", snapshot.capacityCwut());
        data.putLong("Funded", snapshot.fundedCwut());
        data.putLong("Infrastructure", snapshot.infrastructureReservedCwut());
        data.putLong("Crafting", snapshot.craftingReservedCwut());
        data.putLong("ChannelTax", snapshot.channelOverloadCwut());
        data.putLong("Devices", snapshot.channelDeviceCount());
        return data;
    }

    static void append(CompoundTag data, TooltipBuilder tooltip, boolean expanded) {
        ComputeSnapshot snapshot = new ComputeSnapshot(data.getLong("Capacity"), data.getLong("Funded"),
                data.getLong("Infrastructure"), data.getLong("Crafting"), data.getLong("ChannelTax"),
                data.getLong("Devices"));
        tooltip.addLine(Component.translatable("tooltip.nebulaeae2.controller.compute_load",
                value(snapshot.reservedCwut(), snapshot.craftingPaused() ? ChatFormatting.RED : ChatFormatting.AQUA),
                value(snapshot.capacityCwut(), ChatFormatting.AQUA)));
        add(tooltip, "infrastructure", snapshot.infrastructureReservedCwut());
        if (expanded) {
            add(tooltip, "channel_overhead", snapshot.channelOverloadCwut());
            add(tooltip, "channel_devices", snapshot.channelDeviceCount());
            add(tooltip, "device_scale", snapshot.deviceScaleCwut());
        } else {
            tooltip.addLine(Component.translatable("tooltip.nebulaeae2.compute.hold_shift")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (snapshot.craftingPaused()) {
            tooltip.addLine(Component.translatable("tooltip.nebulaeae2.controller.crafting_paused",
                    value(snapshot.shortfallCwut(), ChatFormatting.RED)).withStyle(ChatFormatting.RED));
        }
    }

    private static void add(TooltipBuilder tooltip, String key, long amount) {
        tooltip.addLine(Component.translatable("tooltip.nebulaeae2.controller." + key,
                value(amount, ChatFormatting.AQUA)));
    }
}
