package com.ghostipedia.nebulaeae2.mixin.ae2.storage.sticky;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.ArrayList;
import java.util.List;

import com.ghostipedia.nebulaeae2.sticky.StickyPartitionGuard;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;

import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GenericStackInv.class, remap = false)
public abstract class ConfigInventoryStickyMixin implements StickyPartitionGuard {
    @Shadow @Final protected GenericStack[] stacks;
    @Unique private BooleanSupplier nebulae$stickyProtected;
    @Unique private IntSupplier nebulae$activeSlots = () -> Integer.MAX_VALUE;
    @Unique private int nebulae$replacementDepth;

    @Override
    public void nebulae$protectStickyPartition(BooleanSupplier protectedPartition, IntSupplier activeSlots) {
        nebulae$stickyProtected = protectedPartition;
        nebulae$activeSlots = activeSlots;
    }

    @Unique
    private boolean nebulae$protected() {
        return nebulae$replacementDepth == 0 && nebulae$stickyProtected != null
                && nebulae$stickyProtected.getAsBoolean();
    }

    @Override
    public boolean nebulae$beginPartitionReplacement(List<GenericStack> replacement) {
        if (nebulae$protected()) {
            var inventory = (GenericStackInv) (Object) this;
            boolean valid = false;
            int limit = Math.min(nebulae$activeSlots.getAsInt(), Math.min(stacks.length, replacement.size()));
            for (int slot = 0; slot < limit; slot++) {
                var stack = replacement.get(slot);
                if (stack != null && inventory.isAllowedIn(slot, stack.what())) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                return false;
            }
        }
        nebulae$replacementDepth++;
        return true;
    }

    @Override
    public void nebulae$endPartitionReplacement() {
        nebulae$replacementDepth--;
    }

    @WrapMethod(method = "readFromList")
    private void nebulae$replaceList(List<GenericStack> replacement, Operation<Void> original) {
        if (!nebulae$beginPartitionReplacement(replacement)) {
            return;
        }
        try {
            original.call(replacement);
        } finally {
            nebulae$endPartitionReplacement();
        }
    }

    @WrapMethod(method = "readFromTag")
    private void nebulae$replaceTag(ListTag tag, HolderLookup.Provider registries, Operation<Void> original) {
        if (!nebulae$protected()) {
            original.call(tag, registries);
            return;
        }
        var replacement = new ArrayList<GenericStack>();
        for (int slot = 0; slot < Math.min(tag.size(), stacks.length); slot++) {
            replacement.add(GenericStack.readTag(registries, tag.getCompound(slot)));
        }
        if (!nebulae$beginPartitionReplacement(replacement)) {
            return;
        }
        try {
            original.call(tag, registries);
        } finally {
            nebulae$endPartitionReplacement();
        }
    }

    @Inject(method = "setStack", at = @At("HEAD"), cancellable = true)
    private void nebulae$keepFinalEntry(int slot, GenericStack value, CallbackInfo ci) {
        if (value != null || !nebulae$protected() || stacks[slot] == null) {
            return;
        }
        for (int i = 0; i < Math.min(stacks.length, nebulae$activeSlots.getAsInt()); i++) {
            if (i != slot && stacks[i] != null) {
                return;
            }
        }
        ci.cancel();
    }

    @Inject(method = "clear", at = @At("HEAD"), cancellable = true)
    private void nebulae$preventClear(CallbackInfo ci) {
        if (nebulae$protected()) {
            ci.cancel();
        }
    }
}
