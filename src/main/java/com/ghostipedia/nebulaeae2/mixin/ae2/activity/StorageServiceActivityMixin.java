package com.ghostipedia.nebulaeae2.mixin.ae2.activity;

import com.ghostipedia.nebulaeae2.activity.ActivityStorageBinding;
import com.ghostipedia.nebulaeae2.activity.IActivityService;
import appeng.me.service.StorageService;
import appeng.me.storage.NetworkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StorageService.class)
public abstract class StorageServiceActivityMixin implements ActivityStorageBinding {
    @Shadow @Final private NetworkStorage storage;

    @Override
    public void nebulae$bindActivity(IActivityService service) {
        ((ActivityStorageBinding) storage).nebulae$bindActivity(service);
    }
}
