package com.ghostipedia.nebulaeae2.blocking;

import java.util.HashSet;
import java.util.Set;

import com.gregtechceu.gtceu.common.data.GTItems;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;

public final class ProviderBlockingPolicy {
    private ProviderBlockingPolicy() {}

    public static Set<AEKey> inputs(Iterable<IPatternDetails> patterns) {
        var result = new HashSet<AEKey>();
        for (var pattern : patterns) {
            for (var input : pattern.getInputs()) {
                for (var candidate : input.getPossibleInputs()) {
                    result.add(candidate.what().dropSecondary());
                }
            }
        }
        return result;
    }

    public static boolean blocks(ProviderBlockingMode mode, Set<AEKey> inputs, AEKey key) {
        boolean circuit = key instanceof AEItemKey item && item.getItem() == GTItems.PROGRAMMED_CIRCUIT.asItem();
        return mode.blocks(inputs.contains(key.dropSecondary()), circuit);
    }

    public static boolean blocks(ProviderBlockingMode mode, Set<AEKey> inputs, KeyCounter contents) {
        for (var entry : contents) {
            if (entry.getLongValue() > 0 && blocks(mode, inputs, entry.getKey())) {
                return true;
            }
        }
        return false;
    }
}
