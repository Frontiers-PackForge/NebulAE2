package com.ghostipedia.nebulaeae2.client.crafting;

import com.ghostipedia.nebulaeae2.crafting.follow.CraftFinishedPayload;
import com.ghostipedia.nebulaeae2.crafting.follow.CraftingFollowPolicy;
import com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting.PendingCraftingJobsFollowAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AmountFormat;
import appeng.client.gui.me.common.FinishedJobToast;
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
        var amount = Component.literal(output.what().formatAmount(output.amount(), AmountFormat.SLOT))
                .withStyle(ChatFormatting.GREEN)
                .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.literal(Long.toString(output.amount())).withStyle(ChatFormatting.GREEN))));
        var item = AEKeyRendering.getDisplayName(output.what()).copy().withStyle(ChatFormatting.AQUA);
        var duration = Component.literal(CraftingFollowPolicy.elapsed(payload.elapsedNanos()))
                .withStyle(ChatFormatting.GREEN);
        var message = Component.translatable("gui.nebulaeae2.crafting.finished",
                amount, item, duration);
        if (!(minecraft.screen instanceof MEStorageScreen<?>)) {
            minecraft.getToasts().addToast(new FinishedJobToast(output.what(), output.amount()));
        }
        minecraft.player.sendSystemMessage(message);
    }
}
