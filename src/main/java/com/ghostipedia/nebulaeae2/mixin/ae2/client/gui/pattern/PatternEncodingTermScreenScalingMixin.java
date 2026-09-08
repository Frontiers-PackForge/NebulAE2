package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.pattern;

import com.ghostipedia.nebulaeae2.client.pattern.PatternScalingButton;
import com.ghostipedia.nebulaeae2.pattern.PatternScalingMenu;
import com.ghostipedia.nebulaeae2.pattern.ProcessingPatternScaling;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;

@Mixin(PatternEncodingTermScreen.class)
public abstract class PatternEncodingTermScreenScalingMixin<C extends PatternEncodingTermMenu>
        extends MEStorageScreen<C> {
    @Unique private PatternScalingButton nebulae$multiply;
    @Unique private PatternScalingButton nebulae$divide;

    protected PatternEncodingTermScreenScalingMixin(C menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nebulae$addScaling(C menu, Inventory inventory, Component title, ScreenStyle style,
            CallbackInfo callback) {
        nebulae$multiply = new PatternScalingButton(true, button -> nebulae$scale(1));
        nebulae$divide = new PatternScalingButton(false, button -> nebulae$scale(-1));
        widgets.add("nebulaeMultiplyPattern", nebulae$multiply);
        widgets.add("nebulaeDividePattern", nebulae$divide);
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void nebulae$updateScaling(CallbackInfo callback) {
        boolean processing = menu.getMode() == EncodingMode.PROCESSING;
        nebulae$multiply.setVisibility(processing);
        nebulae$divide.setVisibility(processing);
    }

    @Unique
    private void nebulae$scale(int direction) {
        ((PatternScalingMenu) menu).nebulae$scalePattern(
                direction * ProcessingPatternScaling.factor(hasShiftDown(), hasControlDown()));
    }
}
