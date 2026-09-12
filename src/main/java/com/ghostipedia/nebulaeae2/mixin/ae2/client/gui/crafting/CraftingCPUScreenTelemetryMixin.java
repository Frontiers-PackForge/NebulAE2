package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import com.ghostipedia.nebulaeae2.client.crafting.CpuTelemetryButton;
import com.ghostipedia.nebulaeae2.crafting.api.ICpuTelemetryMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.crafting.CraftingCPUMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingCPUScreen.class)
public abstract class CraftingCPUScreenTelemetryMixin extends AEBaseScreen<CraftingCPUMenu> {

    protected CraftingCPUScreenTelemetryMixin(CraftingCPUMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nebulae$addComputeTelemetry(CraftingCPUMenu menu, Inventory inventory, Component title,
            ScreenStyle style, CallbackInfo ci) {
        widgets.add("nebulaeCompute", new CpuTelemetryButton((ICpuTelemetryMenu) menu));
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void nebulae$updateComputeTelemetry(CallbackInfo ci) {
        var data = ((ICpuTelemetryMenu) menu).nebulae$getCpuTelemetry();
        if (data.paused()) {
            setTextContent(TEXT_ID_DIALOG_TITLE, Component.translatable("gui.nebulaeae2.cpu.paused"));
        }
    }
}
