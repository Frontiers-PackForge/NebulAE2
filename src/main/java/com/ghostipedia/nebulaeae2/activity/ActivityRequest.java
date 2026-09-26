package com.ghostipedia.nebulaeae2.activity;

import java.util.UUID;
import com.ghostipedia.nebulaeae2.NebulaeAE2;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ActivityRequest(int menuId, int serial, int tab, int page, int window, String search,
        String status, boolean mine, UUID selected, int detailPage, String sort, boolean reverse) implements CustomPacketPayload {
    public static final Type<ActivityRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NebulaeAE2.MODID, "activity_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActivityRequest> CODEC = StreamCodec.of((buf, value) -> {
        buf.writeVarInt(value.menuId); buf.writeVarInt(value.serial); buf.writeVarInt(value.tab);
        buf.writeVarInt(value.page); buf.writeVarInt(value.window); buf.writeUtf(value.search, 128);
        buf.writeUtf(value.status, 16); buf.writeBoolean(value.mine);
        buf.writeBoolean(value.selected != null);
        if (value.selected != null) buf.writeUUID(value.selected);
        buf.writeVarInt(value.detailPage);
        buf.writeUtf(value.sort, 16); buf.writeBoolean(value.reverse);
    }, buf -> new ActivityRequest(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
            buf.readVarInt(), buf.readUtf(128), buf.readUtf(16), buf.readBoolean(),
            buf.readBoolean() ? buf.readUUID() : null, buf.readVarInt(), buf.readUtf(16), buf.readBoolean()));

    @Override public Type<ActivityRequest> type() { return TYPE; }
}
