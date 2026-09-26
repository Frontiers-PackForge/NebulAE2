package com.ghostipedia.nebulaeae2.mixin.ae2.storage.sticky;

import com.ghostipedia.nebulaeae2.sticky.StickyCard;
import com.ghostipedia.nebulaeae2.sticky.StickyPartitionGuard;
import com.ghostipedia.nebulaeae2.sticky.StickyStorageConfiguration;
import com.ghostipedia.nebulaeae2.sticky.StickyUpgradeGuard;

import net.minecraft.network.chat.Component;

import appeng.api.config.IncludeExclude;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.core.definitions.AEItems;
import appeng.parts.storagebus.StorageBusPart;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.IPartitionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StorageBusPart.class, remap = false)
public abstract class StorageBusStickyMixin {
    @Shadow @Final private ConfigInventory config;
    @Shadow protected abstract IPartitionList createFilter();
    @Unique private IPartitionList nebulae$filter;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void nebulae$bindGuards(CallbackInfo ci) {
        var bus = (StorageBusPart) (Object) this;
        ((StickyUpgradeGuard) bus.getUpgrades()).nebulae$setStickyInstallAllowed(
                () -> !createFilter().isEmpty());
        ((StickyPartitionGuard) config).nebulae$protectStickyPartition(
                () -> bus.isUpgradedWith(StickyCard.ITEM),
                () -> 18 + bus.getInstalledUpgrades(AEItems.CAPACITY_CARD) * 9);
    }

    @Inject(method = "onConfigurationChanged", at = @At("HEAD"))
    private void nebulae$invalidateFilter(CallbackInfo ci) {
        nebulae$filter = null;
    }

    @Inject(method = "updateTarget", at = @At("RETURN"))
    private void nebulae$refreshFilter(boolean forceFullUpdate, CallbackInfo ci) {
        nebulae$filter = createFilter();
    }

    @Inject(method = "getConnectedToDescription", at = @At("HEAD"), cancellable = true)
    private void nebulae$invalidDescription(CallbackInfoReturnable<Component> cir) {
        if (nebulae$invalid()) {
            cir.setReturnValue(StickyCard.invalidMessage());
        }
    }

    @Unique
    private boolean nebulae$invalid() {
        var bus = (StorageBusPart) (Object) this;
        return bus.isUpgradedWith(StickyCard.ITEM) && nebulae$currentFilter().isEmpty();
    }

    @Unique
    private IPartitionList nebulae$currentFilter() {
        if (nebulae$filter == null) {
            nebulae$filter = createFilter();
        }
        return nebulae$filter;
    }

    @Unique
    private boolean nebulae$claims(AEKey key) {
        var bus = (StorageBusPart) (Object) this;
        var filter = nebulae$currentFilter();
        return bus.isUpgradedWith(StickyCard.ITEM) && !filter.isEmpty()
                && filter.matchesFilter(key, bus.isUpgradedWith(AEItems.INVERTER_CARD)
                        ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
    }

    @ModifyArg(method = "mountInventories", at = @At(value = "INVOKE",
            target = "Lappeng/api/storage/IStorageMounts;mount(Lappeng/api/storage/MEStorage;I)V"), index = 0)
    private MEStorage nebulae$configureMountedStorage(MEStorage storage) {
        ((StickyStorageConfiguration) storage).nebulae$configureSticky(this::nebulae$claims, this::nebulae$invalid);
        return storage;
    }
}
