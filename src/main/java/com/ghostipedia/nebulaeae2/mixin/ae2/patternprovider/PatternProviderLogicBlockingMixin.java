package com.ghostipedia.nebulaeae2.mixin.ae2.patternprovider;

import java.util.Set;
import com.ghostipedia.nebulaeae2.blocking.BlockingTarget;
import com.ghostipedia.nebulaeae2.blocking.ProviderBlockingMode;
import com.ghostipedia.nebulaeae2.blocking.ProviderBlockingSettings;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigManagerBuilder;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderTarget;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatternProviderLogic.class)
public abstract class PatternProviderLogicBlockingMixin {
    @Shadow @Final private IConfigManager configManager;
    @Shadow @Final private Set<AEKey> patternInputs;
    @Shadow private PatternProviderTarget findAdapter(Direction side) { throw new AssertionError(); }

    @Redirect(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V", at = @At(value = "INVOKE", target = "Lappeng/api/util/IConfigManagerBuilder;build()Lappeng/api/util/IConfigManager;"))
    private IConfigManager nebulae$registerMode(IConfigManagerBuilder builder) {
        return builder.registerSetting(ProviderBlockingSettings.MODE, ProviderBlockingMode.OFF).build();
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void nebulae$readLegacyBlocking(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo callback) {
        if (!tag.contains(ProviderBlockingSettings.MODE.getName())) {
            configManager.putSetting(ProviderBlockingSettings.MODE,
                    configManager.getSetting(Settings.BLOCKING_MODE) == YesNo.YES ? ProviderBlockingMode.PATTERN_INPUTS : ProviderBlockingMode.OFF);
        }
    }

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    private void nebulae$isBlocking(CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(configManager.getSetting(ProviderBlockingSettings.MODE) != ProviderBlockingMode.OFF);
    }

    @Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Lappeng/helpers/patternprovider/PatternProviderTarget;containsPatternInput(Ljava/util/Set;)Z"))
    private boolean nebulae$blockTarget(PatternProviderTarget target, Set<AEKey> inputs) {
        return target instanceof BlockingTarget blocking && blocking.nebulae$blocks(configManager.getSetting(ProviderBlockingSettings.MODE), inputs);
    }

    @Redirect(method = "pushPattern", at = @At(value = "INVOKE", target = "Lappeng/api/implementations/blockentities/ICraftingMachine;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;Lnet/minecraft/core/Direction;)Z"))
    private boolean nebulae$blockDedicatedMachine(ICraftingMachine machine, IPatternDetails pattern, KeyCounter[] inputs, Direction side) {
        var mode = configManager.getSetting(ProviderBlockingSettings.MODE);
        if (mode != ProviderBlockingMode.OFF) {
            var target = findAdapter(side.getOpposite());
            if (target instanceof BlockingTarget blocking && blocking.nebulae$blocks(mode, patternInputs)) {
                return false;
            }
        }
        return machine.pushPattern(pattern, inputs, side);
    }
}
