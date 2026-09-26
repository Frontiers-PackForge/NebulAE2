package com.ghostipedia.nebulaeae2.activity;

import appeng.api.networking.GridServices;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class ActivityRegistration {
    private ActivityRegistration() {}

    public static void init(IEventBus bus) {
        GridServices.register(IActivityService.class, ActivityGridService.class);
        bus.addListener(ActivityNetworking::register);
        NeoForge.EVENT_BUS.addListener(ActivityRegistration::tick);
    }

    private static void tick(ServerTickEvent.Post event) {
        ActivityArchive.get(event.getServer()).tick(System.nanoTime(), System.currentTimeMillis());
    }
}
