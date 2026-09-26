package com.ghostipedia.nebulaeae2.mixin.ae2.menu.optimizer;

import com.ghostipedia.nebulaeae2.optimizer.OptimizationHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.PatternProviderMenu;

@Mixin(PatternProviderMenu.class)
public abstract class PatternProviderMenuOptimizerMixin extends AEBaseMenu implements OptimizationHost {
    @Shadow @Final protected PatternProviderLogic logic;
    @Unique @GuiSync(751) public boolean nebulae$optimizationAllowed = true;

    protected PatternProviderMenuOptimizerMixin(MenuType<?> type, int id, Inventory inventory, Object host) {
        super(type, id, inventory, host);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nebulae$registerOptimizerToggle(CallbackInfo callback) {
        registerClientAction("nebulaeAllowOptimization", Boolean.class, allowed -> {
            if (allowed != null) {
                nebulae$setAllowsOptimization(allowed);
            }
        });
    }

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void nebulae$syncOptimizerToggle(CallbackInfo callback) {
        if (isServerSide()) {
            nebulae$optimizationAllowed = ((OptimizationHost) logic).nebulae$allowsOptimization();
        }
    }

    @Override
    public boolean nebulae$allowsOptimization() {
        return nebulae$optimizationAllowed;
    }

    @Override
    public void nebulae$setAllowsOptimization(boolean allowed) {
        if (isClientSide()) {
            sendClientAction("nebulaeAllowOptimization", allowed);
        } else if (getPlayer().containerMenu == this && stillValid(getPlayer())) {
            ((OptimizationHost) logic).nebulae$setAllowsOptimization(allowed);
        }
    }
}
