package com.ghostipedia.nebulaeae2.mixin.ae2.crafting.pattern;

import com.ghostipedia.nebulaeae2.pattern.PatternAuthorship;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;

@Mixin(PatternDetailsHelper.class)
public abstract class PatternDetailsHelperAuthorshipMixin {
    @ModifyVariable(method = "decodePattern(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Lappeng/api/crafting/IPatternDetails;", at = @At("HEAD"), argsOnly = true)
    private static ItemStack nebulae$decodeContent(ItemStack stack) {
        return PatternAuthorship.contentCopy(stack);
    }

    @ModifyVariable(method = "decodePattern(Lappeng/api/stacks/AEItemKey;Lnet/minecraft/world/level/Level;)Lappeng/api/crafting/IPatternDetails;", at = @At("HEAD"), argsOnly = true)
    private static AEItemKey nebulae$decodeContentKey(AEItemKey key) {
        return PatternAuthorship.contentKey(key);
    }
}
