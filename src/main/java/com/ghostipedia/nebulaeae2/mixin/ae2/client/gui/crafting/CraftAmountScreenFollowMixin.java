package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import com.ghostipedia.nebulaeae2.crafting.follow.CraftAmountFollowPayload;
import com.ghostipedia.nebulaeae2.crafting.follow.CraftingFollowPolicy;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.client.gui.me.crafting.CraftAmountScreen;
import appeng.core.AEConfig;
import appeng.menu.me.crafting.CraftAmountMenu;

@Mixin(CraftAmountScreen.class)
public abstract class CraftAmountScreenFollowMixin {
    @Redirect(method = "confirm", at = @At(value = "INVOKE",
            target = "Lappeng/menu/me/crafting/CraftAmountMenu;confirm(IZZ)V"))
    private void nebulae$confirmFollow(CraftAmountMenu menu, int amount, boolean missing, boolean autoStart) {
        boolean followed = CraftingFollowPolicy.followFromAmount(
                AEConfig.instance().isNotifyForFinishedCraftingJobs(), Screen.hasControlDown());
        PacketDistributor.sendToServer(new CraftAmountFollowPayload(menu.containerId, amount, missing, autoStart, followed));
    }
}
