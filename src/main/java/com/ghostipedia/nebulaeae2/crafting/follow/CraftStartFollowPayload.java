package com.ghostipedia.nebulaeae2.crafting.follow;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import appeng.menu.me.crafting.CraftConfirmMenu;

public record CraftStartFollowPayload(int containerId, boolean followed) implements CustomPacketPayload {
    public static final Type<CraftStartFollowPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NebulaeAE2.MODID, "craft_start_follow"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftStartFollowPayload> CODEC = StreamCodec.ofMember(
            CraftStartFollowPayload::write, buffer -> new CraftStartFollowPayload(buffer.readVarInt(), buffer.readBoolean()));

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeBoolean(followed);
    }

    @Override
    public Type<CraftStartFollowPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (!(player.containerMenu instanceof CraftConfirmMenu menu)
                    || menu.containerId != containerId || !menu.stillValid(player)) {
                return;
            }
            ((FollowedCraftingMenu) menu).nebulae$setFollowed(followed);
            menu.startJob();
        });
    }
}
