package com.ghostipedia.nebulaeae2.locating;

import java.util.function.Consumer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ProviderLocatingNetworking {
    private static Consumer<ProviderLocations> clientReceiver = payload -> {};

    private ProviderLocatingNetworking() {}

    public static void setClientReceiver(Consumer<ProviderLocations> receiver) {
        clientReceiver = receiver;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("nebulae_provider_locating_1");
        registrar.playToServer(LocateProviderRequest.TYPE, LocateProviderRequest.STREAM_CODEC, ProviderLocator::handle);
        registrar.playToClient(ProviderLocations.TYPE, ProviderLocations.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> clientReceiver.accept(payload)));
    }
}
