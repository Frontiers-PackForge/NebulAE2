package com.ghostipedia.nebulaeae2.client.crafting;

import com.ghostipedia.nebulaeae2.crafting.follow.CraftFinishedPayload;
import com.ghostipedia.nebulaeae2.crafting.follow.CraftingFollowPolicy;
import com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting.PendingCraftingJobsFollowAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AmountFormat;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.core.AEConfig;

public final class CraftingFollowClient {
    private CraftingFollowClient() {}

    public static void finished(CraftFinishedPayload payload) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || payload.output() == null) {
            return;
        }
        if (!CraftingFollowPolicy.shouldNotify(AEConfig.instance().isNotifyForFinishedCraftingJobs(),
                payload.followed(), false,
                PendingCraftingJobsFollowAccessor.nebulae$hasNotificationItem(minecraft.player))) {
            return;
        }
        var output = payload.output();
        var message = Component.translatable("gui.nebulaeae2.crafting.finished",
                output.what().formatAmount(output.amount(), AmountFormat.FULL),
                AEKeyRendering.getDisplayName(output.what()), CraftingFollowPolicy.elapsed(payload.elapsedNanos()));
        if (!(minecraft.screen instanceof MEStorageScreen<?>)) {
            minecraft.getToasts().addToast(new FollowedCraftingToast(message));
        }
        minecraft.player.sendSystemMessage(message);
    }
}
