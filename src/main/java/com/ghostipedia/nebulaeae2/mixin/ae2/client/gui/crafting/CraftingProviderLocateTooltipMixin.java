package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.AbstractTableRenderer;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;

@Mixin(AbstractTableRenderer.class)
public abstract class CraftingProviderLocateTooltipMixin {
    @Shadow @Final protected AEBaseScreen<?> screen;

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Lappeng/client/gui/me/crafting/AbstractTableRenderer;getEntryTooltip(Ljava/lang/Object;)Ljava/util/List;"))
    private List<Component> nebulae$addLocateHint(List<Component> original) {
        if (!(screen instanceof CraftConfirmScreen || screen instanceof CraftingCPUScreen<?>)) {
            return original;
        }
        var lines = new ArrayList<>(original);
        lines.add(Component.translatable("gui.nebulaeae2.locating.hint").withStyle(ChatFormatting.GRAY));
        return lines;
    }
}
