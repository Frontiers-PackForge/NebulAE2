package com.ghostipedia.nebulaeae2.client.crafting;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class FollowedCraftingToast implements Toast {
    private final List<FormattedCharSequence> lines;

    public FollowedCraftingToast(Component message) {
        lines = Minecraft.getInstance().font.split(message, width() - 16);
    }

    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toasts, long elapsed) {
        var font = toasts.getMinecraft().font;
        graphics.fill(0, 0, width(), height(), 0xFF171D2A);
        graphics.fill(0, 0, 3, height(), 0xFF72DDB0);
        graphics.drawString(font, Component.translatable("gui.nebulaeae2.crafting.finished_title"), 8, 6, 0xFF72DDB0, false);
        int y = 20;
        for (var line : lines) {
            graphics.drawString(font, line, 8, y, 0xFFFFFFFF, false);
            y += font.lineHeight;
        }
        return elapsed >= 5000 ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int width() {
        return 220;
    }

    @Override
    public int height() {
        return 28 + lines.size() * Minecraft.getInstance().font.lineHeight;
    }
}
