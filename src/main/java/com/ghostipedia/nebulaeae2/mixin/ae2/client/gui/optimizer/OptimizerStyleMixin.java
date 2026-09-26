package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.optimizer;

import appeng.client.gui.style.StyleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StyleManager.class)
public abstract class OptimizerStyleMixin {
    @ModifyVariable(method = "loadStyleDoc", at = @At("HEAD"), argsOnly = true)
    private static String nebulae$optimizerTerminalLayout(String path) {
        return "/screens/terminals/pattern_access_terminal.json".equals(path)
                ? "/screens/nebulae_pattern_access_terminal.json" : path;
    }
}
