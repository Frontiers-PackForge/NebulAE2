package com.ghostipedia.nebulaeae2.mixin.ae2.crafting.follow;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.ExecutingCraftingJob;

@Mixin(ExecutingCraftingJob.class)
public interface ExecutingCraftingJobFollowAccessor {
    @Accessor("playerId")
    Integer nebulae$playerId();

    @Accessor("finalOutput")
    GenericStack nebulae$finalOutput();

    @Accessor("link")
    CraftingLink nebulae$link();
}
