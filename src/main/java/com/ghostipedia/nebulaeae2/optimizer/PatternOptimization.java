package com.ghostipedia.nebulaeae2.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.EncodedProcessingPattern;

public final class PatternOptimization {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NebulaeAE2.MODID);
    public static final DataComponentType<OptimizationProvenance> PROVENANCE = DataComponentType
            .<OptimizationProvenance>builder().persistent(OptimizationProvenance.CODEC)
            .networkSynchronized(OptimizationProvenance.STREAM_CODEC).build();

    private PatternOptimization() {}

    public static void init(IEventBus bus) {
        COMPONENTS.register("pattern_optimization", () -> PROVENANCE);
        COMPONENTS.register(bus);
    }

    public static OptimizationProvenance provenance(ItemStack stack) {
        var provenance = stack.get(PROVENANCE);
        if (provenance != null && (provenance.multiplier() < 2
                || !provenance.expected().equals(stack.get(AEComponents.ENCODED_PROCESSING_PATTERN)))) {
            stack.remove(PROVENANCE);
            return null;
        }
        return provenance;
    }

    public static ItemStack transform(ItemStack original, String operation, long factor) {
        var stack = original.copy();
        var encoded = stack.get(AEComponents.ENCODED_PROCESSING_PATTERN);
        if (encoded == null || encoded.containsMissingContent()) {
            throw new IllegalArgumentException("processing_only");
        }
        var provenance = provenance(stack);
        long cumulative = provenance == null ? 1 : provenance.multiplier();
        boolean divide = !operation.equals("multiply");
        if (divide && provenance == null) {
            throw new IllegalArgumentException("unmarked");
        }
        if (operation.equals("restore")) {
            factor = cumulative;
        } else if (!operation.equals("multiply") && !operation.equals("divide")) {
            throw new IllegalArgumentException("invalid_factor");
        }
        long next = divide ? OptimizerArithmetic.reducedMultiplier(cumulative, factor)
                : OptimizerArithmetic.multiply(cumulative, factor);
        var changed = new EncodedProcessingPattern(scale(encoded.sparseInputs(), factor, divide),
                scale(encoded.sparseOutputs(), factor, divide));
        stack.set(AEComponents.ENCODED_PROCESSING_PATTERN, changed);
        if (next == 1) {
            stack.remove(PROVENANCE);
        } else {
            stack.set(PROVENANCE, new OptimizationProvenance(next, changed));
        }
        return stack;
    }

    private static List<GenericStack> scale(List<GenericStack> source, long factor, boolean divide) {
        var result = new ArrayList<GenericStack>(source.size());
        Map<AEKey, Long> totals = new HashMap<>();
        for (var stack : source) {
            if (stack == null) {
                result.add(null);
                continue;
            }
            long amount = divide ? OptimizerArithmetic.divide(stack.amount(), factor)
                    : OptimizerArithmetic.multiply(stack.amount(), factor);
            if (amount > 999999L * stack.what().getAmountPerUnit()) {
                throw new IllegalArgumentException("quantity_limit");
            }
            try {
                totals.merge(stack.what(), amount, Math::addExact);
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("overflow");
            }
            result.add(new GenericStack(stack.what(), amount));
        }
        return result;
    }
}
