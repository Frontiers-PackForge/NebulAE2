package com.ghostipedia.nebulaeae2.p2p;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public record PatternTunnelAddress(ResourceKey<Level> dimension, BlockPos position, Direction side) {
    public PatternTunnelAddress {
        position = position.immutable();
    }

    public ServerLevel level(MinecraftServer server) {
        return server.getLevel(dimension);
    }

    public boolean loaded(MinecraftServer server) {
        var level = level(server);
        return level != null && level.hasChunkAt(position);
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putString("dimension", dimension.location().toString());
        tag.putLong("position", position.asLong());
        tag.putInt("side", side == null ? -1 : side.get3DDataValue());
        return tag;
    }

    public static PatternTunnelAddress load(CompoundTag tag) {
        var dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension")));
        int side = tag.getInt("side");
        return new PatternTunnelAddress(dimension, BlockPos.of(tag.getLong("position")),
                side < 0 ? null : Direction.from3DDataValue(side));
    }
}
