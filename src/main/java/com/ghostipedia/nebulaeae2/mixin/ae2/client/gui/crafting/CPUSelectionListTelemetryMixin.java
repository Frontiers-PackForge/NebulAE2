package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import com.ghostipedia.nebulaeae2.client.CpuTelemetryText;
import com.ghostipedia.nebulaeae2.crafting.api.ICpuTelemetryList;
import appeng.client.Point;
import appeng.client.gui.Tooltip;
import appeng.client.gui.widgets.CPUSelectionList;
import appeng.menu.me.crafting.CraftingStatusMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CPUSelectionList.class)
public abstract class CPUSelectionListTelemetryMixin {
    @Shadow @Final private CraftingStatusMenu menu;
    @Shadow private CraftingStatusMenu.CraftingCpuListEntry hitTestCpu(Point point) { throw new AssertionError(); }

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void nebulae$showCpuCapability(int mouseX, int mouseY, CallbackInfoReturnable<Tooltip> cir) {
        var entry = hitTestCpu(new Point(mouseX, mouseY));
        if (entry != null && cir.getReturnValue() != null) {
            var data = ((ICpuTelemetryList) menu).nebulae$getCpuTelemetry(entry.serial());
            cir.getReturnValue().getContent().addAll(CpuTelemetryText.lines(data));
        }
    }
}
