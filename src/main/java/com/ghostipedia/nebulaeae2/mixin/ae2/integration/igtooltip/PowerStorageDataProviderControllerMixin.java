package com.ghostipedia.nebulaeae2.mixin.ae2.integration.igtooltip;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.integration.modules.igtooltip.blocks.PowerStorageDataProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PowerStorageDataProvider.class)
public abstract class PowerStorageDataProviderControllerMixin {

    @Inject(
            method = "provideServerData(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void nebulae$suppressControllerPowerData(
            Player player, BlockEntity blockEntity, CompoundTag serverData, CallbackInfo callback) {
        if (blockEntity instanceof ControllerBlockEntity) {
            callback.cancel();
        }
    }

    @Inject(
            method = "buildTooltip(Lnet/minecraft/world/level/block/entity/BlockEntity;Lappeng/api/integrations/igtooltip/TooltipContext;Lappeng/api/integrations/igtooltip/TooltipBuilder;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void nebulae$suppressControllerPowerTooltip(
            BlockEntity blockEntity, TooltipContext context, TooltipBuilder tooltip, CallbackInfo callback) {
        if (blockEntity instanceof ControllerBlockEntity) {
            callback.cancel();
        }
    }
}
