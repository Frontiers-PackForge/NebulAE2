package com.ghostipedia.nebulaeae2.pattern;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
import com.ghostipedia.nebulaeae2.optimizer.PatternOptimization;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.api.stacks.AEItemKey;

public final class PatternAuthorship {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NebulaeAE2.MODID);
    public static final DataComponentType<PatternAuthor> AUTHOR = DataComponentType.<PatternAuthor>builder()
            .persistent(PatternAuthor.CODEC).networkSynchronized(PatternAuthor.STREAM_CODEC).build();

    private PatternAuthorship() {}

    public static void init(IEventBus bus) {
        COMPONENTS.register("pattern_author", () -> AUTHOR);
        COMPONENTS.register(bus);
    }

    public static ItemStack contentCopy(ItemStack original) {
        PatternOptimization.provenance(original);
        if (!original.has(AUTHOR) && !original.has(PatternOptimization.PROVENANCE)) {
            return original;
        }
        ItemStack copy = original.copy();
        copy.remove(AUTHOR);
        copy.remove(PatternOptimization.PROVENANCE);
        return copy;
    }

    public static AEItemKey contentKey(AEItemKey original) {
        if (original == null || original.get(AUTHOR) == null && original.get(PatternOptimization.PROVENANCE) == null) {
            return original;
        }
        return AEItemKey.of(contentCopy(original.toStack()));
    }
}
