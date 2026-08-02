package com.ghostipedia.nebulaeae2.integration.igtooltip;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

final class ComputeTooltipFormatting {

    private ComputeTooltipFormatting() {
    }

    static ChatFormatting loadColor(long used, long capacity) {
        if (used > capacity) {
            return ChatFormatting.RED;
        }
        if (capacity > 0 && (double) used / capacity >= 0.9) {
            return ChatFormatting.GOLD;
        }
        return ChatFormatting.AQUA;
    }

    static ChatFormatting loadColor(double used, long capacity) {
        if (used > capacity) {
            return ChatFormatting.RED;
        }
        if (capacity > 0 && used / capacity >= 0.9) {
            return ChatFormatting.GOLD;
        }
        return ChatFormatting.AQUA;
    }

    static Component value(long value, ChatFormatting color) {
        return Component.literal(Long.toString(value)).withStyle(color);
    }

    static Component value(double value, ChatFormatting color) {
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
