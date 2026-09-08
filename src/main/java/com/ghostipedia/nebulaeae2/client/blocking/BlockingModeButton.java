package com.ghostipedia.nebulaeae2.client.blocking;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import com.ghostipedia.nebulaeae2.blocking.BlockingModeMenu;
import com.ghostipedia.nebulaeae2.blocking.ProviderBlockingMode;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public final class BlockingModeButton extends IconButton {
    private final BlockingModeMenu menu;

    public BlockingModeButton(BlockingModeMenu menu, BooleanSupplier backwards) {
        super(button -> menu.nebulae$cycleBlockingMode(backwards.getAsBoolean()));
        this.menu = menu;
    }

    @Override
    protected Icon getIcon() {
        return switch (menu.nebulae$blockingMode()) {
            case OFF -> Icon.BLOCKING_MODE_NO;
            case ANY_CONTENTS -> Icon.BLOCKING_MODE_YES;
            case PATTERN_INPUTS -> Icon.PATTERN_ACCESS_SHOW;
            case NON_INPUT_CONTENTS -> Icon.PATTERN_ACCESS_HIDE;
        };
    }

    @Override
    public List<Component> getTooltipMessage() {
        var mode = menu.nebulae$blockingMode();
        String key = "gui.nebulaeae2.blocking." + mode.name().toLowerCase(Locale.ROOT);
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable(key));
        tooltip.add(Component.translatable(key + ".description").withStyle(ChatFormatting.GRAY));
        if (mode == ProviderBlockingMode.PATTERN_INPUTS || mode == ProviderBlockingMode.NON_INPUT_CONTENTS) {
            tooltip.add(Component.translatable("gui.nebulaeae2.blocking.scope").withStyle(ChatFormatting.GRAY));
        }
        if (mode != ProviderBlockingMode.OFF) {
            tooltip.add(Component.translatable("gui.nebulaeae2.blocking.circuit_exemption").withStyle(ChatFormatting.GRAY));
        }
        return tooltip;
    }
}
