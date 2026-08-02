package com.ghostipedia.nebulaeae2.integration.igtooltip;

import static com.ghostipedia.nebulaeae2.integration.igtooltip.ComputeTooltipFormatting.loadColor;
import static com.ghostipedia.nebulaeae2.integration.igtooltip.ComputeTooltipFormatting.value;

import com.ghostipedia.nebulaeae2.compute.api.ComputeSnapshot;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.integrations.igtooltip.PartTooltips;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;

final class CableComputeTooltipProvider {

    private static final int PRIORITY = 900;
    private static final String DATA = "NebulaeCableCompute";
    private static final String CAPACITY_CWUT = "CapacityCwut";
    private static final String RESERVED_CWUT = "ReservedCwut";
    private static final String PASSIVE_SHORTFALL_CWUT = "PassiveShortfallCwut";
    private static final String WORK_BUDGET_CWUT = "WorkBudgetCwut";
    private static final String RECENT_WORK_AVERAGE_CWUT = "RecentWorkAverageCwut";
    private static final String RECENT_WORK_PEAK_CWUT = "RecentWorkPeakCwut";
    private static final String CHANNEL_OVERLOAD_CWUT = "ChannelOverloadCwut";
    private static final String CHANNEL_DEVICE_COUNT = "ChannelDeviceCount";
    private static final String DEVICE_SCALE_CWUT = "DeviceScaleCwut";
    private static boolean commonRegistered;
    private static boolean clientRegistered;

    private CableComputeTooltipProvider() {
    }

    static synchronized void registerCommon() {
        if (!commonRegistered) {
            PartTooltips.addServerData(ICablePart.class, CableComputeTooltipProvider::provideServerData, PRIORITY);
            commonRegistered = true;
        }
    }

    static synchronized void registerClient() {
        if (!clientRegistered) {
            PartTooltips.addBody(ICablePart.class, CableComputeTooltipProvider::buildTooltip, PRIORITY);
            clientRegistered = true;
        }
    }

    private static void provideServerData(Player player, ICablePart cable, CompoundTag serverData) {
        var node = cable.getGridNode();
        if (node == null) {
            return;
        }

        try {
            ComputeSnapshot snapshot = node.getGrid().getService(IComputeService.class).snapshot();
            CompoundTag telemetry = new CompoundTag();
            telemetry.putLong(CAPACITY_CWUT, snapshot.capacityCwut());
            telemetry.putLong(RESERVED_CWUT, snapshot.reservedCwut());
            telemetry.putLong(PASSIVE_SHORTFALL_CWUT, snapshot.passiveShortfallCwut());
            telemetry.putLong(WORK_BUDGET_CWUT, snapshot.workBudgetCwut());
            telemetry.putDouble(RECENT_WORK_AVERAGE_CWUT, snapshot.recentWorkAverageCwut());
            telemetry.putLong(RECENT_WORK_PEAK_CWUT, snapshot.recentWorkPeakCwut());
            telemetry.putLong(CHANNEL_OVERLOAD_CWUT, snapshot.channelOverloadCwut());
            telemetry.putLong(CHANNEL_DEVICE_COUNT, snapshot.channelDeviceCount());
            telemetry.putLong(DEVICE_SCALE_CWUT, snapshot.deviceScaleCwut());
            serverData.put(DATA, telemetry);
        } catch (IllegalStateException ignored) {
        }
    }

    private static void buildTooltip(ICablePart cable, TooltipContext context, TooltipBuilder tooltip) {
        CompoundTag serverData = context.serverData();
        if (!serverData.contains(DATA, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag telemetry = serverData.getCompound(DATA);
        long capacityCwut = telemetry.getLong(CAPACITY_CWUT);
        long reservedCwut = telemetry.getLong(RESERVED_CWUT);
        long passiveShortfallCwut = telemetry.getLong(PASSIVE_SHORTFALL_CWUT);
        double recentWorkAverageCwut = telemetry.getDouble(RECENT_WORK_AVERAGE_CWUT);
        long recentWorkPeakCwut = telemetry.getLong(RECENT_WORK_PEAK_CWUT);
        long channelOverloadCwut = telemetry.getLong(CHANNEL_OVERLOAD_CWUT);
        long deviceScaleCwut = telemetry.getLong(DEVICE_SCALE_CWUT);
        long workBudgetCwut = telemetry.getLong(WORK_BUDGET_CWUT);

        tooltip.addLine(Component.translatable(
                "tooltip.nebulaeae2.cable.grid_compute",
                value(reservedCwut,
                        passiveShortfallCwut > 0 ? ChatFormatting.RED : loadColor(reservedCwut, capacityCwut)),
                value(capacityCwut, ChatFormatting.AQUA)));
        tooltip.addLine(Component.translatable(
                "tooltip.nebulaeae2.cable.recent_work",
                value(recentWorkAverageCwut, loadColor(recentWorkAverageCwut, workBudgetCwut)),
                value(recentWorkPeakCwut, ChatFormatting.AQUA)));
        tooltip.addLine(Component.translatable(
                "tooltip.nebulaeae2.cable.channel_devices",
                value(telemetry.getLong(CHANNEL_DEVICE_COUNT), ChatFormatting.AQUA)));
        if (deviceScaleCwut > 0) {
            tooltip.addLine(Component.translatable(
                    "tooltip.nebulaeae2.cable.device_scale",
                    value(deviceScaleCwut, ChatFormatting.AQUA)));
        }
        if (channelOverloadCwut > 0) {
            tooltip.addLine(Component.translatable(
                    "tooltip.nebulaeae2.cable.channel_overhead",
                    value(channelOverloadCwut, ChatFormatting.AQUA)));
        }
    }
}
