package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import com.ghostipedia.nebulaeae2.client.CpuTelemetryText;
import com.ghostipedia.nebulaeae2.crafting.api.ICpuTelemetryMenu;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.crafting.CraftConfirmMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftConfirmScreen.class)
public abstract class CraftConfirmScreenTelemetryMixin extends AEBaseScreen<CraftConfirmMenu> {
    @Shadow @Final private Button selectCPU;
    @Shadow @Final private Button start;

    protected CraftConfirmScreenTelemetryMixin(CraftConfirmMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }

    @ModifyExpressionValue(method = "updateBeforeRender", at = @At(value = "INVOKE", target = "Lappeng/menu/me/crafting/CraftConfirmMenu$SyncableSubmitResult;result()Lappeng/api/networking/crafting/ICraftingSubmitResult;"))
    private ICraftingSubmitResult nebulae$keepComputeFailureOnPlan(ICraftingSubmitResult original) {
        return ((ICpuTelemetryMenu) menu).nebulae$admissionFailed() ? null : original;
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void nebulae$showQuote(CallbackInfo ci) {
        var extension = (ICpuTelemetryMenu) menu;
        var data = extension.nebulae$getCpuTelemetry();
        selectCPU.setTooltip(Tooltip.create(CpuTelemetryText.joined(data)));
        if (menu.getPlan() == null || menu.getPlan().isSimulation()) {
            return;
        }
        boolean insufficient = extension.nebulae$admissionFailed() || data.paused();
        var text = data.present()
                ? Component.translatable("gui.nebulaeae2.cpu.quote", data.reservation(), data.available())
                : Component.translatable("gui.nebulaeae2.cpu.no_quote");
        if (insufficient) {
            text = Component.translatable("gui.nebulaeae2.cpu.insufficient").withStyle(ChatFormatting.RED);
        }
        setTextContent("cpu_status", text);
        if (data.paused() || !data.present()) {
            start.active = false;
        }
    }
}
