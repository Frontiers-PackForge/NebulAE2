package com.ghostipedia.nebulaeae2.mixin.ae2.storage.sticky;

import com.ghostipedia.nebulaeae2.sticky.StickyCard;
import com.ghostipedia.nebulaeae2.sticky.StickyPartitionGuard;

import net.minecraft.world.item.ItemStack;

import appeng.util.ConfigInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "appeng.items.contents.CellConfig$Holder", remap = false)
public abstract class CellConfigStickyMixin {
    @Shadow @Final private ItemStack stack;
    @Shadow private ConfigInventory inv;

    @Inject(method = "load", at = @At("RETURN"))
    private void nebulae$bindPartitionGuard(CallbackInfo ci) {
        ((StickyPartitionGuard) inv).nebulae$protectStickyPartition(() -> StickyCard.installed(stack));
    }
}
