package com.ghostipedia.nebulaeae2.client.crafting;

import com.ghostipedia.nebulaeae2.client.CpuTelemetryText;
import com.ghostipedia.nebulaeae2.crafting.api.ICpuTelemetryMenu;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class CpuTelemetryButton extends IconButton {
    private final ICpuTelemetryMenu menu;

    public CpuTelemetryButton(ICpuTelemetryMenu menu) {
        super(button -> {});
        this.menu = menu;
    }

    @Override
    protected Icon getIcon() {
        return Icon.PATTERN_TERMINAL_ALL;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return CpuTelemetryText.lines(menu.nebulae$getCpuTelemetry());
    }
}
