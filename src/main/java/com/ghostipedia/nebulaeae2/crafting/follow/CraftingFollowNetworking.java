package com.ghostipedia.nebulaeae2.crafting.follow;

import com.ghostipedia.nebulaeae2.client.crafting.CraftingFollowClient;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class CraftingFollowNetworking {
    private CraftingFollowNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(CraftAmountFollowPayload.TYPE, CraftAmountFollowPayload.CODEC,
                (payload, context) -> payload.handle(context));
        registrar.playToServer(CraftStartFollowPayload.TYPE, CraftStartFollowPayload.CODEC,
                (payload, context) -> payload.handle(context));
        registrar.playToClient(CraftFinishedPayload.TYPE, CraftFinishedPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> CraftingFollowClient.finished(payload)));
    }
}
