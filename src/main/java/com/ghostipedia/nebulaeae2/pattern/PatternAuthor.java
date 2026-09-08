package com.ghostipedia.nebulaeae2.pattern;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PatternAuthor(UUID id, String lastKnownName) {
    public static final Codec<PatternAuthor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(PatternAuthor::id),
            Codec.STRING.fieldOf("name").forGetter(PatternAuthor::lastKnownName))
            .apply(instance, PatternAuthor::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PatternAuthor> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PatternAuthor::id,
            ByteBufCodecs.STRING_UTF8, PatternAuthor::lastKnownName,
            PatternAuthor::new);
}
