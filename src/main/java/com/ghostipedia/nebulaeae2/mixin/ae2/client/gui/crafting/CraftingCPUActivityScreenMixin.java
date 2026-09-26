package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import com.ghostipedia.nebulaeae2.client.activity.ActivityScreen;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.crafting.CraftingCPUMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingCPUScreen.class)
public abstract class CraftingCPUActivityScreenMixin extends AEBaseScreen<CraftingCPUMenu> {
    protected CraftingCPUActivityScreenMixin(CraftingCPUMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nebulae$history(CraftingCPUMenu menu, Inventory inventory, Component title, ScreenStyle style, CallbackInfo ci) {
        widgets.addButton("nebulaeHistory", Component.translatable("gui.nebulaeae2.activity.history"),
                () -> ActivityScreen.open(this));
    }
}
