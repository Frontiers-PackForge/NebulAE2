package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.optimizer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import appeng.core.network.clientbound.ClearPatternAccessTerminalPacket;
import appeng.core.network.clientbound.CraftingStatusPacket;
import appeng.core.network.clientbound.PatternAccessTerminalPacket;
import appeng.client.gui.AESubScreen;

@Mixin({PatternAccessTerminalPacket.class, ClearPatternAccessTerminalPacket.class, CraftingStatusPacket.class})
public abstract class OptimizerTerminalPacketMixin {
    @ModifyExpressionValue(method = "handleOnClient", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;"))
    private Screen nebulae$updateOptimizerParent(Screen screen) {
        while (screen instanceof AESubScreen<?, ?> subScreen) {
            screen = subScreen.getParent();
        }
        return screen;
    }
}
