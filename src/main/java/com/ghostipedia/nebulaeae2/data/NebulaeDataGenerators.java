package com.ghostipedia.nebulaeae2.data;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = NebulaeAE2.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class NebulaeDataGenerators {
    private NebulaeDataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeClient() && event.getMods().contains(NebulaeAE2.MODID)) {
            event.getGenerator().addProvider(true,
                    new NebulaeLanguageProvider(event.getGenerator().getPackOutput()));
        }
    }
}
