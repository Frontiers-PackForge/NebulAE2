package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.networktool;

import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ghostipedia.nebulaeae2.compute.api.ComputeSnapshot;
import com.ghostipedia.nebulaeae2.compute.network.NetworkStatusComputeExtension;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;

import appeng.api.config.PowerUnit;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.menu.me.networktool.NetworkStatus;
import appeng.menu.me.networktool.NetworkStatusMenu;

@Mixin(NetworkStatusScreen.class)
public abstract class NetworkStatusScreenMixin extends AEBaseScreen<NetworkStatusMenu> {

    @Unique
    private static final int LABEL_LEFT = 16;

    @Unique
    private static final int VALUE_RIGHT = 179;

    @Unique
    private static final int WARNING_COLOR = 0xFF7A4B00;

    @Shadow
    @Final
    private Scrollbar scrollbar;

    @Shadow
    private NetworkStatus status;

    protected NetworkStatusScreenMixin(NetworkStatusMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void nebulae$configureComputeTelemetry(CallbackInfo callback) {
        boolean controllerMenu = nebulae$isControllerMenu();
        scrollbar.setVisible(!controllerMenu);
        if (!controllerMenu) {
            return;
        }

        setTextContent("dialog_title", Component.translatable("gui.nebulaeae2.controller_compute.title"));
        setTextHidden("stored_power", true);
        setTextHidden("max_power", true);
        setTextHidden("channel_power_rate", true);
        setTextHidden("power_usage_rate", true);
        setTextHidden("power_input_rate", true);
    }

    @Inject(method = "drawFG", at = @At("HEAD"), cancellable = true)
    private void nebulae$drawComputeTelemetry(GuiGraphics graphics, int offsetX, int offsetY, int mouseX,
            int mouseY, CallbackInfo callback) {
        if (!nebulae$isControllerMenu()) {
            return;
        }

        var snapshot = ((NetworkStatusComputeExtension) (Object) status).nebulae$getComputeSnapshot();
        int textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();

        nebulae$drawMetric(graphics, "capacity",
                nebulae$ratePair(snapshot.reservedCwut(), snapshot.capacityCwut()), 24,
                nebulae$loadColor(snapshot.reservedCwut(), snapshot.capacityCwut()));
        nebulae$drawMetric(graphics, "work_capacity", nebulae$rate(snapshot.workCeilingCwut()), 37, textColor);
        nebulae$drawMetric(graphics, "recent_work",
                nebulae$recentWork(snapshot.recentWorkAverageCwut(), snapshot.recentWorkPeakCwut()), 50,
                nebulae$loadColor(snapshot.recentWorkAverageCwut(), snapshot.workCeilingCwut()));
        nebulae$drawMetric(graphics, "debt", nebulae$work(snapshot.debtCwu()), 63,
                snapshot.debtCwu() > 0 ? WARNING_COLOR : textColor);
        nebulae$drawMetric(graphics, "sources", nebulae$count(snapshot.sourceCount()), 76, textColor);
        nebulae$drawMetric(graphics, "nodes", nebulae$count(snapshot.trackedNodeCount()), 89, textColor);
        nebulae$drawMetric(graphics, "throttled", nebulae$count(snapshot.recentThrottledOperations()), 102,
                snapshot.recentThrottledOperations() > 0 ? WARNING_COLOR : textColor);
        nebulae$drawMetric(graphics, "channel_overhead", nebulae$rate(snapshot.channelOverloadCwut()), 115, textColor);
        nebulae$drawMetric(graphics, "channels", nebulae$count(status.getChannelsUsed()), 139, textColor);
        nebulae$drawMetric(graphics, "power_usage", nebulae$powerRate(status.getAveragePowerUsage()), 152,
                textColor);
        callback.cancel();
    }

    @Unique
    private boolean nebulae$isControllerMenu() {
        return menu.getType() == NetworkStatusMenu.CONTROLLER_TYPE;
    }

    @Unique
    private void nebulae$drawMetric(GuiGraphics graphics, String labelKey, Component value, int y, int valueColor) {
        var label = Component.translatable("gui.nebulaeae2.controller_compute." + labelKey);
        int textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        graphics.drawString(font, label, LABEL_LEFT, y, textColor, false);
        graphics.drawString(font, value, VALUE_RIGHT - font.width(value), y, valueColor, false);
    }

    @Unique
    private static Component nebulae$ratePair(long used, long capacity) {
        return Component.translatable(
                "gui.nebulaeae2.controller_compute.value.rate_pair",
                nebulae$compact(used),
                nebulae$compact(capacity));
    }

    @Unique
    private static Component nebulae$rate(long value) {
        return Component.translatable("gui.nebulaeae2.controller_compute.value.rate", nebulae$compact(value));
    }

    @Unique
    private static Component nebulae$work(long value) {
        return Component.translatable("gui.nebulaeae2.controller_compute.value.work", nebulae$compact(value));
    }

    @Unique
    private static Component nebulae$recentWork(double average, long peak) {
        return Component.translatable(
                "gui.nebulaeae2.controller_compute.value.recent_work",
                nebulae$compact(average),
                nebulae$compact(peak));
    }

    @Unique
    private static Component nebulae$powerRate(double aePerTick) {
        double fePerTick = PowerUnit.AE.convertTo(PowerUnit.FE, aePerTick);
        double euPerTick = Math.max(0, fePerTick / FeCompat.ratio(false));
        return Component.translatable("gui.nebulaeae2.controller_compute.value.eu_rate",
                nebulae$compact(euPerTick));
    }

    @Unique
    private static Component nebulae$count(long value) {
        return Component.literal(nebulae$compact(value));
    }

    @Unique
    private int nebulae$loadColor(long used, long capacity) {
        if (used > capacity) {
            return style.getColor(PaletteColor.ERROR).toARGB();
        }
        if (capacity > 0 && (double) used / capacity >= 0.9) {
            return WARNING_COLOR;
        }
        return style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
    }

    @Unique
    private int nebulae$loadColor(double used, long capacity) {
        if (used > capacity) {
            return style.getColor(PaletteColor.ERROR).toARGB();
        }
        if (capacity > 0 && used / capacity >= 0.9) {
            return WARNING_COLOR;
        }
        return style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
    }

    @Unique
    private static String nebulae$compact(long value) {
        long magnitude = value == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(value);
        if (magnitude >= 1_000_000_000_000L) {
            return nebulae$scaled(value, 1_000_000_000_000L, "T");
        }
        if (magnitude >= 1_000_000_000L) {
            return nebulae$scaled(value, 1_000_000_000L, "G");
        }
        if (magnitude >= 1_000_000L) {
            return nebulae$scaled(value, 1_000_000L, "M");
        }
        if (magnitude >= 1_000L) {
            return nebulae$scaled(value, 1_000L, "k");
        }
        return Long.toString(value);
    }

    @Unique
    private static String nebulae$compact(double value) {
        if (!Double.isFinite(value)) {
            return "0";
        }

        double magnitude = Math.abs(value);
        if (magnitude >= 1_000_000_000_000.0) {
            return nebulae$scaled(value, 1_000_000_000_000.0, "T");
        }
        if (magnitude >= 1_000_000_000.0) {
            return nebulae$scaled(value, 1_000_000_000.0, "G");
        }
        if (magnitude >= 1_000_000.0) {
            return nebulae$scaled(value, 1_000_000.0, "M");
        }
        if (magnitude >= 1_000.0) {
            return nebulae$scaled(value, 1_000.0, "k");
        }
        return nebulae$decimal(value);
    }

    @Unique
    private static String nebulae$scaled(long value, long divisor, String suffix) {
        double scaled = (double) value / divisor;
        String pattern = Math.abs(scaled) >= 100 ? "%.0f%s" : Math.abs(scaled) >= 10 ? "%.1f%s" : "%.2f%s";
        return String.format(Locale.ROOT, pattern, scaled, suffix);
    }

    @Unique
    private static String nebulae$scaled(double value, double divisor, String suffix) {
        return nebulae$decimal(value / divisor) + suffix;
    }

    @Unique
    private static String nebulae$decimal(double value) {
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
