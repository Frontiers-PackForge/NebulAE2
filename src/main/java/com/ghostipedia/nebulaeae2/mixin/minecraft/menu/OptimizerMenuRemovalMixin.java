package com.ghostipedia.nebulaeae2.mixin.minecraft.menu;

import com.ghostipedia.nebulaeae2.optimizer.OptimizerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class OptimizerMenuRemovalMixin {
    @Inject(method = "removed", at = @At("HEAD"))
    private void nebulae$cancelOptimizer(Player player, CallbackInfo callback) {
        if (this instanceof OptimizerMenu optimizer) {
            optimizer.nebulae$closeOptimizer();
        }
    }
}
