package com.ghostipedia.nebulaeae2.locating;

import java.util.ArrayList;
import java.util.List;
import com.ghostipedia.nebulaeae2.NebulaeAE2;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ProviderLocations(int menuId, ResourceLocation dimension, List<BlockPos> positions) implements CustomPacketPayload {
    public static final int MAX_POSITIONS = 64;
    public static final Type<ProviderLocations> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NebulaeAE2.MODID, "provider_locations"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProviderLocations> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.menuId);
                buffer.writeResourceLocation(payload.dimension);
                buffer.writeVarInt(payload.positions.size());
                for (var pos : payload.positions) {
                    buffer.writeBlockPos(pos);
                }
            }, buffer -> {
                int menu = buffer.readVarInt();
                var dimension = buffer.readResourceLocation();
                int count = buffer.readVarInt();
                if (count < 0 || count > MAX_POSITIONS) {
                    throw new IllegalArgumentException("Invalid provider count");
                }
                var positions = new ArrayList<BlockPos>(count);
                for (int i = 0; i < count; i++) {
                    positions.add(buffer.readBlockPos());
                }
                return new ProviderLocations(menu, dimension, positions);
            });

    public ProviderLocations {
        positions = List.copyOf(positions);
        if (positions.size() > MAX_POSITIONS) {
            throw new IllegalArgumentException("Too many providers");
        }
    }

    @Override
    public Type<ProviderLocations> type() {
        return TYPE;
    }
}
