package com.ghostipedia.nebulaeae2.mixin.gtceu.client.bloom;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "com.gregtechceu.gtceu.integration.sodium.GTSodiumCompat", remap = false)
public abstract class GTSodiumCompatAlphaCutoutMixin {

    @ModifyArg(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;<init>(Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/parameters/AlphaCutoffParameter;Z)V"),
            index = 1,
            require = 1)
    private static AlphaCutoffParameter nebulae$useBloomAlphaCutoff(AlphaCutoffParameter original) {
        return AlphaCutoffParameter.ONE_TENTH;
    }
}
