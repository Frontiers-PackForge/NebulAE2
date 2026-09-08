package com.ghostipedia.nebulaeae2.mixin.ae2.client.integration.emi;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;

@Mixin(targets = "appeng.integration.modules.emi.EmiAeBaseScreenStackProvider")
public abstract class EmiCraftingFavoritesMixin {
    @WrapOperation(method = "getStackAt", at = @At(value = "INVOKE",
            target = "Lappeng/integration/modules/emi/EmiStackHelper;toEmiStack(Lappeng/api/stacks/GenericStack;)Ldev/emi/emi/api/stack/EmiStack;"))
    private EmiStack nebulae$normalizeCraftingHover(GenericStack stack, Operation<EmiStack> original,
            @Local(argsOnly = true) Screen screen) {
        if (stack.amount() == 0 && (screen instanceof CraftConfirmScreen || screen instanceof CraftingCPUScreen<?>)) {
            stack = new GenericStack(stack.what(), 1);
        }
        return original.call(stack);
    }
}
