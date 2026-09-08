package com.ghostipedia.nebulaeae2.blocking;

import java.util.Set;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.helpers.patternprovider.PatternProviderTarget;

public record StorageBlockingTarget(MEStorage storage, IActionSource source) implements PatternProviderTarget, BlockingTarget {
    @Override
    public long insert(AEKey key, long amount, Actionable mode) {
        return storage.insert(key, amount, mode, source);
    }

    @Override
    public boolean containsPatternInput(Set<AEKey> inputs) {
        return nebulae$blocks(ProviderBlockingMode.PATTERN_INPUTS, inputs);
    }

    @Override
    public boolean nebulae$blocks(ProviderBlockingMode mode, Set<AEKey> inputs) {
        return ProviderBlockingPolicy.blocks(mode, inputs, storage.getAvailableStacks());
    }
}
