package com.ghostipedia.nebulaeae2.mixin.ae2.menu.pattern;

import com.ghostipedia.nebulaeae2.pattern.PatternScalingMenu;
import com.ghostipedia.nebulaeae2.pattern.ProcessingPatternScaling;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.stacks.GenericStack;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.ConfigInventory;

@Mixin(PatternEncodingTermMenu.class)
public abstract class PatternEncodingTermMenuScalingMixin extends MEStorageMenu implements PatternScalingMenu {
    @Shadow @Final private PatternEncodingLogic encodingLogic;
    @Shadow @Final private ConfigInventory encodedInputsInv;
    @Shadow @Final private ConfigInventory encodedOutputsInv;

    protected PatternEncodingTermMenuScalingMixin(MenuType<?> type, int id, Inventory inventory,
            IPatternTerminalMenuHost host, boolean bindInventory) {
        super(type, id, inventory, host, bindInventory);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V", at = @At("TAIL"))
    private void nebulae$registerScaling(MenuType<?> type, int id, Inventory inventory,
            IPatternTerminalMenuHost host, boolean bindInventory, CallbackInfo callback) {
        registerClientAction("nebulaeScalePattern", Integer.class, operation -> {
            if (operation != null) {
                nebulae$scalePattern(operation);
            }
        });
    }

    @Override
    public void nebulae$scalePattern(int operation) {
        if (isClientSide()) {
            sendClientAction("nebulaeScalePattern", operation);
            return;
        }
        if (getPlayer().containerMenu != this || !stillValid(getPlayer())
                || encodingLogic.getMode() != EncodingMode.PROCESSING) {
            return;
        }
        GenericStack[] inputs;
        GenericStack[] outputs;
        try {
            ProcessingPatternScaling.scale(8, operation, Long.MAX_VALUE);
            inputs = nebulae$scaled(encodedInputsInv, operation);
            outputs = nebulae$scaled(encodedOutputsInv, operation);
        } catch (IllegalArgumentException exception) {
            getPlayer().displayClientMessage(Component.translatable(
                    "gui.nebulaeae2.pattern_scaling." + exception.getMessage()), true);
            return;
        }
        encodedInputsInv.beginBatch();
        encodedOutputsInv.beginBatch();
        try {
            for (int i = 0; i < inputs.length; i++) {
                encodedInputsInv.setStack(i, inputs[i]);
            }
            for (int i = 0; i < outputs.length; i++) {
                encodedOutputsInv.setStack(i, outputs[i]);
            }
        } finally {
            encodedInputsInv.endBatch();
            encodedOutputsInv.endBatch();
        }
        broadcastChanges();
    }

    @Unique
    private static GenericStack[] nebulae$scaled(ConfigInventory inventory, int operation) {
        var result = new GenericStack[inventory.size()];
        for (int i = 0; i < result.length; i++) {
            var stack = inventory.getStack(i);
            if (stack != null) {
                long maximum = Math.min(inventory.getMaxAmount(stack.what()),
                        999999L * stack.what().getAmountPerUnit());
                result[i] = new GenericStack(stack.what(),
                        ProcessingPatternScaling.scale(stack.amount(), operation, maximum));
            }
        }
        return result;
    }
}
