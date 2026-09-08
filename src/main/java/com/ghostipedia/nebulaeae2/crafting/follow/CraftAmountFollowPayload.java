package com.ghostipedia.nebulaeae2.crafting.follow;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;

public record CraftAmountFollowPayload(int containerId, int amount, boolean missing, boolean autoStart,
        boolean followed) implements CustomPacketPayload {
    public static final Type<CraftAmountFollowPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NebulaeAE2.MODID, "craft_amount_follow"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftAmountFollowPayload> CODEC = StreamCodec.ofMember(
            CraftAmountFollowPayload::write, CraftAmountFollowPayload::read);

    private static CraftAmountFollowPayload read(RegistryFriendlyByteBuf buffer) {
        return new CraftAmountFollowPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeVarInt(amount);
        buffer.writeBoolean(missing);
        buffer.writeBoolean(autoStart);
        buffer.writeBoolean(followed);
    }

    @Override
    public Type<CraftAmountFollowPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (amount <= 0 || !(player.containerMenu instanceof CraftAmountMenu menu)
                    || menu.containerId != containerId || !menu.stillValid(player)) {
                return;
            }
            menu.confirm(amount, missing, autoStart);
            if (player.containerMenu instanceof CraftConfirmMenu confirmation) {
                ((FollowedCraftingMenu) confirmation).nebulae$setFollowed(followed);
            }
        });
    }
}
