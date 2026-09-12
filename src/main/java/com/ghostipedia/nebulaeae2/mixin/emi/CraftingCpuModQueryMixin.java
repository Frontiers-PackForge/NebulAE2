package com.ghostipedia.nebulaeae2.mixin.emi;

import com.ghostipedia.nebulaeae2.crafting.api.ICraftingCpuComponent;
import net.minecraft.world.item.BlockItem;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.search.ModQuery;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModQuery.class, remap = false)
public class CraftingCpuModQueryMixin {
    @Shadow
    @Final
    private String name;

    @Inject(method = {"matches", "matchesUnbaked"}, at = @At("RETURN"), cancellable = true)
    private void nebulae$includeCraftingComponents(EmiStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && ("ae2".contains(name) || "applied energistics 2".contains(name))
                && stack.getItemStack().getItem() instanceof BlockItem item
                && item.getBlock() instanceof ICraftingCpuComponent) {
            cir.setReturnValue(true);
        }
    }
}
