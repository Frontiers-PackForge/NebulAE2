package com.ghostipedia.nebulaeae2.client.pattern;

import com.ghostipedia.nebulaeae2.pattern.ProcessingPatternScaling;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;

import java.util.List;

public final class PatternScalingButton extends IconButton {
    private final boolean multiply;

    public PatternScalingButton(boolean multiply, OnPress onPress) {
        super(onPress);
        this.multiply = multiply;
        setHalfSize(true);
        setDisableBackground(true);
        setWidth(8);
        setHeight(8);
        setMessage(Component.translatable("gui.nebulaeae2.pattern_scaling." +
                (multiply ? "multiply" : "divide")).withStyle(ChatFormatting.WHITE));
    }

    @Override
    protected Icon getIcon() {
        return multiply ? Icon.S_ARROW_UP : Icon.S_ARROW_DOWN;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(
                getMessage(),
                Component.translatable("gui.nebulaeae2.pattern_scaling." +
                        (multiply ? "multiply_hint" : "divide_hint")).withStyle(ChatFormatting.GRAY),
                Component.translatable("gui.nebulaeae2.pattern_scaling.click",
                        ProcessingPatternScaling.factor(false, false)).withStyle(ChatFormatting.GRAY),
                Component.translatable("gui.nebulaeae2.pattern_scaling.shift_click",
                        ProcessingPatternScaling.factor(true, false)).withStyle(ChatFormatting.GRAY),
                Component.translatable("gui.nebulaeae2.pattern_scaling.ctrl_click",
                        ProcessingPatternScaling.factor(false, true)).withStyle(ChatFormatting.GRAY));
    }
}
