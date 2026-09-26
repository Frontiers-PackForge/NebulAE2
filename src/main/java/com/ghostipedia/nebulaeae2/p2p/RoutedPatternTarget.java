package com.ghostipedia.nebulaeae2.p2p;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import com.ghostipedia.nebulaeae2.blocking.BlockingTarget;
import com.ghostipedia.nebulaeae2.blocking.ProviderBlockingMode;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderTarget;

public final class RoutedPatternTarget implements PatternProviderTarget, BlockingTarget {
    private final PatternP2PTunnelPart input;
    private final PatternProviderRouteHost provider;
    private final PatternTunnelAddress providerAddress;
    private final ProviderBlockingMode blockingMode;
    private final IActionSource source;
    private PatternProviderTarget selected;

    public RoutedPatternTarget(PatternP2PTunnelPart input, PatternProviderRouteHost provider,
            PatternTunnelAddress providerAddress, ProviderBlockingMode blockingMode, IActionSource source) {
        this.input = input;
        this.provider = provider;
        this.providerAddress = providerAddress;
        this.blockingMode = blockingMode;
        this.source = source;
    }

    public boolean prepare(KeyCounter[] ingredients) {
        var outputs = PatternTunnelRouting.outputs(input);
        if (outputs.isEmpty()) {
            return false;
        }
        var server = input.getLevel().getServer();
        var pending = PendingPatternRoutes.get(server);
        var recognized = PatternTunnelRouting.recognizedInputs(input);
        var targets = new HashMap<UUID, PatternProviderTarget>();
        var outputId = PatternTunnelRouting.selector(input).select(outputs.stream().map(PatternP2PTunnelPart::identity).toList(), id -> {
            var output = outputs.stream().filter(candidate -> candidate.identity().equals(id)).findFirst().orElseThrow();
            var destination = output.destination();
            if (!destination.loaded(server) || pending.occupied(destination, provider.nebulae$providerIdentity(), server)) {
                return false;
            }
            var target = PatternProviderTarget.get(destination.level(server), destination.position(), null, destination.side(), source);
            if (target == null || target instanceof BlockingTarget blocking && blocking.nebulae$blocks(blockingMode, recognized)) {
                return false;
            }
            for (var alternatives : ingredients) {
                for (var entry : alternatives) {
                    if (target.insert(entry.getKey(), entry.getLongValue(), Actionable.SIMULATE) == 0) {
                        return false;
                    }
                }
            }
            targets.put(id, target);
            return true;
        });
        if (outputId == null) {
            return false;
        }
        var output = outputs.stream().filter(candidate -> candidate.identity().equals(outputId)).findFirst().orElseThrow();
        selected = targets.get(outputId);
        var route = new PatternProviderRoute(provider.nebulae$providerIdentity(), providerAddress, input.address(), input.identity(),
                output.address(), output.identity(), input.getFrequency(), false);
        provider.nebulae$setRoute(route);
        pending.claim(route);
        return true;
    }

    @Override
    public long insert(AEKey key, long amount, Actionable mode) {
        return selected == null ? 0 : selected.insert(key, amount, mode);
    }

    @Override
    public boolean containsPatternInput(Set<AEKey> inputs) {
        return false;
    }

    @Override
    public boolean nebulae$blocks(ProviderBlockingMode mode, Set<AEKey> inputs) {
        return false;
    }
}
