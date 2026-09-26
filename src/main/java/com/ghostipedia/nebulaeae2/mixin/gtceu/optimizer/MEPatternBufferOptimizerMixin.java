package com.ghostipedia.nebulaeae2.mixin.gtceu.optimizer;

import com.ghostipedia.nebulaeae2.optimizer.OptimizationHost;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.core.definitions.AEItems;

@Mixin(MEPatternBufferPartMachine.class)
public abstract class MEPatternBufferOptimizerMixin implements OptimizationHost {
    @Unique @SaveField(nbtKey = "nebulaeOptimizationDisabled") private boolean nebulae$optimizationDisabled;

    @Override
    public boolean nebulae$allowsOptimization() {
        return !nebulae$optimizationDisabled;
    }

    @Override
    public void nebulae$setAllowsOptimization(boolean allowed) {
        nebulae$optimizationDisabled = !allowed;
        ((MEPatternBufferPartMachine) (Object) this).setChanged();
    }

    @Inject(method = "getPanelBuilder", at = @At("RETURN"), remap = false)
    private void nebulae$optimizerToggle(PosGuiData guiData, PanelSyncManager syncManager,
            UISettings settings, CallbackInfoReturnable<MachineUIPanelBuilder> callback) {
        var allowed = new BooleanSyncValue(this::nebulae$allowsOptimization, this::nebulae$setAllowsOptimization);
        syncManager.syncValue("nebulae_optimization_allowed", allowed);
        var pattern = new ItemDrawable(AEItems.PROCESSING_PATTERN.stack());
        var enabledIcon = pattern.asIcon().size(16);
        var disabledIcon = new DrawableStack(pattern, new ItemDrawable(Items.BARRIER)).asIcon().size(16);
        callback.getReturnValue().rightConfigurators(flow -> flow.child(new ButtonWidget<>().size(18)
                .overlay(new DynamicDrawable(() -> allowed.getBoolValue() ? enabledIcon : disabledIcon))
                .onMousePressed((context, button) -> {
                    if (button == 0) {
                        allowed.setBoolValue(!allowed.getBoolValue());
                        return true;
                    }
                    return false;
                })
                .tooltipAutoUpdate(true)
                .tooltipDynamic(tooltip -> tooltip
                        .addLine(Component.translatable(allowed.getBoolValue()
                                ? "gui.nebulaeae2.optimizer.enabled" : "gui.nebulaeae2.optimizer.disabled"))
                        .addLine(Component.translatable("gui.nebulaeae2.optimizer.toggle_tooltip")))));
    }
}
