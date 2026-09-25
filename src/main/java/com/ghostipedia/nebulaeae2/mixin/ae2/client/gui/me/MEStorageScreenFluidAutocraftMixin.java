package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.me;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.helpers.InventoryAction;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MEStorageScreen.class)
public abstract class MEStorageScreenFluidAutocraftMixin<C extends MEStorageMenu> extends AEBaseScreen<C> {
    protected MEStorageScreenFluidAutocraftMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "handleGridInventoryEntryMouseClick", at = @At("HEAD"), cancellable = true)
    private void nebulae$openCraftAmountForEmptyFluid(GridInventoryEntry entry, int mouseButton, ClickType clickType,
            CallbackInfo callbackInfo) {
        if (entry == null
                || mouseButton != 0
                || clickType != ClickType.PICKUP
                || !menu.getCarried().isEmpty()
                || entry.getStoredAmount() != 0
                || !entry.isCraftable()
                || !ContainerItemStrategies.isKeySupported(entry.getWhat())) {
            return;
        }

        menu.handleInteraction(entry.getSerial(), InventoryAction.AUTO_CRAFT);
        callbackInfo.cancel();
    }
}
