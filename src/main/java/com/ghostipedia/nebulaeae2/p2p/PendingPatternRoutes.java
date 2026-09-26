package com.ghostipedia.nebulaeae2.p2p;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class PendingPatternRoutes extends SavedData {
    private static final Factory<PendingPatternRoutes> FACTORY = new Factory<>(PendingPatternRoutes::new, PendingPatternRoutes::load, null);
    private final Map<UUID, PatternProviderRoute> routes = new HashMap<>();

    public static PendingPatternRoutes get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, "nebulae_pattern_p2p_routes");
    }

    public void claim(PatternProviderRoute route) {
        if (!route.equals(routes.put(route.owner(), route))) {
            setDirty();
        }
    }

    public void release(UUID owner) {
        if (routes.remove(owner) != null) {
            setDirty();
        }
    }

    public boolean occupied(PatternTunnelAddress destination, UUID owner, MinecraftServer server) {
        var iterator = routes.values().iterator();
        while (iterator.hasNext()) {
            var route = iterator.next();
            if (route.owner().equals(owner)) {
                continue;
            }
            if (route.provider().loaded(server)) {
                var provider = PatternTunnelRouting.providerAt(route.provider(), server);
                if (provider == null || !(provider.getLogic() instanceof PatternProviderRouteHost host)
                        || !route.equals(host.nebulae$pendingRoute())) {
                    iterator.remove();
                    setDirty();
                    continue;
                }
            }
            if (PatternTunnelRouting.sameDestination(destination, route.destination(), server)) {
                return true;
            }
        }
        return false;
    }

    private static PendingPatternRoutes load(CompoundTag tag, HolderLookup.Provider registries) {
        var result = new PendingPatternRoutes();
        var entries = tag.getList("routes", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            var route = PatternProviderRoute.load(entries.getCompound(index));
            result.routes.put(route.owner(), route);
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var entries = new ListTag();
        for (var route : routes.values()) {
            entries.add(route.save());
        }
        tag.put("routes", entries);
        return tag;
    }
}
