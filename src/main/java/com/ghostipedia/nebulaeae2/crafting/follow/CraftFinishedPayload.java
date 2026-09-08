package com.ghostipedia.nebulaeae2.crafting.follow;

import java.util.UUID;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import appeng.api.stacks.GenericStack;

public record CraftFinishedPayload(UUID jobId, GenericStack output, long elapsedNanos, boolean followed)
        implements CustomPacketPayload {
    public static final Type<CraftFinishedPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NebulaeAE2.MODID, "craft_finished"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftFinishedPayload> CODEC = StreamCodec.ofMember(
            CraftFinishedPayload::write, CraftFinishedPayload::read);

    private static CraftFinishedPayload read(RegistryFriendlyByteBuf buffer) {
        return new CraftFinishedPayload(buffer.readUUID(), GenericStack.readBuffer(buffer), buffer.readVarLong(),
                buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(jobId);
        GenericStack.writeBuffer(output, buffer);
        buffer.writeVarLong(elapsedNanos);
        buffer.writeBoolean(followed);
    }

    @Override
    public Type<CraftFinishedPayload> type() {
        return TYPE;
    }
}
