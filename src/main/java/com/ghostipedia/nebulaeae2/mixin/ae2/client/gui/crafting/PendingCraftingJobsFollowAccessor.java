package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import appeng.client.gui.me.common.PendingCraftingJobs;

@Mixin(PendingCraftingJobs.class)
public interface PendingCraftingJobsFollowAccessor {
    @Invoker("hasNotificationEnablingItem")
    static boolean nebulae$hasNotificationItem(LocalPlayer player) {
        throw new AssertionError();
    }
}
