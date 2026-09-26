package com.ghostipedia.nebulaeae2.mixin.ae2.storage.sticky;

import com.ghostipedia.nebulaeae2.sticky.StickyCard;
import com.ghostipedia.nebulaeae2.sticky.StickyUpgradeGuard;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "appeng.api.upgrades.ItemUpgradeInventory", remap = false)
public abstract class ItemUpgradeStickyMixin {
    @Shadow @Final private ItemStack stack;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void nebulae$bindCellGuard(CallbackInfo ci) {
        ((StickyUpgradeGuard) this).nebulae$setStickyInstallAllowed(() -> StickyCard.partitioned(stack));
    }
}
