package com.ghostipedia.nebulaeae2.client.optimizer;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.TabButton;
import appeng.core.definitions.AEItems;

public final class OptimizerToggleButton extends TabButton {
    private boolean allowed;

    public OptimizerToggleButton(OnPress onPress) {
        super(AEItems.PROCESSING_PATTERN.stack(), Component.empty(), onPress);
        setWidth(20);
        setHeight(20);
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
        setMessage(Component.translatable(allowed
                ? "gui.nebulaeae2.optimizer.enabled" : "gui.nebulaeae2.optimizer.disabled"));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (visible && !allowed) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            Icon.S_CLEAR.getBlitter().dest(getX() + 11, getY() + 11).blit(graphics);
            graphics.pose().popPose();
        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(getMessage(), Component.translatable("gui.nebulaeae2.optimizer.toggle_tooltip"));
    }
}
