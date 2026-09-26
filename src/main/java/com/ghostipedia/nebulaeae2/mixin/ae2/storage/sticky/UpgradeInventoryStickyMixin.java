package com.ghostipedia.nebulaeae2.mixin.ae2.storage.sticky;

import java.util.function.BooleanSupplier;

import com.ghostipedia.nebulaeae2.sticky.StickyCard;
import com.ghostipedia.nebulaeae2.sticky.StickyUpgradeGuard;

import net.minecraft.world.item.ItemStack;

import appeng.util.inv.AppEngInternalInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AppEngInternalInventory.class, remap = false)
public abstract class UpgradeInventoryStickyMixin implements StickyUpgradeGuard {
    @Unique private BooleanSupplier nebulae$stickyInstallAllowed;

    @Override
    public void nebulae$setStickyInstallAllowed(BooleanSupplier allowed) {
        nebulae$stickyInstallAllowed = allowed;
    }

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    private void nebulae$requirePartition(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (nebulae$stickyInstallAllowed != null && StickyCard.isCard(stack)
                && !nebulae$stickyInstallAllowed.getAsBoolean()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setItemDirect", at = @At("HEAD"), cancellable = true)
    private void nebulae$requirePartitionOnDirectSet(int slot, ItemStack stack, CallbackInfo ci) {
        if (nebulae$stickyInstallAllowed != null && StickyCard.isCard(stack)
                && !nebulae$stickyInstallAllowed.getAsBoolean()) {
            ci.cancel();
        }
    }
}
