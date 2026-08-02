package com.ghostipedia.nebulaeae2.mixin.ae2.blockentity.networking;

import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.hooks.ticking.TickHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

@Mixin(ControllerBlockEntity.class)
public abstract class ControllerBlockEntityVisualStateMixin {

    @Inject(method = "onReady", at = @At("RETURN"))
    private void nebulae$scheduleVisualStateReconciliation(CallbackInfo callback) {
        ControllerBlockEntity controller = (ControllerBlockEntity) (Object) this;
        Level level = controller.getLevel();
        if (level != null && !level.isClientSide()) {
            TickHandler.instance().addCallable(level, ignored -> nebulae$reconcileVisualState(controller, level));
        }
    }

    @Unique
    private static void nebulae$reconcileVisualState(ControllerBlockEntity controller, Level level) {
        if (controller.isRemoved()
                || controller.getLevel() != level
                || level.getBlockEntity(controller.getBlockPos()) != controller) {
            return;
        }

        controller.updateState();
        var state = level.getBlockState(controller.getBlockPos());
        level.sendBlockUpdated(controller.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
    }
}
