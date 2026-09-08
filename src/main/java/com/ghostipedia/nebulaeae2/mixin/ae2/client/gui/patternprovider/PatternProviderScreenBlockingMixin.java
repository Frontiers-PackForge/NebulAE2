package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.patternprovider;

import com.ghostipedia.nebulaeae2.blocking.BlockingModeMenu;
import com.ghostipedia.nebulaeae2.client.blocking.BlockingModeButton;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PatternProviderScreen.class)
public abstract class PatternProviderScreenBlockingMixin extends AEBaseScreen<PatternProviderMenu> {
    protected PatternProviderScreenBlockingMixin(PatternProviderMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lappeng/client/gui/implementations/PatternProviderScreen;addToLeftToolbar(Lnet/minecraft/client/gui/components/Button;)Lnet/minecraft/client/gui/components/Button;", ordinal = 0), index = 0)
    private Button nebulae$replaceBlockingButton(Button original) {
        return new BlockingModeButton((BlockingModeMenu) menu, this::isHandlingRightClick);
    }
}
