package com.ghostipedia.nebulaeae2.client.locating;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.ghostipedia.nebulaeae2.locating.ProviderLocatingNetworking;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = NebulaeAE2.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ProviderLocatingClientSetup {
    private ProviderLocatingClientSetup() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ProviderLocatingNetworking.setClientReceiver(ProviderHighlightClient::receive));
    }
}
