package com.ghostipedia.nebulaeae2.pattern;

import com.ghostipedia.nebulaeae2.NebulaeAE2;
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
        if (!original.has(AUTHOR)) {
            return original;
        }
        ItemStack copy = original.copy();
        copy.remove(AUTHOR);
        return copy;
    }

    public static AEItemKey contentKey(AEItemKey original) {
        if (original == null || original.get(AUTHOR) == null) {
            return original;
        }
        return AEItemKey.of(contentCopy(original.toStack()));
    }
}
