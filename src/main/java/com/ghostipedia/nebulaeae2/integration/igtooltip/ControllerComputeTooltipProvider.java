package com.ghostipedia.nebulaeae2.integration.igtooltip;

import java.util.Locale;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.ghostipedia.nebulaeae2.compute.api.ComputeSnapshot;
import com.ghostipedia.nebulaeae2.compute.api.IComputeService;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import appeng.api.config.PowerUnit;
import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.TooltipProvider;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.block.networking.ControllerBlock;
import appeng.blockentity.networking.ControllerBlockEntity;

public final class ControllerComputeTooltipProvider implements TooltipProvider,
        BodyProvider<ControllerBlockEntity>, ServerDataProvider<ControllerBlockEntity> {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            NebulaeAE2.MODID,
            "controller_compute");
    private static final String DATA = "NebulaeControllerCompute";
    private static final String CAPACITY_CWUT = "CapacityCwut";
    private static final String RESERVED_CWUT = "ReservedCwut";
    private static final String PASSIVE_SHORTFALL_CWUT = "PassiveShortfallCwut";
    private static final String WORK_CEILING_CWUT = "WorkCeilingCwut";
    private static final String RECENT_WORK_AVERAGE_CWUT = "RecentWorkAverageCwut";
    private static final String RECENT_WORK_PEAK_CWUT = "RecentWorkPeakCwut";
    private static final String CHANNEL_OVERLOAD_CWUT = "ChannelOverloadCwut";
    private static final String DEBT_CWU = "DebtCwu";
    private static final String SOURCE_COUNT = "SourceCount";
    private static final String TRACKED_NODE_COUNT = "TrackedNodeCount";
    private static final String THROTTLED_OPERATIONS = "ThrottledOperations";
    private static final String RECOVERY_OPERATIONS = "RecoveryOperations";
    private static final String USED_CHANNELS = "UsedChannels";
    private static final String EU_DEMAND = "EuDemand";

    @Override
    public void registerCommon(CommonRegistration registration) {
        registration.addBlockEntityData(ID, ControllerBlockEntity.class, this);
    }

    @Override
    public void registerClient(ClientRegistration registration) {
        registration.addBlockEntityBody(
                ControllerBlockEntity.class,
                ControllerBlock.class,
                ID,
                this);
    }

    @Override
    public void provideServerData(Player player, ControllerBlockEntity controller, CompoundTag serverData) {
        var grid = controller.getMainNode().getGrid();
        if (grid == null) {
            return;
        }

        ComputeSnapshot snapshot = grid.getService(IComputeService.class).snapshot();
        CompoundTag telemetry = new CompoundTag();
        telemetry.putLong(CAPACITY_CWUT, snapshot.capacityCwut());
        telemetry.putLong(RESERVED_CWUT, snapshot.reservedCwut());
        telemetry.putLong(PASSIVE_SHORTFALL_CWUT, snapshot.passiveShortfallCwut());
        telemetry.putLong(WORK_CEILING_CWUT, snapshot.workCeilingCwut());
        telemetry.putDouble(RECENT_WORK_AVERAGE_CWUT, snapshot.recentWorkAverageCwut());
        telemetry.putLong(RECENT_WORK_PEAK_CWUT, snapshot.recentWorkPeakCwut());
        telemetry.putLong(CHANNEL_OVERLOAD_CWUT, snapshot.channelOverloadCwut());
        telemetry.putLong(DEBT_CWU, snapshot.debtCwu());
        telemetry.putInt(SOURCE_COUNT, snapshot.sourceCount());
        telemetry.putInt(TRACKED_NODE_COUNT, snapshot.trackedNodeCount());
        telemetry.putLong(THROTTLED_OPERATIONS, snapshot.recentThrottledOperations());
        telemetry.putLong(RECOVERY_OPERATIONS, snapshot.recentRecoveryOperations());
        telemetry.putInt(USED_CHANNELS, grid.getPathingService().getUsedChannels());
        telemetry.putDouble(EU_DEMAND, aeToEu(grid.getEnergyService().getAvgPowerUsage()));
        serverData.put(DATA, telemetry);
    }

    @Override
    public void buildTooltip(ControllerBlockEntity controller, TooltipContext context, TooltipBuilder tooltip) {
        CompoundTag serverData = context.serverData();
        if (!serverData.contains(DATA, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag telemetry = serverData.getCompound(DATA);
        long capacityCwut = telemetry.getLong(CAPACITY_CWUT);
        long reservedCwut = telemetry.getLong(RESERVED_CWUT);
        long passiveShortfallCwut = telemetry.getLong(PASSIVE_SHORTFALL_CWUT);
        long workCeilingCwut = telemetry.getLong(WORK_CEILING_CWUT);
        double recentWorkAverageCwut = telemetry.getDouble(RECENT_WORK_AVERAGE_CWUT);
        long recentWorkPeakCwut = telemetry.getLong(RECENT_WORK_PEAK_CWUT);
        long channelOverloadCwut = telemetry.getLong(CHANNEL_OVERLOAD_CWUT);
        long debtCwu = telemetry.getLong(DEBT_CWU);
        long throttledOperations = telemetry.getLong(THROTTLED_OPERATIONS);

        tooltip.addLine(Component.translatable(
                "tooltip.nebulaeae2.controller.compute_load",
                value(reservedCwut,
                        passiveShortfallCwut > 0 ? ChatFormatting.RED : loadColor(reservedCwut, capacityCwut)),
                value(capacityCwut, ChatFormatting.AQUA)));
        if (passiveShortfallCwut > 0) {
            tooltip.addLine(Component.translatable(
                    "tooltip.nebulaeae2.controller.passive_shortfall",
                    value(passiveShortfallCwut, ChatFormatting.RED)));
        }
        tooltip.addLine(Component.translatable(
                "tooltip.nebulaeae2.controller.work_ceiling",
                value(workCeilingCwut, ChatFormatting.AQUA)));
        tooltip.addLine(Component.translatable(
                "tooltip.nebulaeae2.controller.recent_work",
                value(recentWorkAverageCwut, loadColor(recentWorkAverageCwut, workCeilingCwut)),
                value(recentWorkPeakCwut, ChatFormatting.AQUA)));
        if (channelOverloadCwut > 0) {
            tooltip.addLine(Component.translatable(
                    "tooltip.nebulaeae2.controller.channel_overhead",
                    value(channelOverloadCwut, ChatFormatting.GOLD)));
        }
        if (debtCwu > 0) {
            tooltip.addLine(Component.translatable(
                    "tooltip.nebulaeae2.controller.debt",
                    value(debtCwu, ChatFormatting.RED)));
        }
        tooltip.addLine(Component.translatable(
                "tooltip.nebulaeae2.controller.sources",
                value(telemetry.getInt(SOURCE_COUNT), ChatFormatting.AQUA)));
        tooltip.addLine(Component.translatable(
                "tooltip.nebulaeae2.controller.nodes",
                value(telemetry.getInt(TRACKED_NODE_COUNT), ChatFormatting.AQUA)));
        if (throttledOperations > 0) {
            tooltip.addLine(Component.translatable(
                    "tooltip.nebulaeae2.controller.throttled_operations",
                    value(throttledOperations, ChatFormatting.RED)));
        }
        long recoveryOperations = telemetry.getLong(RECOVERY_OPERATIONS);
        if (recoveryOperations > 0) {
            tooltip.addLine(Component.translatable(
                    "tooltip.nebulaeae2.controller.recovery_operations",
                    value(recoveryOperations, ChatFormatting.YELLOW)));
        }
        tooltip.addLine(Component.translatable(
                "tooltip.nebulaeae2.controller.channels",
                value(telemetry.getInt(USED_CHANNELS), ChatFormatting.AQUA)));
        tooltip.addLine(Component.translatable(
                "tooltip.nebulaeae2.controller.eu_demand",
                value(telemetry.getDouble(EU_DEMAND), ChatFormatting.YELLOW)));
    }

    private static double aeToEu(double ae) {
        double fe = PowerUnit.AE.convertTo(PowerUnit.FE, ae);
        return Math.max(0, fe / FeCompat.ratio(false));
    }

    private static ChatFormatting loadColor(long used, long capacity) {
        if (used > capacity) {
            return ChatFormatting.RED;
        }
        if (capacity > 0 && (double) used / capacity >= 0.9) {
            return ChatFormatting.GOLD;
        }
        return ChatFormatting.AQUA;
    }

    private static ChatFormatting loadColor(double used, long capacity) {
        if (used > capacity) {
            return ChatFormatting.RED;
        }
        if (capacity > 0 && used / capacity >= 0.9) {
            return ChatFormatting.GOLD;
        }
        return ChatFormatting.AQUA;
    }

    private static Component value(long value, ChatFormatting color) {
        return Component.literal(Long.toString(value)).withStyle(color);
    }

    private static Component value(double value, ChatFormatting color) {
        return Component.literal(formatDecimal(value)).withStyle(color);
    }

    private static String formatDecimal(double value) {
        if (!Double.isFinite(value)) {
            return "0";
        }
        String formatted = String.format(Locale.ROOT, "%.2f", value);
        int end = formatted.length();
        while (end > 0 && formatted.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && formatted.charAt(end - 1) == '.') {
            end--;
        }
        return formatted.substring(0, end);
    }
}
