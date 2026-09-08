package com.ghostipedia.nebulaeae2.client.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ghostipedia.nebulaeae2.crafting.stock.StockUsageMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.client.gui.AEBaseScreen;
import appeng.core.localization.GuiText;

public final class StockUsagePresentation {
    private StockUsagePresentation() {}

    public static List<Component> append(AEBaseScreen<?> screen, AEKey key, List<Component> original,
            boolean initial, boolean tooltip) {
        if (!(screen.getMenu() instanceof StockUsageMenu menu) || key == null) {
            return original;
        }
        var snapshot = menu.nebulae$stockUsage();
        var amounts = snapshot.entries().get(key);
        if (amounts == null || amounts.required() <= 0) {
            if (initial && !snapshot.initial() && tooltip) {
                var lines = new ArrayList<>(original);
                lines.add(Component.translatable("gui.nebulaeae2.stock.unavailable").withStyle(ChatFormatting.GRAY));
                return lines;
            }
            return original;
        }
        var lines = new ArrayList<>(original);
        double percentage = amounts.percentage();
        var color = amounts.available() == 0 || percentage >= 100 ? ChatFormatting.RED
                : percentage >= 75 ? ChatFormatting.GOLD : ChatFormatting.GREEN;
        Component value = amounts.available() == 0 ? Component.translatable("gui.nebulaeae2.stock.none")
                : Component.literal(String.format(Locale.ROOT, percentage >= 10000 && !tooltip ? "%.2g%%" : "%.1f%%", percentage));
        Component stockLine = Component.translatable(initial ? "gui.nebulaeae2.stock.initial" : "gui.nebulaeae2.stock.demand", value)
                .withStyle(color);
        if (tooltip && !initial) {
            int stockLineIndex = lines.size();
            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                if (line.getContents() instanceof TranslatableContents translation
                        && translation.getKey().equals(GuiText.FromStorage.getTranslationKey())) {
                    lines.set(i, line.copy().append(" / " + key.formatAmount(amounts.available(), AmountFormat.FULL))
                            .withStyle(color));
                    stockLineIndex = i + 1;
                    break;
                }
            }
            lines.add(stockLineIndex, stockLine);
            return lines;
        }
        lines.add(stockLine);
        if (tooltip) {
            lines.add(Component.translatable("gui.nebulaeae2.stock.initial_details",
                    key.formatAmount(amounts.required(), AmountFormat.FULL),
                    key.formatAmount(amounts.available(), AmountFormat.FULL)));
        }
        return lines;
    }
}
