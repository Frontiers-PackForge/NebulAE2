package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import java.util.List;

import com.ghostipedia.nebulaeae2.client.crafting.StockUsagePresentation;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.AbstractTableRenderer;
import appeng.client.gui.me.crafting.CraftConfirmTableRenderer;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;

@Mixin(CraftConfirmTableRenderer.class)
public abstract class CraftConfirmTableStockMixin extends AbstractTableRenderer<CraftingPlanSummaryEntry> {
    protected CraftConfirmTableStockMixin(AEBaseScreen<?> screen, int x, int y, int rows) {
        super(screen, x, y, rows);
    }

    @Inject(method = "getEntryDescription(Lappeng/menu/me/crafting/CraftingPlanSummaryEntry;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void nebulae$stockDescription(CraftingPlanSummaryEntry entry, CallbackInfoReturnable<List<Component>> callback) {
        callback.setReturnValue(StockUsagePresentation.append(screen, entry.getWhat(), callback.getReturnValue(), false, false));
    }

    @Inject(method = "getEntryTooltip(Lappeng/menu/me/crafting/CraftingPlanSummaryEntry;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void nebulae$stockTooltip(CraftingPlanSummaryEntry entry, CallbackInfoReturnable<List<Component>> callback) {
        callback.setReturnValue(StockUsagePresentation.append(screen, entry.getWhat(), callback.getReturnValue(), false, true));
    }
}
