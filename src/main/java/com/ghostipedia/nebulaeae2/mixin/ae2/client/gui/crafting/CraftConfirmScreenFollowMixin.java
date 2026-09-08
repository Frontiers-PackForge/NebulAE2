package com.ghostipedia.nebulaeae2.mixin.ae2.client.gui.crafting;

import com.ghostipedia.nebulaeae2.crafting.follow.CraftStartFollowPayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.AEConfig;
import appeng.menu.me.crafting.CraftConfirmMenu;

@Mixin(value = CraftConfirmScreen.class, priority = 900)
public abstract class CraftConfirmScreenFollowMixin extends AEBaseScreen<CraftConfirmMenu> {
    @Shadow @Final private Button start;
    @Unique private Button nebulae$follow;

    protected CraftConfirmScreenFollowMixin(CraftConfirmMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nebulae$addFollow(CraftConfirmMenu menu, Inventory inventory, Component title, ScreenStyle style,
            CallbackInfo callback) {
        nebulae$follow = widgets.addButton("nebulaeStartFollow",
                Component.translatable("gui.nebulaeae2.crafting.start_follow"), () -> nebulae$start(true));
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void nebulae$updateFollow(CallbackInfo callback) {
        nebulae$follow.visible = !AEConfig.instance().isNotifyForFinishedCraftingJobs();
        nebulae$follow.active = start.active;
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void nebulae$normalStart(CallbackInfo callback) {
        nebulae$start(AEConfig.instance().isNotifyForFinishedCraftingJobs());
        callback.cancel();
    }

    @Unique
    private void nebulae$start(boolean followed) {
        if (start.active) {
            PacketDistributor.sendToServer(new CraftStartFollowPayload(menu.containerId, followed));
        }
    }
}
