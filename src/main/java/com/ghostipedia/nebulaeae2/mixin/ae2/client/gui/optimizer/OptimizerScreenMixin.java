package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.optimizer;

import com.ghostipedia.nebulaeae2.client.optimizer.PatternOptimizerScreen;
import com.ghostipedia.nebulaeae2.client.optimizer.OptimizerToggleButton;
import com.ghostipedia.nebulaeae2.optimizer.OptimizationHost;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.TabButton;
import appeng.core.definitions.AEItems;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.menu.AEBaseMenu;

@Mixin(AEBaseScreen.class)
public abstract class OptimizerScreenMixin extends AbstractContainerScreen<AEBaseMenu> {
    @Shadow @Final protected WidgetContainer widgets;
    @Shadow @Final protected ScreenStyle style;
    @Unique private OptimizerToggleButton nebulae$optimizerToggle;
    @Unique private TabButton nebulae$optimizerTab;

    protected OptimizerScreenMixin(AEBaseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nebulae$registerOptimizerTab(AEBaseMenu menu, Inventory inventory, Component title,
            ScreenStyle style, CallbackInfo callback) {
        Object screen = this;
        if (screen instanceof CraftConfirmScreen || screen instanceof PatternAccessTermScreen) {
            var parent = (AEBaseScreen<AEBaseMenu>) screen;
            nebulae$optimizerTab = new TabButton(AEItems.PROCESSING_PATTERN.stack(),
                    Component.translatable("gui.nebulaeae2.optimizer.title"),
                    button -> parent.switchToScreen(new PatternOptimizerScreen<>(parent)));
            nebulae$optimizerTab.setStyle(screen instanceof CraftConfirmScreen
                    ? TabButton.Style.BOX : TabButton.Style.HORIZONTAL);
            widgets.add("nebulaeOptimize", nebulae$optimizerTab);
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void nebulae$positionOptimizerControls(CallbackInfo callback) {
        if (nebulae$optimizerTab != null) {
            nebulae$optimizerTab.setY(Math.max(0, nebulae$optimizerTab.getY()));
        }
        if ((Object) this instanceof PatternProviderScreen && menu instanceof OptimizationHost host) {
            var priority = style.getWidget("openPriority").resolve(new Rect2i(leftPos, topPos, imageWidth, imageHeight));
            nebulae$optimizerToggle = addRenderableWidget(new OptimizerToggleButton(
                    button -> host.nebulae$setAllowsOptimization(!host.nebulae$allowsOptimization())));
            nebulae$optimizerToggle.setX(priority.getX());
            nebulae$optimizerToggle.setY(priority.getY() + 22);
            nebulae$optimizerToggle.setAllowed(host.nebulae$allowsOptimization());
        }
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void nebulae$refreshOptimizerToggle(CallbackInfo callback) {
        if (nebulae$optimizerToggle != null && menu instanceof OptimizationHost host) {
            nebulae$optimizerToggle.setAllowed(host.nebulae$allowsOptimization());
        }
    }
}
