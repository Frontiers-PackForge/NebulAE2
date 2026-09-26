package com.ghostipedia.nebulaeae2.activity;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ActivityResponse(int menuId, int serial, CompoundTag data) implements CustomPacketPayload {
    public static final Type<ActivityResponse> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NebulaeAE2.MODID, "activity_response"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActivityResponse> CODEC = StreamCodec.of((buf, value) -> {
        buf.writeVarInt(value.menuId); buf.writeVarInt(value.serial); buf.writeNbt(value.data);
    }, buf -> new ActivityResponse(buf.readVarInt(), buf.readVarInt(), buf.readNbt()));

    @Override public Type<ActivityResponse> type() { return TYPE; }
}
