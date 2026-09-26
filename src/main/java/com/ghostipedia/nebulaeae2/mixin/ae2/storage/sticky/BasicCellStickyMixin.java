package com.ghostipedia.nebulaeae2.mixin.ae2.storage.sticky;

import com.ghostipedia.nebulaeae2.sticky.StickyCard;
import com.ghostipedia.nebulaeae2.sticky.StickyStorage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.me.cells.BasicCellInventory;
import appeng.util.prioritylist.IPartitionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BasicCellInventory.class, remap = false)
public abstract class BasicCellStickyMixin implements StickyStorage {
    @Shadow @Final private ItemStack i;
    @Shadow @Final private AEKeyType keyType;
    @Shadow @Final private IPartitionList partitionList;
    @Shadow @Final private IncludeExclude partitionListMode;
    @Unique private boolean nebulae$sticky;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void nebulae$loadSticky(CallbackInfo ci) {
        nebulae$sticky = StickyCard.installed(i);
    }

    @Override
    public boolean nebulae$claimsSticky(AEKey key) {
        return nebulae$sticky && key.getType() == keyType && !partitionList.isEmpty()
                && partitionList.matchesFilter(key, partitionListMode);
    }

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void nebulae$rejectInvalid(AEKey key, long amount, Actionable mode, IActionSource source,
                                       CallbackInfoReturnable<Long> cir) {
        if (nebulae$sticky && partitionList.isEmpty()) {
            cir.setReturnValue(0L);
        }
    }

    @Inject(method = "getDescription", at = @At("HEAD"), cancellable = true)
    private void nebulae$invalidDescription(CallbackInfoReturnable<Component> cir) {
        if (nebulae$sticky && partitionList.isEmpty()) {
            cir.setReturnValue(StickyCard.invalidMessage());
        }
    }
}
