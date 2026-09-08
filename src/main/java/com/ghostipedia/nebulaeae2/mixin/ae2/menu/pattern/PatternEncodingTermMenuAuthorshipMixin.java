package com.ghostipedia.nebulaeae2.mixin.ae2.menu.pattern;

import com.ghostipedia.nebulaeae2.pattern.PatternAuthor;
import com.ghostipedia.nebulaeae2.pattern.PatternAuthorship;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import appeng.menu.me.items.PatternEncodingTermMenu;

@Mixin(PatternEncodingTermMenu.class)
public abstract class PatternEncodingTermMenuAuthorshipMixin {
    @ModifyArg(method = "encode", at = @At(value = "INVOKE", target = "Lappeng/menu/slot/RestrictedInputSlot;set(Lnet/minecraft/world/item/ItemStack;)V", ordinal = 1), index = 0)
    private ItemStack nebulae$recordEncoder(ItemStack output) {
        PatternEncodingTermMenu menu = (PatternEncodingTermMenu) (Object) this;
        output.set(PatternAuthorship.AUTHOR,
                new PatternAuthor(menu.getPlayer().getUUID(), menu.getPlayer().getGameProfile().getName()));
        return output;
    }
}
