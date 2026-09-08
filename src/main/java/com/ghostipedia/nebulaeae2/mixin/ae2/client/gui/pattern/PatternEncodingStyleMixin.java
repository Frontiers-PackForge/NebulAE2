package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.pattern;

import appeng.client.gui.style.StyleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StyleManager.class)
public abstract class PatternEncodingStyleMixin {
    @ModifyVariable(method = "loadStyleDoc", at = @At("HEAD"), argsOnly = true)
    private static String nebulae$usePatternControls(String path) {
        return switch (path) {
            case "/screens/terminals/pattern_encoding_terminal.json" ->
                    "/screens/terminals/nebulae_pattern_encoding_terminal.json";
            case "/screens/wtlib/wireless_pattern_encoding_terminal.json" ->
                    "/screens/wtlib/nebulae_wireless_pattern_encoding_terminal.json";
            default -> path;
        };
    }
}
