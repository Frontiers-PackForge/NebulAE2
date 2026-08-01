package com.ghostipedia.nebulaeae2.mixin.gtceu.client.bloom;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BloomShaderManager.class, remap = false)
public abstract class BloomShaderManagerAlphaCutoutMixin {

    @ModifyExpressionValue(
            method = "onRegisterShaders(Lnet/neoforged/neoforge/client/event/RegisterShadersEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/GTCEu;id(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;",
                    ordinal = 0),
            require = 1)
    private static ResourceLocation nebulae$useAlphaCutoutBlockBloomShader(ResourceLocation original) {
        return ResourceLocation.fromNamespaceAndPath(NebulaeAE2.MODID, "rendertype_bloom");
    }
}
