package com.ghostipedia.nebulaeae2.optimizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import appeng.crafting.pattern.EncodedProcessingPattern;

public record OptimizationProvenance(long multiplier, EncodedProcessingPattern expected) {
    public static final Codec<OptimizationProvenance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.validate(value -> value >= 2 ? DataResult.success(value)
                    : DataResult.error(() -> "Optimizer multiplier must be at least two"))
                    .fieldOf("multiplier").forGetter(OptimizationProvenance::multiplier),
            EncodedProcessingPattern.CODEC.fieldOf("expected").forGetter(OptimizationProvenance::expected))
            .apply(instance, OptimizationProvenance::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, OptimizationProvenance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, OptimizationProvenance::multiplier,
            EncodedProcessingPattern.STREAM_CODEC, OptimizationProvenance::expected,
            OptimizationProvenance::new);
}
