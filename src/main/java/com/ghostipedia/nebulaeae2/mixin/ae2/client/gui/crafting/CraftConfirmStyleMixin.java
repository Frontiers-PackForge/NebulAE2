package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import appeng.client.gui.style.StyleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StyleManager.class)
public abstract class CraftConfirmStyleMixin {
    @ModifyVariable(method = "loadStyleDoc", at = @At("HEAD"), argsOnly = true)
    private static String nebulae$useFollowControls(String path) {
        return "/screens/craft_confirm.json".equals(path) ? "/screens/nebulae_craft_confirm.json" : path;
    }
}
