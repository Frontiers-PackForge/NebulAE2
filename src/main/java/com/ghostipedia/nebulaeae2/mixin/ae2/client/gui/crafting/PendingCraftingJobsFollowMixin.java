package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.core.AEConfig;

@Mixin(PendingCraftingJobs.class)
public abstract class PendingCraftingJobsFollowMixin {
    @Redirect(method = "jobStatus", at = @At(value = "INVOKE",
            target = "Lappeng/core/AEConfig;isNotifyForFinishedCraftingJobs()Z"))
    private static boolean nebulae$suppressNativeToast(AEConfig config) {
        return false;
    }
}
