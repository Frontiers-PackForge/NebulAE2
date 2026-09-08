package com.ghostipedia.nebulaeae2.locating;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import appeng.api.stacks.AEKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LocateProviderRequest(int menuId, AEKey key) implements CustomPacketPayload {
    public static final Type<LocateProviderRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NebulaeAE2.MODID, "locate_provider"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LocateProviderRequest> STREAM_CODEC = StreamCodec.of(
            (buffer, request) -> {
                buffer.writeVarInt(request.menuId);
                AEKey.STREAM_CODEC.encode(buffer, request.key);
            }, buffer -> new LocateProviderRequest(buffer.readVarInt(), AEKey.STREAM_CODEC.decode(buffer)));

    @Override
    public Type<LocateProviderRequest> type() {
        return TYPE;
    }
}
