package com.ghostipedia.nebulaeae2.mixin.ae2.crafting.pattern;

import java.util.List;

import com.ghostipedia.nebulaeae2.pattern.PatternAuthorship;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.stacks.AEItemKey;
import appeng.crafting.pattern.EncodedPatternItem;

@Mixin(EncodedPatternItem.class)
public abstract class EncodedPatternItemAuthorshipMixin {
    @ModifyArg(method = {"decode(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;)Lappeng/api/crafting/IPatternDetails;",
            "decode(Lappeng/api/stacks/AEItemKey;Lnet/minecraft/world/level/Level;)Lappeng/api/crafting/IPatternDetails;",
            "appendHoverText"},
            at = @At(value = "INVOKE", target = "Lappeng/api/crafting/EncodedPatternDecoder;decode(Lappeng/api/stacks/AEItemKey;Lnet/minecraft/world/level/Level;)Lappeng/api/crafting/IPatternDetails;"), index = 0)
    private AEItemKey nebulae$decodeContent(AEItemKey key) {
        return PatternAuthorship.contentKey(key);
    }

    @Inject(method = "appendHoverText", at = @At("RETURN"))
    private void nebulae$appendAuthor(ItemStack stack, Item.TooltipContext context, List<Component> lines,
            TooltipFlag flags, CallbackInfo callback) {
        var author = stack.get(PatternAuthorship.AUTHOR);
        if (author != null) {
            lines.add(Component.translatable("tooltip.nebulaeae2.pattern_author", author.lastKnownName())
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
