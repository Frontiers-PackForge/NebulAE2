package com.ghostipedia.nebulaeae2.p2p;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import com.ghostipedia.nebulaeae2.blocking.ProviderBlockingPolicy;

import appeng.api.AECapabilities;
import appeng.api.networking.IGrid;
import appeng.api.parts.PartHelper;
import appeng.api.stacks.AEKey;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.capabilities.Capabilities;

public final class PatternTunnelRouting {
    private static final Map<IGrid, Map<Short, PatternOutputSelector<UUID>>> SELECTORS = new WeakHashMap<>();

    private PatternTunnelRouting() {}

    public static boolean frequencyInUse(IGrid grid, short frequency) {
        return grid.getMachines(PatternP2PTunnelPart.class).stream().anyMatch(part -> part.getFrequency() == frequency);
    }

    public static List<PatternP2PTunnelPart> outputs(PatternP2PTunnelPart input) {
        var grid = input.getMainNode().getGrid();
        if (grid == null || input.isOutput() || input.getFrequency() == 0 || !input.getMainNode().isActive()) {
            return List.of();
        }
        return grid.getMachines(PatternP2PTunnelPart.class).stream()
                .filter(part -> part.isOutput() && part.getFrequency() == input.getFrequency() && part.getMainNode().isActive())
                .sorted(Comparator.comparing((PatternP2PTunnelPart part) -> part.address().dimension().location().toString())
                        .thenComparingLong(part -> part.address().position().asLong())
                        .thenComparingInt(part -> part.getSide().get3DDataValue()))
                .toList();
    }

    public static PatternOutputSelector<UUID> selector(PatternP2PTunnelPart input) {
        return SELECTORS.computeIfAbsent(input.getMainNode().getGrid(), ignored -> new HashMap<>())
                .computeIfAbsent(input.getFrequency(), ignored -> new PatternOutputSelector<>());
    }

    public static Set<AEKey> recognizedInputs(PatternP2PTunnelPart input) {
        var result = new HashSet<AEKey>();
        var grid = input.getMainNode().getGrid();
        if (grid == null) {
            return result;
        }
        for (var part : grid.getMachines(PatternP2PTunnelPart.class)) {
            if (!part.isOutput() && part.getFrequency() == input.getFrequency()) {
                var provider = providerAt(part.destination(), part.getLevel().getServer());
                if (provider != null && provider.getTargets().contains(part.getSide().getOpposite())) {
                    result.addAll(ProviderBlockingPolicy.inputs(provider.getLogic().getAvailablePatterns()));
                }
            }
        }
        return result;
    }

    public static PatternP2PTunnelPart tunnelAt(PatternTunnelAddress address, MinecraftServer server) {
        if (!address.loaded(server)) {
            return null;
        }
        var host = PartHelper.getPartHost(address.level(server), address.position());
        return host != null && host.getPart(address.side()) instanceof PatternP2PTunnelPart tunnel ? tunnel : null;
    }

    public static PatternProviderLogicHost providerAt(PatternTunnelAddress address, MinecraftServer server) {
        if (!address.loaded(server)) {
            return null;
        }
        var level = address.level(server);
        var entity = level.getBlockEntity(address.position());
        if (entity instanceof PatternProviderLogicHost provider) {
            return provider;
        }
        var host = PartHelper.getPartHost(level, address.position());
        return host != null && host.getPart(address.side()) instanceof PatternProviderLogicHost provider ? provider : null;
    }

    public static boolean sameDestination(PatternTunnelAddress first, PatternTunnelAddress second, MinecraftServer server) {
        var firstIdentity = new PatternDestinationIdentity(first, first.loaded(server) ? destinationCapabilities(first, server) : List.of());
        var secondIdentity = new PatternDestinationIdentity(second, second.loaded(server) ? destinationCapabilities(second, server) : List.of());
        return firstIdentity.sharesInventory(secondIdentity);
    }

    private static List<Object> destinationCapabilities(PatternTunnelAddress address, MinecraftServer server) {
        var level = address.level(server);
        var result = new ArrayList<Object>();
        var storage = level.getCapability(AECapabilities.ME_STORAGE, address.position(), address.side());
        var items = level.getCapability(Capabilities.ItemHandler.BLOCK, address.position(), address.side());
        var fluids = level.getCapability(Capabilities.FluidHandler.BLOCK, address.position(), address.side());
        if (storage != null) {
            result.add(storage);
        }
        if (items != null) {
            result.add(items);
        }
        if (fluids != null) {
            result.add(fluids);
        }
        return result;
    }
}
