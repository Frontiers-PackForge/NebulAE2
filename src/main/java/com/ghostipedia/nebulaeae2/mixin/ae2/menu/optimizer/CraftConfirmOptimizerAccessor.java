package com.ghostipedia.nebulaeae2.mixin.ae2.menu.optimizer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEKey;
import appeng.menu.me.crafting.CraftConfirmMenu;

@Mixin(CraftConfirmMenu.class)
public interface CraftConfirmOptimizerAccessor {
    @Accessor("result")
    ICraftingPlan nebulae$optimizerPlan();

    @Accessor("whatToCraft")
    AEKey nebulae$optimizerRequestedKey();

    @Accessor("amount")
    int nebulae$optimizerRequestedAmount();
}
