package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import appeng.client.gui.style.StyleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StyleManager.class)
public abstract class CraftingCPUStyleMixin {
    @ModifyVariable(method = "loadStyleDoc", at = @At("HEAD"), argsOnly = true)
    private static String nebulae$useTelemetryLayout(String path) {
        return switch (path) {
            case "/screens/crafting_cpu.json" -> "/screens/nebulae_crafting_cpu.json";
            case "/screens/crafting_status.json" -> "/screens/nebulae_crafting_status.json";
            default -> path;
        };
    }
}
