package com.ghostipedia.nebulaeae2.mixin.ae2.storage.sticky;

import com.ghostipedia.nebulaeae2.sticky.StickyPartitionGuard;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import appeng.blockentity.misc.CellWorkbenchBlockEntity;
import appeng.helpers.externalstorage.GenericStackInv;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CellWorkbenchBlockEntity.class, remap = false)
public abstract class CellWorkbenchStickyMixin {
    @WrapMethod(method = "copy")
    private static void nebulae$copyPartition(GenericStackInv from, GenericStackInv to, Operation<Void> original) {
        var guard = (StickyPartitionGuard) to;
        if (!guard.nebulae$beginPartitionReplacement(from.toList())) {
            return;
        }
        try {
            original.call(from, to);
        } finally {
            guard.nebulae$endPartitionReplacement();
        }
    }
}
